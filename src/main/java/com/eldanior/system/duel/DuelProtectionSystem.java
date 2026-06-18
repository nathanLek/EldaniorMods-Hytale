package com.eldanior.system.duel;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.territory.ArenaManager;
import com.hypixel.hytale.server.core.universe.Universe;
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
        if (index == 0) tickCounter++;
        if (tickCounter % 5 != 0) return;

        // Traiter les fins de duel en attente sur le thread serveur (thread-safe pour ECS)
        DuelManager.processPendingEndDuels();

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        Player player = store.getComponent(entityRef, Player.getComponentType());
        if (player == null) return;

        PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
        if (playerRef == null) return;

        UUID playerUUID = extractUUID(playerRef);
        if (playerUUID == null) return;

        boolean inDuel = DuelManager.isInDuel(playerUUID);
        boolean inArena = ArenaManager.isInArena(playerUUID);
        if (!inDuel && !inArena) return;

        // Verifier les HP
        EntityStatMap statMap = store.getComponent(entityRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return;

        var healthStat = statMap.get(DefaultEntityStatTypes.getHealth());
        if (healthStat == null) return;

        float currentHP = healthStat.get();
        float maxHP = healthStat.getMax();

        // Si HP <= 5% du max -> protection
        if (currentHP <= maxHP * 0.05f) {
            // Remettre a 1 HP (pas pleine vie — le heal complet se fait dans endDuel)
            statMap.setStatValue(DefaultEntityStatTypes.getHealth(), 1.0f);

            if (inDuel) {
                // Duel classique : terminer le duel
                DuelManager.scheduleEndDuel(playerUUID);
            } else if (inArena) {
                // Arene : enregistrer death + kill via lastAttackers
                String arenaId = ArenaManager.getArenaId(playerUUID);
                if (arenaId != null) {
                    String victimName = playerRef.getUsername();
                    ArenaManager.recordDeath(arenaId, victimName);

                    // Trouver le killer
                    UUID killerUUID = EldaniorSystem.get().getLastAttackers().remove(playerUUID);
                    if (killerUUID != null) {
                        PlayerRef killerRef = Universe.get().getPlayer(killerUUID);
                        if (killerRef != null) {
                            ArenaManager.recordKill(arenaId, killerRef.getUsername());
                        }
                    }
                }
            }
        }
    }

    private UUID extractUUID(PlayerRef playerRef) {
        try { return UUIDExtractor.getUUID(playerRef); } catch (Exception e) { return null; }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
