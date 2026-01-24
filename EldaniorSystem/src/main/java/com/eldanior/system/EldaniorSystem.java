package com.eldanior.system;

import com.eldanior.system.commands.ESCommand;
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.systems.CombatTrackerSystem;
import com.eldanior.system.systems.DeathXPSystem;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.ComponentType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EldaniorSystem extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static EldaniorSystem instance;
    private ComponentType<EntityStore, PlayerLevelData> playerLevelDataType;

    public EldaniorSystem(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    private final Map<UUID, UUID> lastAttackers = new ConcurrentHashMap<>();

    public Map<UUID, UUID> getLastAttackers() {
        return lastAttackers;
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log(">>> ELDANIOR SYSTEM : Initialisation... <<<");

        // 1. Composants
        try {
            this.playerLevelDataType = this.getEntityStoreRegistry().registerComponent(
                    PlayerLevelData.class,
                    "PlayerLevelData",
                    PlayerLevelData.CODEC
            );
            LOGGER.atInfo().log("- Composant PlayerLevelData enregistré avec succès !");
        } catch (Exception e) {
            LOGGER.atSevere().log("ERREUR CRITIQUE : Impossible d'enregistrer PlayerLevelData ! " + e.getMessage());
        }

        try {
            this.getCommandRegistry().registerCommand(new ESCommand());
            LOGGER.atInfo().log(">>> COMMANDE /level ENREGISTRÉE <<<");
        } catch (Exception e) {
            LOGGER.atSevere().log("ERREUR : Impossible d'enregistrer la commande ! " + e.getMessage());
        }

        try {
            // Enregistre le traqueur de coups
            this.getEntityStoreRegistry().registerSystem(new CombatTrackerSystem());

            // Enregistre le détecteur de mort
            this.getEntityStoreRegistry().registerSystem(new DeathXPSystem());

            LOGGER.atInfo().log(">>> SYSTÈMES XP ACTIVÉS (TRACKER + DEATH) <<<");
        } catch (Exception e) {
            LOGGER.atSevere().log("Erreur lors de l'enregistrement des systèmes : " + e.getMessage());
        }
    }

    public static EldaniorSystem get() { return instance; }

    public ComponentType<EntityStore, PlayerLevelData> getPlayerLevelDataType() {
        return playerLevelDataType;
    }
}