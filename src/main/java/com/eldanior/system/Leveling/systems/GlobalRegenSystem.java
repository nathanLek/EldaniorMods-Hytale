package com.eldanior.system.Leveling.systems;

import com.eldanior.system.config.configs.StatConfig;
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

public class GlobalRegenSystem extends EntityTickingSystem<EntityStore> {

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        EntityStatMap statMap = store.getComponent(entityRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        for (StatConfig config : StatConfig.values()) {
            // Filtres de base
            if (config.getType() != StatConfig.StatType.ATTRIBUTE) continue;
            if (config.getRegenRate() <= 0) continue;

            EntityStatValue statValue = statMap.get(config.getStatId());
            if (statValue == null) continue;

            float current = statValue.get();
            float max = statValue.getMax();

            // Sécurité : Pas de regen de Vie si mort
            if (config.getStatId() == DefaultEntityStatTypes.getHealth() && current <= 0) {
                continue;
            }

            // Application de la régénération
            if (current < max) {
                float amountToAdd = getAmountToAdd(dt, config, max);
                float newValue = Math.min(max, current + amountToAdd);

                statMap.setStatValue(config.getStatId(), newValue);
            }
        }
    }

    private static float getAmountToAdd(float dt, StatConfig config, float max) {
        float rate = config.getRegenRate();

        // Bonus de vitesse : +0.05% de vitesse par point de stat max.
        // Exemple : 1000 Mana = +50% de vitesse de regen.
        float bonusMultiplier = 1.0f + (max * 0.0005f);

        // Formule : (Max * TauxBase * Multiplicateur) * Temps_Ecoule
        return (max * rate * bonusMultiplier) * dt;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}