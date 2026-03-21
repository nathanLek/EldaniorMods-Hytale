package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerLoginSystem extends EntityTickingSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final Set<UUID> initializedPlayers = new HashSet<>();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Ref<EntityStore> playerRef = archetypeChunk.getReferenceTo(index);
        if (!playerRef.isValid()) return;

        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) return;

        UUID uuid = getPlayerUUID(store, playerRef);
        if (uuid == null) return;

        if (initializedPlayers.contains(uuid)) return;

        EntityStatMap statMap = store.getComponent(playerRef,
                EntityStatsModule.get().getEntityStatMapComponentType());

        if (statMap == null) return;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(playerRef, type);

        if (data == null) {
            data = new PlayerLevelData();
            commandBuffer.putComponent(playerRef, type, data);
            LOGGER.atInfo().log("[PlayerLogin] Nouveau joueur initialisé : " + uuid);
        }

        final PlayerLevelData finalData = data;

        commandBuffer.run(deferredStore -> {
            LOGGER.atInfo().log("[PlayerLogin] Mana avant update : " +
                    statMap.get(DefaultEntityStatTypes.getMana()).get() +
                    " / " + statMap.get(DefaultEntityStatTypes.getMana()).getMax());

            StatCalculator.updatePlayerStats(playerRef, deferredStore, finalData);
            LOGGER.atInfo().log("[PlayerLogin] Mana après update : " +
                    statMap.get(DefaultEntityStatTypes.getMana()).get() +
                    " / " + statMap.get(DefaultEntityStatTypes.getMana()).getMax());
            LOGGER.atInfo().log("[PlayerLogin] Stats appliquées pour : " + uuid + " Lv." + finalData.getLevel());
        });

        initializedPlayers.add(uuid);
    }

    private UUID getPlayerUUID(Store<EntityStore> store, Ref<EntityStore> playerRef) {
        try {
            Player player = store.getComponent(playerRef, Player.getComponentType());
            if (player == null) return null;

            for (java.lang.reflect.Method m : player.getClass().getMethods()) {
                if (m.getReturnType().equals(UUID.class) && m.getParameterCount() == 0) {
                    return (UUID) m.invoke(player);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public void invalidate(UUID uuid) {
        initializedPlayers.remove(uuid);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}