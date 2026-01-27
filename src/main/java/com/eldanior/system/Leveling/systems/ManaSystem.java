package com.eldanior.system.Leveling.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class ManaSystem extends EntityTickingSystem<EntityStore> {

    private float timer = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        timer += dt;
        if (timer < 10.0f) return;
        if (index == 0) timer = 0;

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        EntityStatMap statMap = store.getComponent(entityRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        int manaIndex = DefaultEntityStatTypes.getMana();
        EntityStatValue manaValue = statMap.get(manaIndex);
        if (manaValue == null) return;

        float current = manaValue.get();
        float max = manaValue.getMax();

        if (current < max) {
            float regenAmount = max * 0.000185f;
            if (regenAmount < 1.0f) regenAmount = 1.0f;
            float newValue = Math.min(max, current + regenAmount);
            statMap.setStatValue(manaIndex, newValue);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}