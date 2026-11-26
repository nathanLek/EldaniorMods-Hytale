package com.eldanior.system.titles.nobility.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Entretien lie a la dignite.
 *
 * L'ancienne "Aura de Dignite" passive (auto-debloquee + bonus permanents + ralentissement)
 * a ete REMPLACEE par la Rune d'Aura (toggle canalise) — voir {@link RuneAuraSystem}.
 *
 * Ce systeme ne garde que :
 *  - le plancher de dignite de la classe Dragon (min 1000),
 *  (les anciens passifs DIGNITY_AURA_* sont retires des sauvegardes par PlayerLevelData.migratePassives, 2026-11-24)
 *    (pour eviter tout doublon avec la rune).
 */
public class DignityAuraSystem extends EntityTickingSystem<EntityStore> {

    private float updateTimer = 0;
    private boolean shouldUpdate = false;


    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        if (index == 0) {
            updateTimer += dt;
            if (updateTimer >= 1.0f) {
                shouldUpdate = true;
                updateTimer = 0;
            } else {
                shouldUpdate = false;
            }
        }
        if (!shouldUpdate) return;

        Ref<EntityStore> playerRef = chunk.getReferenceTo(index);
        if (!playerRef.isValid()) return;

        PlayerLevelData selfData = store.getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (selfData == null) return;

        // Plancher de dignite pour la classe Dragon
        if ("dragon".equalsIgnoreCase(selfData.getPlayerClassId()) && selfData.getDignity() < 1000) {
            selfData.setDignity(1000);
        }

    }

    /** Conserve pour compatibilite (appele a la deconnexion). */
    public static void handleDisconnect(UUID uuid) {
        // Plus d'etat par joueur a nettoyer ici (migre vers RuneAuraSystem).
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
