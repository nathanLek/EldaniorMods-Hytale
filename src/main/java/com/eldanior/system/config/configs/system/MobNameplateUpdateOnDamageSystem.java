package com.eldanior.system.config.configs.system;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.config.configs.Mobs.MobLevelData;
import com.eldanior.system.config.configs.Mobs.MobVirtualHPSystem;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MobNameplateUpdateOnDamageSystem extends DamageEventSystem {

    @Override
    public SystemGroup<EntityStore> getGroup() {
        // S'exécute APRÈS que les dégâts soient appliqués
        return DamageModule.get().getInspectDamageGroup();
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                       @Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> commandBuffer,
                       @Nonnull Damage damage) {

        Ref<EntityStore> targetRef = chunk.getReferenceTo(index);

        // Vérifie si la cible est un NPC avec un niveau
        NPCEntity npc = store.getComponent(targetRef, NPCEntity.getComponentType());
        if (npc == null) return;

        ComponentType<EntityStore, MobLevelData> mobLevelType = EldaniorSystem.get().getMobLevelDataType();
        MobLevelData mobData = store.getComponent(targetRef, mobLevelType);

        if (mobData == null || !mobData.isStatsApplied()) return;

        // Met à jour la nameplate IMMÉDIATEMENT après les dégâts
        updateNameplate(targetRef, npc, mobData, store, commandBuffer);
    }

    private void updateNameplate(Ref<EntityStore> mobRef, NPCEntity npc, MobLevelData mobData,
                                 Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {

        TransformComponent mobTransform = store.getComponent(mobRef, TransformComponent.getComponentType());
        if (mobTransform == null) return;

        Vector3d mobPos = mobTransform.getPosition();
        int mobLevel = mobData.getLevel();

        // Récupère les HP actuels
        String hpText = getHPText(mobRef, mobData, store);

        // Trouve le joueur le plus proche
        UUID nearestPlayerUUID = getPlayerUUID(mobPos);

        String nameplateText;
        if (nearestPlayerUUID != null) {
            Integer playerLevel = PlayerPositionTracker.PLAYER_LEVELS.get(nearestPlayerUUID);
            if (playerLevel != null) {
                nameplateText = getSymbolNameplate(playerLevel, mobLevel) + hpText;
            } else {
                nameplateText = "Lv." + mobLevel + hpText;
            }
        } else {
            nameplateText = "Lv." + mobLevel + hpText;
        }

        Nameplate nameplate = new Nameplate(nameplateText);
        commandBuffer.putComponent(mobRef, Nameplate.getComponentType(), nameplate);
    }

    private String getHPText(Ref<EntityStore> mobRef, MobLevelData mobData, Store<EntityStore> store) {
        EntityStatMap statMap = store.getComponent(mobRef,
                EntityStatsModule.get().getEntityStatMapComponentType());

        if (statMap != null) {
            int healthIndex = DefaultEntityStatTypes.getHealth();
            if (statMap.get(healthIndex) != null) {
                float currentHPReal = Objects.requireNonNull(statMap.get(healthIndex)).get();
                float baseHP = Objects.requireNonNull(statMap.get(healthIndex)).getMax();

                float multiplier = MobVirtualHPSystem.getHPMultiplier(mobData.getLevel(), baseHP);
                float virtualMaxHP = MobVirtualHPSystem.getVirtualMaxHP(mobData.getLevel(), baseHP);
                float virtualCurrentHP = currentHPReal * multiplier;

                int currentHPInt = Math.round(virtualCurrentHP);
                int maxHPInt = Math.round(virtualMaxHP);

                return " - [" + currentHPInt + "/" + maxHPInt + "]";
            }
        }
        return "";
    }

    private UUID getPlayerUUID(Vector3d mobPos) {
        UUID nearestPlayerUUID = null;
        double nearestDistance = 15.0;

        for (Map.Entry<UUID, Vector3d> entry : PlayerPositionTracker.PLAYER_POSITIONS.entrySet()) {
            Vector3d playerPos = entry.getValue();
            double distance = calculateDistance(mobPos, playerPos);

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestPlayerUUID = entry.getKey();
            }
        }
        return nearestPlayerUUID;
    }

    private double calculateDistance(Vector3d pos1, Vector3d pos2) {
        double dx = pos1.x - pos2.x;
        double dy = pos1.y - pos2.y;
        double dz = pos1.z - pos2.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private String getSymbolNameplate(int playerLevel, int mobLevel) {
        int diff = mobLevel - playerLevel;

        if (diff <= 0) {
            return "Lv." + mobLevel;
        } else if (mobLevel >= playerLevel * 2) {
            return "Lv." + mobLevel;
        } else {
            return "Lv." + mobLevel;
        }
    }
}