package com.eldanior.system.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.utils.NotificationHelper;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeathXPSystem extends EntityTickingSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Map<UUID, Boolean> processedDeaths = new ConcurrentHashMap<>();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        try {
            Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
            if (!entityRef.isValid()) return;

            UUIDComponent uuidComp = (UUIDComponent) store.getComponent(entityRef, UUIDComponent.getComponentType());
            if (uuidComp == null) return;
            UUID victimUUID = uuidComp.getUuid();

            if (processedDeaths.containsKey(victimUUID)) return;
            processedDeaths.put(victimUUID, true);

            UUID killerUUID = EldaniorSystem.get().getLastAttackers().remove(victimUUID);

            if (killerUUID != null) {
                giveXP(killerUUID, entityRef, store, commandBuffer);
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("ERREUR CRITIQUE dans DeathXPSystem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void giveXP(UUID killerUUID, Ref<EntityStore> victimRef, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
        try {
            PlayerRef playerRef = Universe.get().getPlayer(killerUUID);
            if (playerRef == null) return;

            int xpAmount = 0;
            String victimName = "Inconnu";

            // --- CORRECTION 1 : hasComponent -> getComponent != null ---
            // A. PvP
            if (store.getComponent(victimRef, Player.getComponentType()) != null) {
                xpAmount = 100;
                victimName = "Joueur";
            }
            // B. PvE
            else {
                NPCEntity npc = store.getComponent(victimRef, Objects.requireNonNull(NPCEntity.getComponentType()));
                if (npc != null) {
                    String typeId = npc.getNPCTypeId().toLowerCase();
                    victimName = typeId.replace("_", " ");

                    if (typeId.contains("boss")) {
                        xpAmount = 500;
                    } else if (typeId.contains("void") || typeId.contains("dungeon")) {
                        xpAmount = 25;
                    } else if (typeId.contains("skeleton") || typeId.contains("zombie")) {
                        xpAmount = 100;
                    } else if (typeId.contains("cow") || typeId.contains("pig") || typeId.contains("sheep")) {
                        xpAmount = 100;
                    } else {
                        xpAmount = 10;
                    }
                }
            }

            if (xpAmount == 0) return;

            var killerEntityRef = playerRef.getReference();
            if (killerEntityRef != null) {
                Store<EntityStore> killerStore = killerEntityRef.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

                PlayerLevelData existingData = killerStore.getComponent(killerEntityRef, type);

                PlayerLevelData data = (existingData != null) ? (PlayerLevelData) existingData.clone() : new PlayerLevelData();

                assert data != null;
                int oldLvl = data.getLevel();
                data.addExperience(xpAmount);

                commandBuffer.putComponent(killerEntityRef, type, data);

                // --- CORRECTION 2 : Action Bar -> Notification ---
                String msgContent = "<color:green>+" + xpAmount + " XP</color> <color:gray>(" + victimName + ")</color>";
                // On utilise NotificationHelper car PlayerRef ne supporte pas sendActionBar
                NotificationHelper.sendNotification(playerRef, msgContent, NotificationStyle.Success);

                if (data.getLevel() > oldLvl) {
                    NotificationHelper.showLevelUpTitle(playerRef, data.getLevel());
                }
            }

        } catch (Exception e) {
            LOGGER.atSevere().log("ERREUR lors du don d'XP : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return DeathComponent.getComponentType();
    }
}