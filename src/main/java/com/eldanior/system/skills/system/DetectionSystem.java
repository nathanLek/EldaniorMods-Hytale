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
import com.hypixel.hytale.server.core.entity.entities.Player;
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

        if (isPlayer) {
            updatePlayerRadar(index, archetypeChunk, entityRef, pData, store);
        }

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
                // CORRECTION ICI : On utilise des valeurs vides correspondant à la nouvelle méthode
                if (skill.getLogic() != null && skill.getLogic().getRadarMessage(java.util.Collections.emptyList(), 0, 0) != null) {
                    radar.activeSkillLogic = skill.getLogic();
                    break;
                }
            }
        }

        radar.detectedMobs.removeIf(ref -> ref == null || !ref.isValid());
        radar.detectedPlayers.removeIf(ref -> ref == null || !ref.isValid());

        long now = System.currentTimeMillis();
        if (now - radar.lastMessageTime >= 20000) {

            if (!radar.detectedMobs.isEmpty() || !radar.detectedPlayers.isEmpty()) {
                PlayerRef pRef = chunk.getComponent(index, PlayerRef.getComponentType());
                if (pRef != null) {

                    // --- CRÉATION DE LA LISTE DES CIBLES ---
                    java.util.List<IPassiveCombatSkill.RadarTarget> allTargets = new java.util.ArrayList<>();

                    for (Ref<EntityStore> target : radar.detectedMobs) {
                        TransformComponent t = store.getComponent(target, TransformComponent.getComponentType());
                        if (t != null) {
                            Vector3d tPos = t.getPosition();
                            double dx = radar.pos.x - tPos.x;
                            double dy = radar.pos.y - tPos.y;
                            double dz = radar.pos.z - tPos.z;
                            int dist = (int) Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));

                            // ⚠️ Adapte ici si tu as une méthode pour récupérer le vrai nom du mob
                            String mobName = "Monstre";
                            allTargets.add(new IPassiveCombatSkill.RadarTarget(mobName, dist, false));
                        }
                    }

                    for (Ref<EntityStore> target : radar.detectedPlayers) {
                        TransformComponent t = store.getComponent(target, TransformComponent.getComponentType());
                        Player targetPlayer = store.getComponent(target, Player.getComponentType());
                        if (t != null && targetPlayer != null) {
                            Vector3d tPos = t.getPosition();
                            double dx = radar.pos.x - tPos.x;
                            double dy = radar.pos.y - tPos.y;
                            double dz = radar.pos.z - tPos.z;
                            int dist = (int) Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));

                            String playerName = targetPlayer.getDisplayName();
                            allTargets.add(new IPassiveCombatSkill.RadarTarget(playerName != null ? playerName : "Joueur", dist, true));
                        }
                    }

                    // On trie tout le monde du plus proche au plus éloigné
                    java.util.Collections.sort(allTargets);

                    // On garde les 3 premiers (les plus proches), et on compte le reste
                    java.util.List<IPassiveCombatSkill.RadarTarget> closestTargets = new java.util.ArrayList<>();
                    int extraMobs = 0;
                    int extraPlayers = 0;

                    for (int i = 0; i < allTargets.size(); i++) {
                        if (i < 3) {
                            closestTargets.add(allTargets.get(i));
                        } else {
                            // En Java (Record), on accède aux variables par nomDeVariable()
                            if (allTargets.get(i).isPlayer()) extraPlayers++;
                            else extraMobs++;
                        }
                    }

                    // --- ENVOI DU MESSAGE ---
                    int total = allTargets.size();
                    String message = "<color:red>" + total + " menace(s) à proximité</color>";
                    NotificationStyle style = NotificationStyle.Warning;

                    if (radar.activeSkillLogic != null) {
                        String customMsg = radar.activeSkillLogic.getRadarMessage(closestTargets, extraMobs, extraPlayers);
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