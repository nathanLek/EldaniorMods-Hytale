package com.eldanior.system.config.configs.system;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.config.configs.MobXP;
import com.eldanior.system.config.configs.Mobs.IMobConfig; // <-- NOUVEL IMPORT ICI
import com.eldanior.system.config.configs.Mobs.MobLevelData;
import com.eldanior.system.config.configs.Mobs.MobVirtualHPSystem;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MobNameplateColorSystem extends EntityTickingSystem<EntityStore> {

    private float updateTimer = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        updateTimer += dt;
        if (updateTimer < 0.5f) return;
        if (index == 0) updateTimer = 0;

        Ref<EntityStore> mobRef = chunk.getReferenceTo(index);
        if (!mobRef.isValid()) return;

        NPCEntity npc = store.getComponent(mobRef, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npc == null) return;

        String mobTypeId = npc.getNPCTypeId();

        String specialTitle = getSpecialNPCTitle(mobTypeId);

        if (specialTitle != null) {
            Nameplate nameplate = new Nameplate(specialTitle);
            commandBuffer.putComponent(mobRef, Nameplate.getComponentType(), nameplate);
            return;
        }

        ComponentType<EntityStore, MobLevelData> mobLevelType = EldaniorSystem.get().getMobLevelDataType();
        MobLevelData mobData = store.getComponent(mobRef, mobLevelType);

        if (mobData == null || !mobData.isStatsApplied()) return;

        TransformComponent mobTransform = store.getComponent(mobRef, TransformComponent.getComponentType());
        if (mobTransform == null) return;

        Vector3d mobPos = mobTransform.getPosition();
        int mobLevel = mobData.getLevel();

        String hpText = getHPText(mobRef, store);
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

    private @Nullable String getSpecialNPCTitle(String mobTypeId) {
        if (mobTypeId == null) return null;

        // CORRECTION ICI : On utilise IMobConfig au lieu de MobXP
        IMobConfig mobData = MobXP.getMobDataForId(mobTypeId);
        return mobData.getCustomTitle();
    }

    private String getHPText(Ref<EntityStore> mobRef, Store<EntityStore> store) {
        EntityStatMap statMap = store.getComponent(mobRef,
                EntityStatsModule.get().getEntityStatMapComponentType());

        ComponentType<EntityStore, MobLevelData> mobLevelType =
                EldaniorSystem.get().getMobLevelDataType();
        MobLevelData mobData = store.getComponent(mobRef, mobLevelType);

        if (statMap != null && mobData != null) {
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

    private @Nullable UUID getPlayerUUID(Vector3d mobPos) {
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

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}