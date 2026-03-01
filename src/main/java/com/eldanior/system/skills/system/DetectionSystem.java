package com.eldanior.system.skills.system;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DetectionSystem extends EntityTickingSystem<EntityStore> {

    private static class PlayerRadarInfo {
        Vector3d pos;
        float threatRangeSq;
        long lastSeen;
        long lastMessageTime;

        // On sépare les Mobs et les Joueurs
        final Set<Ref<EntityStore>> detectedMobs = ConcurrentHashMap.newKeySet();
        final Set<Ref<EntityStore>> detectedPlayers = ConcurrentHashMap.newKeySet();

        IPassiveCombatSkill activeSkillLogic = null;
    }

    private final Map<Ref<EntityStore>, PlayerRadarInfo> activeRadars = new ConcurrentHashMap<>();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        PlayerLevelData pData = archetypeChunk.getComponent(index, EldaniorSystem.get().getPlayerLevelDataType());
        boolean isPlayer = (pData != null);

        // 1. MISE À JOUR DU RADAR DU JOUEUR (S'il en a un)
        if (isPlayer) {
            updatePlayerRadar(index, archetypeChunk, entityRef, pData, store);
        }

        // 2. DÉTECTION DES CIBLES (Les monstres ET les autres joueurs)
        if (activeRadars.isEmpty()) return;

        boolean isMob = archetypeChunk.getComponent(index, EldaniorSystem.get().getMobLevelDataType()) != null;

        if (isPlayer || isMob) {
            TransformComponent targetTransform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
            if (targetTransform != null) {
                processTargetDetection(entityRef, targetTransform.getPosition(), isPlayer);
            }
        }
    }

    private void updatePlayerRadar(int index, ArchetypeChunk<EntityStore> chunk, Ref<EntityStore> playerEntityRef, PlayerLevelData pData, Store<EntityStore> store) {
        ClassModel playerClass = ClassManager.getByDisplayName(pData.getPlayerClass());
        if (playerClass == null) return;

        TransformComponent playerTransform = chunk.getComponent(index, TransformComponent.getComponentType());
        if (playerTransform == null) return;

        float threatRange = StatConfig.THREAT_AWARENESS.getFinalValue(pData, playerClass);

        if (threatRange <= 10.0f) {
            activeRadars.remove(playerEntityRef);
            return;
        }

        PlayerRadarInfo radar = activeRadars.computeIfAbsent(playerEntityRef, k -> new PlayerRadarInfo());
        radar.pos = playerTransform.getPosition();
        radar.threatRangeSq = threatRange * threatRange;
        radar.lastSeen = System.currentTimeMillis();

        if (pData.getActivePassives() != null) {
            for (PassiveSkill skill : pData.getActivePassives()) {
                if (skill.getLogic() != null && skill.getLogic().getRadarMessage(0, 0, 0) != null) {
                    radar.activeSkillLogic = skill.getLogic();
                    break;
                }
            }
        }

        radar.detectedMobs.removeIf(ref -> ref == null || !ref.isValid());
        radar.detectedPlayers.removeIf(ref -> ref == null || !ref.isValid());

        long now = System.currentTimeMillis();
        if (now - radar.lastMessageTime >= 20000) {
            int mCount = radar.detectedMobs.size();
            int pCount = radar.detectedPlayers.size();

            if (mCount > 0 || pCount > 0) {
                PlayerRef pRef = chunk.getComponent(index, PlayerRef.getComponentType());
                if (pRef != null) {

                    // --- CALCUL DE LA DISTANCE LA PLUS PROCHE ---
                    double closestDistSq = Double.MAX_VALUE;

                    for (Ref<EntityStore> target : radar.detectedMobs) {
                        TransformComponent t = store.getComponent(target, TransformComponent.getComponentType());
                        if (t != null) {
                            Vector3d tPos = t.getPosition();
                            double dx = radar.pos.x - tPos.x;
                            double dy = radar.pos.y - tPos.y;
                            double dz = radar.pos.z - tPos.z;
                            closestDistSq = Math.min(closestDistSq, (dx * dx) + (dy * dy) + (dz * dz));
                        }
                    }

                    for (Ref<EntityStore> target : radar.detectedPlayers) {
                        TransformComponent t = store.getComponent(target, TransformComponent.getComponentType());
                        if (t != null) {
                            Vector3d tPos = t.getPosition();
                            double dx = radar.pos.x - tPos.x;
                            double dy = radar.pos.y - tPos.y;
                            double dz = radar.pos.z - tPos.z;
                            closestDistSq = Math.min(closestDistSq, (dx * dx) + (dy * dy) + (dz * dz));
                        }
                    }

                    int closestDist = (int) Math.sqrt(closestDistSq);

                    // --- ENVOI DU MESSAGE ---
                    String message = "<color:red>" + (mCount + pCount) + " menace(s) à " + closestDist + "m</color>";
                    NotificationStyle style = NotificationStyle.Warning;

                    if (radar.activeSkillLogic != null) {
                        String customMsg = radar.activeSkillLogic.getRadarMessage(mCount, pCount, closestDist);
                        if (customMsg != null) message = customMsg;

                        NotificationStyle customStyle = radar.activeSkillLogic.getRadarStyle();
                        if (customStyle != null) style = customStyle;
                    }

                    NotificationHelper.sendNotification(pRef, message, style);
                }
            }
            radar.lastMessageTime = now;
        }
    }

    private void processTargetDetection(Ref<EntityStore> targetRef, Vector3d targetPos, boolean isPlayer) {
        long now = System.currentTimeMillis();

        for (Map.Entry<Ref<EntityStore>, PlayerRadarInfo> entry : activeRadars.entrySet()) {
            Ref<EntityStore> radarOwner = entry.getKey();
            PlayerRadarInfo radar = entry.getValue();

            // Ne pas s'ajouter soi-même à son propre radar
            if (radarOwner.equals(targetRef)) continue;

            if (now - radar.lastSeen > 2000) {
                activeRadars.remove(radarOwner);
                continue;
            }

            double dx = radar.pos.x - targetPos.x;
            double dy = radar.pos.y - targetPos.y;
            double dz = radar.pos.z - targetPos.z;
            double distSq = (dx * dx) + (dy * dy) + (dz * dz);

            if (distSq <= radar.threatRangeSq) {
                if (isPlayer) radar.detectedPlayers.add(targetRef);
                else radar.detectedMobs.add(targetRef);
            } else {
                if (isPlayer) radar.detectedPlayers.remove(targetRef);
                else radar.detectedMobs.remove(targetRef);
            }
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return TransformComponent.getComponentType();
    }
}