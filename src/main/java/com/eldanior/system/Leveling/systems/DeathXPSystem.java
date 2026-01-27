package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.components.PlayerLevelData;
import com.eldanior.system.Leveling.utils.NotificationHelper;
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
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        try {
            Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
            if (!entityRef.isValid()) return;

            UUIDComponent uuidComp = store.getComponent(entityRef, UUIDComponent.getComponentType());
            if (uuidComp == null) return;
            UUID victimUUID = uuidComp.getUuid();

            if (processedDeaths.containsKey(victimUUID)) return;
            processedDeaths.put(victimUUID, true);

            UUID killerUUID = EldaniorSystem.get().getLastAttackers().remove(victimUUID);
            if (killerUUID != null) {
                giveXP(killerUUID, victimUUID, entityRef, store, commandBuffer);
            }

        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("ERREUR CRITIQUE dans DeathXPSystem");
        }
    }

    private void giveXP(UUID killerUUID, UUID victimUUID, Ref<EntityStore> victimRef,
                        Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
        try {
            PlayerRef killerRef = Universe.get().getPlayer(killerUUID);
            if (killerRef == null) return;

            var killerEntityRef = killerRef.getReference();
            if (killerEntityRef == null) return;

            Store<EntityStore> killerStore = killerEntityRef.getStore();
            ComponentType<EntityStore, PlayerLevelData> lvlType = EldaniorSystem.get().getPlayerLevelDataType();

            PlayerLevelData killerDataRead = killerStore.getComponent(killerEntityRef, lvlType);
            int killerLevel = (killerDataRead != null) ? killerDataRead.getLevel() : 1;

            int xpAmount = 0;
            String victimName = "Inconnu";
            boolean isPvP = false;

            // Détection Joueur vs NPC
            if (store.getComponent(victimRef, Player.getComponentType()) != null) {
                isPvP = true;
                PlayerRef victimRefObj = Universe.get().getPlayer(victimUUID);
                victimName = (victimRefObj != null) ? victimRefObj.getUsername() : "Joueur";

                PlayerLevelData victimData = store.getComponent(victimRef, lvlType);
                int victimLevel = (victimData != null) ? victimData.getLevel() : 1;
                xpAmount = calculatePvPXP(killerLevel, victimLevel);
            } else {
                NPCEntity npc = store.getComponent(victimRef, Objects.requireNonNull(NPCEntity.getComponentType()));
                if (npc != null) {
                    String typeId = npc.getNPCTypeId().toLowerCase();
                    victimName = typeId.replace("_", " ");
                    xpAmount = calculatePvEXP(typeId);
                }
            }

            if (xpAmount <= 0) return;

            // Attribution XP
            PlayerLevelData dataToWrite = (killerDataRead != null)
                    ? (PlayerLevelData) killerDataRead.clone()
                    : new PlayerLevelData();

            assert dataToWrite != null;
            int oldLvl = dataToWrite.getLevel();
            dataToWrite.addExperience(xpAmount);

            commandBuffer.putComponent(killerEntityRef, lvlType, dataToWrite);

            // Feedback
            String msgContent = isPvP
                    ? "<color:gold>⚔ PvP : +" + xpAmount + " XP</color> <color:gray>(vs " + victimName + ")</color>"
                    : "<color:green>+" + xpAmount + " XP</color> <color:gray>(" + victimName + ")</color>";

            NotificationHelper.sendNotification(killerRef, msgContent, NotificationStyle.Success);

            if (dataToWrite.getLevel() > oldLvl) {
                NotificationHelper.showLevelUpTitle(killerRef, dataToWrite.getLevel());
            }

        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("ERREUR lors du don d'XP");
        }
    }

    private int calculatePvEXP(String typeId) {
        if (typeId.contains("boss")) return 500;
        if (typeId.contains("void") || typeId.contains("dungeon")) return 25;
        if (typeId.contains("skeleton") || typeId.contains("zombie")) return 15;
        if (typeId.contains("cow") || typeId.contains("pig") || typeId.contains("sheep")) return 2;
        return 10;
    }

    private int calculatePvPXP(int killerLvl, int victimLvl) {
        double ratio = (double) Math.max(1, victimLvl) / (double) Math.max(1, killerLvl);
        return (int) Math.min(2000, Math.max(1, 100 * ratio));
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return DeathComponent.getComponentType();
    }
}