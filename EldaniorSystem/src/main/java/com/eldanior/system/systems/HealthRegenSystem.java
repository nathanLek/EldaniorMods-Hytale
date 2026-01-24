package com.eldanior.system.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
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

public class HealthRegenSystem extends EntityTickingSystem<EntityStore> {

    private float timer = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        // Régénération toutes les 20 secondes (classique dans les RPG)
        timer += dt;
        if (timer < 20.0f) return; // Une fois toutes les 20 secondes
        timer = 0;

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        PlayerLevelData data = store.getComponent(entityRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null) return;

        EntityStatMap statMap = store.getComponent(entityRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        int healthIndex = DefaultEntityStatTypes.getHealth();
        EntityStatValue healthValue = statMap.get(healthIndex);
        if (healthValue == null) return;

        float current = healthValue.get();
        float max = healthValue.getMax();

        if (current < max && current > 0) {
            // NOUVELLE FORMULE : 2% des PV Max toutes les 4 secondes
            // Au niveau 999 (500 PV), on récupère 10 PV par tick.

            float regenRate = 1.0f + (max * 0.02f);

            float newValue = Math.min(max, current + regenRate);
            statMap.setStatValue(healthIndex, newValue);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}