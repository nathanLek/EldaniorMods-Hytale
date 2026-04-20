package com.eldanior.system.skills.system;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.skills.skills.passives.Legendaire.Magique.Fly;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.protocol.packets.player.SetMovementStates;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FlySystem extends EntityTickingSystem<EntityStore> {

    private static final float FLY_SPEED = 6.5f;

    private final Map<UUID, Float> flightTimers = new ConcurrentHashMap<>();
    // Tracks players whose flight was enabled BY THIS SYSTEM (via the VOL skill).
    // Prevents overriding canFly when it was enabled externally (creative/admin commands).
    private final Set<UUID> flyEnabledBySystem = ConcurrentHashMap.newKeySet();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        PlayerLevelData data = archetypeChunk.getComponent(index, EldaniorSystem.get().getPlayerLevelDataType());

        if (player == null || playerRef == null || data == null) return;
        if (player.getReference() == null || !player.getReference().isValid()) return;

        boolean hasVol = data.getActivePassives().contains(PassiveSkill.VOL);

        MovementManager movementManager = store.getComponent(player.getReference(), MovementManager.getComponentType());
        MovementStatesComponent movementStates = store.getComponent(player.getReference(), MovementStatesComponent.getComponentType());

        // --- NOUVEAU : Récupération du composant natif des statistiques (Mana, Vie, etc.) ---
        ComponentType<EntityStore, EntityStatMap> statMapType = EntityStatsModule.get().getEntityStatMapComponentType();
        EntityStatMap statMap = store.getComponent(player.getReference(), statMapType);

        if (movementManager == null || movementStates == null || statMap == null) return;

        // Récupération de la valeur exacte du Mana actuel
        EntityStatValue manaStat = statMap.get(StatConfig.INTELLIGENCE.getStatId());
        if (manaStat == null) return;
        float currentMana = manaStat.get();

        MovementSettings settings = movementManager.getSettings();
        if (settings == null) return;

        boolean currentlyCanFly = settings.canFly;
        boolean isActuallyFlying = movementStates.getMovementStates().flying;
        UUID playerUUID = playerRef.getUuid();

        if (hasVol) {
            if (isActuallyFlying) {
                float timeFlying = flightTimers.getOrDefault(playerUUID, 0f) + dt;

                if (timeFlying >= 1.0f) {

                    // On vérifie le mana actuel (via le système natif)
                    if (currentMana >= Fly.MANA_COST_PER_SECOND) {

                        // On réduit le mana natif (l'UI va se rafraîchir toute seule !)
                        statMap.setStatValue(StatConfig.INTELLIGENCE.getStatId(), currentMana - Fly.MANA_COST_PER_SECOND);
                        commandBuffer.putComponent(player.getReference(), statMapType, statMap);

                        flightTimers.put(playerUUID, timeFlying - 1.0f);
                    } else {
                        if (currentlyCanFly) {
                            NotificationHelper.sendNotification(playerRef, "<color:red>Vol désactivé : Plus de mana</color>", NotificationStyle.Warning);
                            forceStopFlying(player, playerRef, movementManager, settings, commandBuffer);
                            flyEnabledBySystem.remove(playerUUID);
                        }
                        return;
                    }
                } else {
                    flightTimers.put(playerUUID, timeFlying);
                }
            } else {
                flightTimers.remove(playerUUID);
            }

            if (!currentlyCanFly && currentMana >= Fly.MANA_COST_PER_SECOND) {
                settings.canFly = true;
                settings.horizontalFlySpeed = FLY_SPEED;
                settings.verticalFlySpeed = FLY_SPEED;
                movementManager.update(playerRef.getPacketHandler());
                commandBuffer.putComponent(player.getReference(), MovementManager.getComponentType(), movementManager);

                SavedMovementStates saved = new SavedMovementStates(false);
                playerRef.getPacketHandler().writeNoCache(new SetMovementStates(saved));
                flyEnabledBySystem.add(playerUUID);
            }

        } else if (currentlyCanFly && flyEnabledBySystem.contains(playerUUID)) {
            forceStopFlying(player, playerRef, movementManager, settings, commandBuffer);
            flightTimers.remove(playerUUID);
            flyEnabledBySystem.remove(playerUUID);
        }
    }

    private void forceStopFlying(Player player, PlayerRef playerRef, MovementManager movementManager, MovementSettings settings, CommandBuffer<EntityStore> commandBuffer) {
        settings.canFly = false;
        movementManager.update(playerRef.getPacketHandler());
        assert player.getReference() != null;
        commandBuffer.putComponent(player.getReference(), MovementManager.getComponentType(), movementManager);

        SavedMovementStates saved = new SavedMovementStates(false);
        playerRef.getPacketHandler().writeNoCache(new SetMovementStates(saved));
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                Player.getComponentType(),
                PlayerRef.getComponentType(),
                EldaniorSystem.get().getPlayerLevelDataType()
        );
    }
}