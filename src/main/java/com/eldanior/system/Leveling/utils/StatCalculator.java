package com.eldanior.system.Leveling.utils;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class StatCalculator {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final AtomicBoolean regenMethodsLogged = new AtomicBoolean(false);

    public static void updatePlayerStats(Ref<EntityStore> playerRef, Store<EntityStore> store, PlayerLevelData data) {
        if (data == null) return;

        EntityStatMap statMap = store.getComponent(playerRef, EntityStatsModule.get().getEntityStatMapComponentType());

        if (statMap == null) {
            LOGGER.atWarning().log("[StatCalculator] statMap null pour ce joueur.");
            return;
        }

        ClassModel model = ClassManager.get(data.getPlayerClassId());

        boolean statMapChanged = false;
        for (StatConfig config : StatConfig.values()) {
            boolean changed = applyStat(statMap, store, playerRef, config, data, model);
            if (changed) statMapChanged = true;
        }

        if (statMapChanged) {
            syncStatMap(store, playerRef, statMap);
        }
    }

    private static boolean applyStat(EntityStatMap statMap, Store<EntityStore> store, Ref<EntityStore> playerRef,
                                     StatConfig config, PlayerLevelData data, ClassModel model) {

        if (config.getType() == StatConfig.StatType.ATTRIBUTE) {

            EntityStatValue statValue = statMap.get(config.getStatId());
            if (statValue == null) return false;

            float oldMax  = statValue.getMax();
            float currentVal = statValue.get();

            float bonus = config.getFinalValue(data, model) - config.getBaseValue();

            statMap.removeModifier(config.getStatId(), config.getModifierKey());

            if (bonus > 0) {
                statMap.putModifier(config.getStatId(), config.getModifierKey(),
                        new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, bonus));
            }

            float newMax = statValue.getMax();
            float restoredValue = (newMax > oldMax)
                    ? Math.min(newMax, currentVal + (newMax - oldMax))
                    : currentVal;

            if (statValue.get() != restoredValue) {
                statMap.setStatValue(config.getStatId(), restoredValue);
            }

            return true;

        } else if (config.getType() == StatConfig.StatType.MOVEMENT_SPEED
                || config.getType() == StatConfig.StatType.MOVEMENT_JUMP) {

            MovementManager manager = store.getComponent(playerRef, MovementManager.getComponentType());
            if (manager == null) return false;
            MovementSettings settings = manager.getSettings();
            if (settings == null) return false;

            boolean changed = false;

            if (config.getType() == StatConfig.StatType.MOVEMENT_SPEED) {
                float newSpeed = config.getFinalValue(data, model);

                if (Math.abs(settings.forwardSprintSpeedMultiplier - newSpeed) > 0.001f) {
                    settings.forwardSprintSpeedMultiplier = newSpeed;
                    settings.strafeRunSpeedMultiplier     = newSpeed;
                    changed = true;
                }
            } else {
                float newJump = config.getFinalValue(data, model);

                if (Math.abs(settings.jumpForce - newJump) > 0.01f) {
                    settings.jumpForce = newJump;
                    changed = true;
                }
            }

            if (changed) {
                store.putComponent(playerRef, MovementManager.getComponentType(), manager);
                syncMovement(store, playerRef, manager);
            }
        }

        return false;
    }

    private static void syncStatMap(Store<EntityStore> store, Ref<EntityStore> entityRef, EntityStatMap statMap) {
        try {
            UUID playerUuid = getUUID(store, entityRef);
            if (playerUuid == null) return;

            PlayerRef playerRef = Universe.get().getPlayer(playerUuid);
            if (playerRef == null) return;

            for (Method m : statMap.getClass().getMethods()) {
                if ((m.getName().equals("sync") || m.getName().equals("sendUpdate")
                        || m.getName().equals("update") || m.getName().equals("markDirty"))
                        && m.getParameterCount() == 0) {
                    m.invoke(statMap);
                    return;
                }
            }

            store.putComponent(entityRef, EntityStatsModule.get().getEntityStatMapComponentType(), statMap);

        } catch (Exception e) {
            LOGGER.atWarning().log("[StatCalculator] syncStatMap échoué : " + e.getMessage());
        }
    }

    private static void syncMovement(Store<EntityStore> store, Ref<EntityStore> entityRef, MovementManager manager) {
        try {
            UUID playerUuid = getUUID(store, entityRef);
            if (playerUuid == null) return;

            PlayerRef playerRef = Universe.get().getPlayer(playerUuid);
            if (playerRef == null) return;

            Method getPacketHandlerMethod = playerRef.getClass().getMethod("getPacketHandler");
            Object packetHandler = getPacketHandlerMethod.invoke(playerRef);
            for (Method updateMethod : manager.getClass().getMethods()) {
                if (updateMethod.getName().equals("update") && updateMethod.getParameterCount() == 1) {
                    updateMethod.invoke(manager, packetHandler);
                    break;
                }
            }
        } catch (Exception e) { EldaniorLogger.error("StatCalculator", e); }
    }

    private static UUID getUUID(Store<EntityStore> store, Ref<EntityStore> entityRef) {
        try {
            Object playerComp = store.getComponent(entityRef, Player.getComponentType());
            if (playerComp == null) return null;
            for (Method m : playerComp.getClass().getMethods()) {
                if (m.getReturnType().equals(UUID.class) && m.getParameterCount() == 0) {
                    UUID uuid = (UUID) m.invoke(playerComp);
                    if (uuid != null) return uuid;
                }
            }
        } catch (Exception e) { EldaniorLogger.error("StatCalculator", e); }
        return null;
    }
}