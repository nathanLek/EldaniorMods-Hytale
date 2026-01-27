package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.components.PlayerLevelData;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.protocol.MovementSettings;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.UUID;

public class SpeedSystem extends EntityTickingSystem<EntityStore> {

    private float timer = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        timer += dt;
        if (timer < 1.0f) return;
        if (index == 0) timer = 0;

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        PlayerLevelData data = store.getComponent(entityRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null) return;

        MovementManager manager = store.getComponent(entityRef, MovementManager.getComponentType());
        if (manager == null) return;

        MovementSettings settings = manager.getSettings();
        if (settings == null) return;

        float agilityBonusSpeed = data.getAgility() * 0.00033f;
        float newSprintMultiplier = 1.5f * (1.0f + agilityBonusSpeed);
        float agilityBonusJump = data.getAgility() * 0.002f;
        float newJumpForce = 11.8f + agilityBonusJump;
        boolean changed = false;

        if (Math.abs(settings.forwardSprintSpeedMultiplier - newSprintMultiplier) > 0.001f) {
            settings.forwardSprintSpeedMultiplier = newSprintMultiplier;
            settings.strafeRunSpeedMultiplier = newSprintMultiplier;
            changed = true;
        }

        if (Math.abs(settings.forwardWalkSpeedMultiplier - 1.0f) > 0.001f) {
            settings.forwardWalkSpeedMultiplier = 1.0f;
            changed = true;
        }

        if (Math.abs(settings.jumpForce - newJumpForce) > 0.01f) {
            settings.jumpForce = newJumpForce;
            changed = true;
        }

        if (changed) {
            commandBuffer.putComponent(entityRef, MovementManager.getComponentType(), manager);
            syncNetwork(store, entityRef, manager);
        }
    }

    private void syncNetwork(Store<EntityStore> store, Ref<EntityStore> entityRef, MovementManager manager) {
        try {
            UUID playerUuid = null;
            Object playerComp = store.getComponent(entityRef, Player.getComponentType());
            if (playerComp != null) {
                for (Method m : playerComp.getClass().getMethods()) {
                    if (m.getReturnType().equals(UUID.class)) {
                        playerUuid = (UUID) m.invoke(playerComp);
                        if (playerUuid != null) break;
                    }
                }
            }

            if (playerUuid != null) {
                PlayerRef playerRef = Universe.get().getPlayer(playerUuid);
                if (playerRef != null) {
                    Method getPacketHandlerMethod = playerRef.getClass().getMethod("getPacketHandler");
                    Object packetHandler = getPacketHandlerMethod.invoke(playerRef);
                    for (Method updateMethod : manager.getClass().getMethods()) {
                        if (updateMethod.getName().equals("update") && updateMethod.getParameterCount() == 1) {
                            updateMethod.invoke(manager, packetHandler);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // En prod, on peut ignorer les erreurs silencieuses de sync
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}