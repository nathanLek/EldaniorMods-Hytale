package com.eldanior.system;

import com.eldanior.system.Inventory.components.PlayerPersonalChestData;
import com.eldanior.system.Leveling.systems.*;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.Leveling.components.PlayerLevelData;
import com.eldanior.system.skills.InteractionManager;
import com.eldanior.system.skills.SkillManager;
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
    private ComponentType<EntityStore, PlayerPersonalChestData> playerPersonalChestDataType;

    private final Map<UUID, UUID> lastAttackers = new ConcurrentHashMap<>();

    public EldaniorSystem(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log(">>> ELDANIOR SYSTEM : DÉMARRAGE DU SETUP <<<");

        // 1. Initialisation des Managers
        try {
            SkillManager.init();
            ClassManager.init();
            LOGGER.atInfo().log("[OK] Managers RPG initialisés.");
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[ERREUR] Échec init Managers");
        }

        // 2. Enregistrement PlayerLevelData (Isolé)
        try {
            LOGGER.atInfo().log("... Enregistrement PlayerLevelData ...");
            this.playerLevelDataType = this.getEntityStoreRegistry().registerComponent(
                    PlayerLevelData.class, "PlayerLevelData", PlayerLevelData.CODEC);
            PlayerLevelData.TYPE = this.playerLevelDataType;
            LOGGER.atInfo().log("[OK] PlayerLevelData enregistré !");
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[ERREUR CRITIQUE] Impossible d'enregistrer PlayerLevelData");
        }

        // 3. Enregistrement PlayerPersonalChestData (Isolé et détaillé)
        try {
            LOGGER.atInfo().log("... Enregistrement PlayerPersonalChestData ...");

            this.playerPersonalChestDataType = this.getEntityStoreRegistry().registerComponent(
                    PlayerPersonalChestData.class,
                    "PlayerPersonalChestData",
                    PlayerPersonalChestData.CODEC
            );

            LOGGER.atInfo().log("PlayerPersonalChestDataType: " + (playerPersonalChestDataType != null));
        } catch (Exception e) {
            // C'EST ICI QUE L'ERREUR VA S'AFFICHER
            LOGGER.atSevere().withCause(e).log("[ERREUR CRITIQUE] Impossible d'enregistrer PlayerPersonalChestData");
        }

        // 4. Interactions
        try {
            InteractionManager.registerInteractions(this);
            LOGGER.atInfo().log("[OK] Interactions enregistrées.");
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[ERREUR] Interactions");
        }

        // 5. Systèmes ECS
        try {
            this.getEntityStoreRegistry().registerSystem(new CombatTrackerSystem());
            this.getEntityStoreRegistry().registerSystem(new CombatStatsSystem());
            this.getEntityStoreRegistry().registerSystem(new EnduranceSystem());
            this.getEntityStoreRegistry().registerSystem(new ManaSystem());
            this.getEntityStoreRegistry().registerSystem(new HealthRegenSystem());
            this.getEntityStoreRegistry().registerSystem(new FallDamageSystem());
            this.getEntityStoreRegistry().registerSystem(new SpeedSystem());
            this.getEntityStoreRegistry().registerSystem(new DeathXPSystem());
            LOGGER.atInfo().log("[OK] Systèmes ECS activés.");
        } catch (Exception e) {
            LOGGER.atSevere().log("[ERREUR] Systèmes ECS : " + e.getMessage());
        }

        // 6. Enregistrement de la commande (à la toute fin pour être sûr)
        this.getCommandRegistry().registerCommand(new com.eldanior.system.ESCommand());
        // Assure-toi que InventoryCommand est bien enregistré quelque part, soit dans ESCommand, soit ici :
        this.getCommandRegistry().registerCommand(new com.eldanior.system.Inventory.commands.InventoryCommand());

        LOGGER.atInfo().log(">>> ELDANIOR SYSTEM : SETUP TERMINÉ <<<");
    }

    public static EldaniorSystem get() {
        return instance;
    }

    public ComponentType<EntityStore, PlayerLevelData> getPlayerLevelDataType() {
        return playerLevelDataType;
    }

    public ComponentType<EntityStore, PlayerPersonalChestData> getPlayerPersonalChestDataType() {
        return playerPersonalChestDataType;
    }

    public Map<UUID, UUID> getLastAttackers() {
        return lastAttackers;
    }
}