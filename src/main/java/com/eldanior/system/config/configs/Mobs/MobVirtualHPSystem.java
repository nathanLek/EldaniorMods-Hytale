package com.eldanior.system.config.configs.Mobs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.configs.MobXP;
import com.eldanior.system.config.configs.MobsWorldConfig;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Random;

public class MobVirtualHPSystem extends EntityTickingSystem<EntityStore> {

    private final Random random = new Random();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Ref<EntityStore> mobRef = chunk.getReferenceTo(index);
        if (!mobRef.isValid()) return;

        NPCEntity npc = store.getComponent(mobRef, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npc == null) return;

        ComponentType<EntityStore, MobLevelData> mobLevelType = EldaniorSystem.get().getMobLevelDataType();
        MobLevelData existingData = store.getComponent(mobRef, mobLevelType);

        if (existingData != null && existingData.isStatsApplied()) {
            return;
        }

        String mobTypeId = npc.getNPCTypeId();

        int minLevel = MobXP.getMinLevelForId(mobTypeId);
        int maxLevel = MobXP.getMaxLevelForId(mobTypeId);
        int xpReward = MobXP.getXpForId(mobTypeId);

        if (minLevel == 0 && maxLevel == 0) return;

        // Récupère les HP de base du mob
        EntityStatMap statMap = store.getComponent(mobRef,
                EntityStatsModule.get().getEntityStatMapComponentType());

        float baseHP = 100.0f; // Valeur par défaut
        if (statMap != null) {
            int healthIndex = DefaultEntityStatTypes.getHealth();
            if (statMap.get(healthIndex) != null) {
                baseHP = Objects.requireNonNull(statMap.get(healthIndex)).getMax();
            }
        }

        int level = minLevel + random.nextInt(maxLevel - minLevel + 1);

        // Stocke le niveau ET les HP de base
        MobLevelData mobLevelData = new MobLevelData(level, mobTypeId, true);
        commandBuffer.putComponent(mobRef, mobLevelType, mobLevelData);

        System.out.println("✅ [" + mobTypeId + "] Lv." + level +
                " | Base HP: " + baseHP +
                " | HP virtuels: " + getVirtualMaxHP(level, baseHP) +
                " | XP:" + xpReward +
                " | DMG: +" + (level * MobsWorldConfig.DAMAGE_PER_LEVEL));
    }

    public static float getVirtualMaxHP(int level, float baseHP) {
        return baseHP + (level * MobsWorldConfig.HP_PER_LEVEL);
    }

    public static float getHPMultiplier(int level, float baseHP) {
        float virtualMax = getVirtualMaxHP(level, baseHP);
        return virtualMax / baseHP;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Objects.requireNonNull(NPCEntity.getComponentType());
    }
}