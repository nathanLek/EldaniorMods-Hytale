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
                // On passe le victimUUID pour récupérer le nom proprement plus tard
                giveXP(killerUUID, victimUUID, entityRef, store, commandBuffer);
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("ERREUR CRITIQUE dans DeathXPSystem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Ajout de victimUUID dans les arguments pour récupérer le nom facilement
    private void giveXP(UUID killerUUID, UUID victimUUID, Ref<EntityStore> victimRef, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
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

            // --- 1. DÉTECTION ET CALCUL XP ---

            // A. PvP
            if (store.getComponent(victimRef, Player.getComponentType()) != null) {
                isPvP = true;
                victimName = "Joueur";

                // CORRECTION DU NOM : On utilise l'UUID et l'Univers (plus sûr)
                PlayerRef victimRefObj = Universe.get().getPlayer(victimUUID);
                if (victimRefObj != null) {
                    victimName = victimRefObj.getUsername();
                }

                // Récupération niveau victime
                PlayerLevelData victimData = store.getComponent(victimRef, lvlType);
                int victimLevel = (victimData != null) ? victimData.getLevel() : 1;

                // Calcul du ratio
                xpAmount = calculatePvPXP(killerLevel, victimLevel);
            }
            // B. PvE
            else {
                NPCEntity npc = store.getComponent(victimRef, Objects.requireNonNull(NPCEntity.getComponentType()));
                if (npc != null) {
                    String typeId = npc.getNPCTypeId().toLowerCase();
                    victimName = typeId.replace("_", " ");

                    if (typeId.contains("boss")) xpAmount = 500;
                    else if (typeId.contains("void") || typeId.contains("dungeon")) xpAmount = 25;
                    else if (typeId.contains("skeleton") || typeId.contains("zombie")) xpAmount = 15;
                    else if (typeId.contains("cow") || typeId.contains("pig") || typeId.contains("sheep")) xpAmount = 2;
                    else xpAmount = 10;
                }
            }

            if (xpAmount <= 0) return; // Sécurité

            // --- 2. ATTRIBUTION ---

            PlayerLevelData dataToWrite;
            if (killerDataRead != null) {
                dataToWrite = (PlayerLevelData) killerDataRead.clone();
            } else {
                dataToWrite = new PlayerLevelData();
                dataToWrite.setLevel(1);
            }

            assert dataToWrite != null;
            int oldLvl = dataToWrite.getLevel();
            dataToWrite.addExperience(xpAmount);

            commandBuffer.putComponent(killerEntityRef, lvlType, dataToWrite);

            // --- 3. FEEDBACK ---
            String msgContent;
            if (isPvP) {
                msgContent = "<color:gold>⚔ PvP : +" + xpAmount + " XP</color> <color:gray>(vs " + victimName + ")</color>";
            } else {
                msgContent = "<color:green>+" + xpAmount + " XP</color> <color:gray>(" + victimName + ")</color>";
            }

            NotificationHelper.sendNotification(killerRef, msgContent, NotificationStyle.Success);

            if (dataToWrite.getLevel() > oldLvl) {
                NotificationHelper.showLevelUpTitle(killerRef, dataToWrite.getLevel());
            }

        } catch (Exception e) {
            LOGGER.atSevere().log("ERREUR lors du don d'XP : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Calcule l'XP selon le ratio de niveau.
     * Formule : 100 * (NiveauVictime / NiveauTueur)
     */
    private int calculatePvPXP(int killerLvl, int victimLvl) {
        int baseXP = 100;

        // Sécurité anti-crash (division par zéro)
        if (killerLvl < 1) killerLvl = 1;
        if (victimLvl < 1) victimLvl = 1;

        // Calcul du Ratio (avec des doubles pour la précision)
        double ratio = (double) victimLvl / (double) killerLvl;

        // Exemples :
        // 50 vs 50 -> ratio 1.0 -> 100 XP
        // 10 vs 20 -> ratio 2.0 -> 200 XP (x2 car victime 2x plus forte)
        // 20 vs 10 -> ratio 0.5 -> 50 XP (divisé par 2 car victime 2x plus faible)

        int finalXP = (int) (baseXP * ratio);

        // Bornes de sécurité
        if (finalXP < 1) finalXP = 1;      // Minimum 1 XP
        if (finalXP > 2000) finalXP = 2000; // Maximum 2000 XP (anti-abus extrême)

        return finalXP;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return DeathComponent.getComponentType();
    }
}