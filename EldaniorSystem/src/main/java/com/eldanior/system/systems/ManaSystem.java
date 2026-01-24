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

public class ManaSystem extends EntityTickingSystem<EntityStore> {

    private float timer = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        // On ne régénère pas à chaque tick (trop rapide), mais toutes les 1 seconde
        timer += dt;
        if (timer < 25.0f) return; // Une fois par 25 seconde
        timer = 0; // Reset timer pour tous les joueurs traités dans ce chunk (simplifié)
        // Note: Pour être parfait, le timer devrait être par joueur, mais pour un test c'est ok.

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        PlayerLevelData data = store.getComponent(entityRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null) return;

        EntityStatMap statMap = store.getComponent(entityRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        int manaIndex = DefaultEntityStatTypes.getMana();
        EntityStatValue manaValue = statMap.get(manaIndex);
        if (manaValue == null) return;

        float current = manaValue.get();
        float max = manaValue.getMax();

        if (current < max) {
            // ANCIENNE FORMULE : 1 + (Intell * 0.2)
            // NOUVELLE FORMULE : Régénération en % du total + Bonus fixe

            // Base : 1 mana/sec
            // Bonus : 0.05% du Mana Max par point d'Intelligence
            // Au lvl 999 (5000 mana) : on veut regen environ 50-80 par seconde.

            // Calcul simplifié : On regen 1.5% du Mana Max par seconde grâce à l'intelligence
            // Cela assure que la barre se remplit toujours en ~60 secondes, que tu sois lvl 1 ou 999.

            float regenRate = 1.0f + (max * 0.015f);

            float newValue = Math.min(max, current + regenRate);
            statMap.setStatValue(manaIndex, newValue);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}