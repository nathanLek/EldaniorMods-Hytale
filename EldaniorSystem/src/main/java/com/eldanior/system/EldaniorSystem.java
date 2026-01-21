package com.eldanior.system;

import com.eldanior.system.commands.ESCommand;
import com.eldanior.system.components.PlayerLevelData;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.ComponentType;

public class EldaniorSystem extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static EldaniorSystem instance;
    private ComponentType<EntityStore, PlayerLevelData> playerLevelDataType;

    public EldaniorSystem(JavaPluginInit init) {
        super(init);
        instance = this;
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
    }

    public static EldaniorSystem get() { return instance; }

    public ComponentType<EntityStore, PlayerLevelData> getPlayerLevelDataType() {
        return playerLevelDataType;
    }
}