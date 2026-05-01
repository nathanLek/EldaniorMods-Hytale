package com.eldanior.system.config.Player;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerPositionTracker extends EntityTickingSystem<EntityStore> {

    // Map STATIQUE partagée globalement
    public static final Map<UUID, Vector3d> PLAYER_POSITIONS = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> PLAYER_LEVELS = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> PLAYER_DIGNITY = new ConcurrentHashMap<>();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Ref<EntityStore> playerRef = chunk.getReferenceTo(index);
        if (!playerRef.isValid()) return;

        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) return;

        TransformComponent transform = store.getComponent(playerRef, TransformComponent.getComponentType());
        if (transform == null) return;

        UUID playerUUID = getPlayerUUID(player);
        if (playerUUID == null) return;

        ComponentType<EntityStore, PlayerLevelData> playerLevelType =
                EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData playerData = store.getComponent(playerRef, playerLevelType);
        int playerLevel = (playerData != null) ? playerData.getLevel() : 1;

        PLAYER_POSITIONS.put(playerUUID, transform.getPosition());
        PLAYER_LEVELS.put(playerUUID, playerLevel);
        PLAYER_DIGNITY.put(playerUUID, (playerData != null) ? playerData.getDignity() : 0);
    }

    private UUID getPlayerUUID(Player player) {
        try {
            for (java.lang.reflect.Method m : player.getClass().getMethods()) {
                if (m.getReturnType().equals(UUID.class) && m.getParameterCount() == 0) {
                    return (UUID) m.invoke(player);
                }
            }
        } catch (Exception e) { EldaniorLogger.error("PlayerPositionTracker", e); }
        return null;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}