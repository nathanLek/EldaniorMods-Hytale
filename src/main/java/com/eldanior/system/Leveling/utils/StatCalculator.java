package com.eldanior.system.Leveling.utils;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.StatConfig;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue; // IMPORT IMPORTANT
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Method;
import java.util.UUID;

public class StatCalculator {

    public static void updatePlayerStats(Ref<EntityStore> playerRef, Store<EntityStore> store, PlayerLevelData data) {
        if (data == null) return;

        EntityStatMap statMap = store.getComponent(playerRef, EntityStatsModule.get().getEntityStatMapComponentType());
        // Note: statMap peut être null si le joueur vient de se connecter

        ClassModel model = ClassManager.get(data.getPlayerClassId());

        for (StatConfig config : StatConfig.values()) {
            applyStat(statMap, store, playerRef, config, data, model);
        }
    }

    private static void applyStat(EntityStatMap statMap, Store<EntityStore> store, Ref<EntityStore> playerRef,
                                  StatConfig config, PlayerLevelData data, ClassModel model) {

        int totalPoints = config.getTotalPoints(data, model);

        // --- CAS 1 : Statistique classique (Vie, Mana) ---
        if (config.getType() == StatConfig.StatType.ATTRIBUTE && statMap != null) {

            // 1. On récupère la valeur AVANT modification
            EntityStatValue statValue = statMap.get(config.getStatId());
            if (statValue == null) return;

            float oldMax = statValue.getMax();
            float currentVal = statValue.get();

            // 2. Calcul et Application du Bonus
            float bonus = totalPoints * config.getRatio(); // (Note: J'ai retiré ton ratio 0.16 en dur pour utiliser config)

            statMap.removeModifier(config.getStatId(), config.getModifierKey());

            if (bonus > 0) {
                statMap.putModifier(config.getStatId(), config.getModifierKey(),
                        new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, bonus));
            }

            // 3. AUTO-REMPLISSAGE (Le fix que tu as demandé)
            // Hytale recalcule le Max immédiatement après le putModifier.
            float newMax = statValue.getMax();

            // Si le max a augmenté (ex: on a gagné 10 PV max), on ajoute 10 PV à la vie actuelle.
            if (newMax > oldMax) {
                float difference = newMax - oldMax;
                float newValue = currentVal + difference;

                // On s'assure de ne pas dépasser le nouveau max (sécurité)
                statMap.setStatValue(config.getStatId(), Math.min(newMax, newValue));
            }
        }

        // --- CAS 2 : Mouvement (Vitesse, Saut) ---
        else if (config.getType() != StatConfig.StatType.ATTRIBUTE) {
            MovementManager manager = store.getComponent(playerRef, MovementManager.getComponentType());
            if (manager == null) return;
            MovementSettings settings = manager.getSettings();
            if (settings == null) return;

            boolean changed = false;

            if (config.getType() == StatConfig.StatType.MOVEMENT_SPEED) {
                float newSpeed = config.getBaseValue() * (1.0f + (totalPoints * config.getRatio()));
                if (Math.abs(settings.forwardSprintSpeedMultiplier - newSpeed) > 0.001f) {
                    settings.forwardSprintSpeedMultiplier = newSpeed;
                    settings.strafeRunSpeedMultiplier = newSpeed;
                    settings.forwardWalkSpeedMultiplier = 1.0f;
                    changed = true;
                }
            } else if (config.getType() == StatConfig.StatType.MOVEMENT_JUMP) {
                float newJump = config.getBaseValue() + (totalPoints * config.getRatio());
                if (Math.abs(settings.jumpForce - newJump) > 0.01f) {
                    settings.jumpForce = newJump;
                    changed = true;
                }
            }

            if (changed) {
                store.putComponent(playerRef, MovementManager.getComponentType(), manager);
                syncNetwork(store, playerRef, manager);
            }
        }
    }

    private static void syncNetwork(Store<EntityStore> store, Ref<EntityStore> entityRef, MovementManager manager) {
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
        } catch (Exception ignored) {}
    }
}