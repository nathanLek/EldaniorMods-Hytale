package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.components.PlayerLevelData;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FallDamageSystem extends DamageEventSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Damage damage) {

        Damage.Source source = damage.getSource();
        String sourceName = source.getClass().getSimpleName();

        if (!sourceName.contains("Fall")) {
            return;
        }

        Ref<EntityStore> victimRef = archetypeChunk.getReferenceTo(index);
        if (!victimRef.isValid()) return;

        PlayerLevelData data = store.getComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType());

        if (data != null) {
            float damageAmount = damage.getAmount();
            float reductionPercent = data.getAgility() * 0.00034f;
            if (reductionPercent > 1.0f) reductionPercent = 1.0f;
            float newDamage = damageAmount * (1.0f - reductionPercent);
            if (Math.abs(newDamage - damageAmount) > 0.01f) {
                damage.setAmount(newDamage);
            }
        }
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public SystemGroup<EntityStore> getGroup() {
        try {
            Class<?> mod = Class.forName("com.hypixel.hytale.server.core.modules.entity.damage.DamageModule");
            Object inst = mod.getMethod("get").invoke(null);
            return (SystemGroup<EntityStore>) mod.getMethod("getFilterDamageGroup").invoke(inst);
        } catch (Throwable e) {
            return null;
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}