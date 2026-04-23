package com.eldanior.system.duel;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Systeme qui protege les joueurs en duel de la mort.
 * Si un joueur en duel tombe a 1 HP ou moins, on le remet a 1 HP et on termine le duel.
 */
public class DuelProtectionSystem extends EntityTickingSystem<EntityStore> {

    private int tickCounter = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        // Check toutes les 5 ticks (~0.25s)
        tickCounter++;
        if (tickCounter % 5 != 0) return;

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        Player player = store.getComponent(entityRef, Player.getComponentType());
        if (player == null) return;

        PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
        if (playerRef == null) return;

        UUID playerUUID = extractUUID(playerRef);
        if (playerUUID == null) return;

        if (!DuelManager.isInDuel(playerUUID)) return;

        // Verifier les HP
        EntityStatMap statMap = store.getComponent(entityRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        var healthStat = statMap.get(DefaultEntityStatTypes.getHealth());
        if (healthStat == null) return;

        float currentHP = healthStat.get();
        float maxHP = healthStat.getMax();

        // Si HP <= 5% du max -> fin du duel, ce joueur a perdu
        if (currentHP <= maxHP * 0.05f) {
            // Remettre a 1 HP pour ne pas mourir
            statMap.setStatValue(DefaultEntityStatTypes.getHealth(), 1.0f);
            // Terminer le duel
            DuelManager.endDuel(playerUUID);
        }
    }

    private UUID extractUUID(PlayerRef playerRef) {
        try {
            Field f = PlayerRef.class.getDeclaredField("uuid");
            f.setAccessible(true);
            return (UUID) f.get(playerRef);
        } catch (Exception e) { return null; }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
