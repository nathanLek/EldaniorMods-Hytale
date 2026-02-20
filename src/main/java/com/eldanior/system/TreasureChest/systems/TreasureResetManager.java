package com.eldanior.system.TreasureChest.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.TreasureChest.components.PlayerChestData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.playerdata.PlayerStorage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class TreasureResetManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    /**
     * Lance une réinitialisation globale de tous les coffres pour chaque joueur enregistré.
     */
    public static void runGlobalReset(Store<EntityStore> store) {
        PlayerStorage storage = Universe.get().getPlayerStorage();
        Set<UUID> allPlayerUuids;

        try {
            // Récupère la liste de tous les UUIDs de joueurs ayant déjà rejoint le serveur
            allPlayerUuids = storage.getPlayers();
        } catch (IOException e) {
            LOGGER.atSevere().withCause(e).log("Impossible de récupérer la liste des joueurs pour le reset.");
            return;
        }

        LOGGER.atInfo().log("Démarrage du reset global des coffres pour " + allPlayerUuids.size() + " joueurs...");

        // Action à effectuer : vider la map lootData de chaque composant
        Consumer<PlayerChestData> resetAction = PlayerChestData::resetAllChests;

        for (UUID uuid : allPlayerUuids) {
            PlayerRef targetRef = Universe.get().getPlayer(uuid);

            // CAS 1 : Le joueur est EN LIGNE
            if (targetRef != null && targetRef.isValid()) {
                Ref<EntityStore> targetEntityRef = targetRef.getReference();
                if (targetEntityRef != null && targetEntityRef.isValid()) {
                    // Utilisation du Store global pour récupérer le composant
                    PlayerChestData component = store.getComponent(targetEntityRef, EldaniorSystem.get().getPlayerChestDataType());
                    if (component != null) {
                        resetAction.accept(component);
                    }
                }
            }
            // CAS 2 : Le joueur est HORS LIGNE
            else {
                // Chargement des données (jointure pour attendre le résultat)
                storage.load(uuid).thenAccept(holder -> {
                    // Dans Hytale, storage.load renvoie le Holder directement (ou null)
                    if (holder != null) {
                        // Accès au composant via le holder
                        PlayerChestData loot = holder.getComponent(EldaniorSystem.get().getPlayerChestDataType());
                        if (loot != null) {
                            resetAction.accept(loot);
                            // Sauvegarde impérative
                            storage.save(uuid, holder).join();
                        }
                    }
                }).exceptionally(ex -> {
                    LOGGER.atSevere().log("Erreur lors du reset hors-ligne pour " + uuid + " : " + ex.getMessage());
                    return null;
                });
            }
        }
        LOGGER.atInfo().log("Reset global terminé.");
    }
}