package com.eldanior.system;

import com.eldanior.system.Inventory.components.PlayerPersonalChestData;
import com.eldanior.system.Leveling.systems.*;
import com.eldanior.system.TreasureChest.components.OpenedContainerComponent;
import com.eldanior.system.TreasureChest.components.PlayerChestData; // Nouveau
import com.eldanior.system.TreasureChest.events.*;
import com.eldanior.system.TreasureChest.resources.TreasureChestConfig; // Nouveau
import com.eldanior.system.TreasureChest.resources.TreasureChestTemplate; // Nouveau
import com.eldanior.system.TreasureChest.systems.TreasureChestRangeSystem;
import com.eldanior.system.TreasureChest.systems.TreasureContainerMonitoringSystem;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.config.configs.Mobs.*;
import com.eldanior.system.config.configs.system.MasterySystem;
import com.eldanior.system.config.configs.system.MobDeathCheckSystem;
import com.eldanior.system.config.configs.system.MobNameplateColorSystem;
import com.eldanior.system.config.configs.system.MobNameplateUpdateOnDamageSystem;
import com.eldanior.system.skills.InteractionManager;
import com.eldanior.system.skills.SkillManager;
import com.eldanior.system.skills.system.DetectionSystem;
import com.eldanior.system.skills.system.FlySystem;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.ResourceType; // Nouveau
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore; // Nouveau
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EldaniorSystem extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static EldaniorSystem instance;

    // --- Types de Données ---
    private ComponentType<EntityStore, PlayerLevelData> playerLevelDataType;
    private ComponentType<EntityStore, PlayerPersonalChestData> playerPersonalChestDataType;
    private ComponentType<EntityStore, MobLevelData> mobLevelDataType;

    // Nouveaux Types pour TreasureChest
    private ComponentType<EntityStore, PlayerChestData> playerChestDataType;
    public static ResourceType<ChunkStore, TreasureChestTemplate> CHEST_TEMPLATE_TYPE;
    public static ResourceType<ChunkStore, TreasureChestConfig> CONFIG_RESOURCE_TYPE;
    public static ComponentType<EntityStore, OpenedContainerComponent> OPENED_CONTAINER_TYPE;

    private final Map<UUID, UUID> lastAttackers = new ConcurrentHashMap<>();

    public EldaniorSystem(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override

    protected void setup() {
        LOGGER.atInfo().log(">>> ELDANIOR SYSTEM : DÉMARRAGE DU SETUP <<<");

        // 1. Enregistrement des MobData
        this.mobLevelDataType = this.getEntityStoreRegistry().registerComponent(
                MobLevelData.class, "MobLevelData", MobLevelData.CODEC);

        try {
            SkillManager.init();
            ClassManager.init();
            LOGGER.atInfo().log("[OK] Managers et Registres initialisés.");
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[ERREUR] Échec init Managers/Registres");
        }

        // 2. Enregistrement des Composants Joueurs (EntityStore)
        try {
            this.playerLevelDataType = this.getEntityStoreRegistry().registerComponent(
                    PlayerLevelData.class, "PlayerLevelData", PlayerLevelData.CODEC);
            PlayerLevelData.TYPE = this.playerLevelDataType;

            this.playerPersonalChestDataType = this.getEntityStoreRegistry().registerComponent(
                    PlayerPersonalChestData.class, "PlayerPersonalChestData", PlayerPersonalChestData.CODEC);

            // ENREGISTREMENT : PlayerChestData
            this.playerChestDataType = this.getEntityStoreRegistry().registerComponent(
                    PlayerChestData.class, "PlayerChestData", PlayerChestData.CODEC);

            LOGGER.atInfo().log("[OK] Composants EntityStore enregistrés.");
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[ERREUR] Enregistrement Composants EntityStore");
        }

        // 3. Enregistrement des Ressources (ChunkStore)
        try {
            OPENED_CONTAINER_TYPE = this.getEntityStoreRegistry().registerComponent(
                    OpenedContainerComponent.class,
                    "OpenedContainerComponent",
                    OpenedContainerComponent.CODEC
            );
            // Enregistrement de TreasureChestTemplate
            CHEST_TEMPLATE_TYPE = this.getChunkStoreRegistry().registerResource(
                    TreasureChestTemplate.class,
                    "TreasureChestTemplate",
                    TreasureChestTemplate.CODEC
            );

            // Enregistrement de TreasureChestConfig
            CONFIG_RESOURCE_TYPE = this.getChunkStoreRegistry().registerResource(
                    TreasureChestConfig.class,
                    "TreasureChestConfig",
                    TreasureChestConfig.CODEC
            );

            LOGGER.atInfo().log("[OK] Ressources ChunkStore enregistrées avec succès.");
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[ERREUR] Échec de l'enregistrement des ressources");
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
            this.getEntityStoreRegistry().registerSystem(new PlayerLoginSystem());
            this.getEntityStoreRegistry().registerSystem(new CombatTrackerSystem());
            this.getEntityStoreRegistry().registerSystem(new CombatStatsSystem());
            this.getEntityStoreRegistry().registerSystem(new GlobalRegenSystem());
            this.getEntityStoreRegistry().registerSystem(new FallDamageSystem());
            this.getEntityStoreRegistry().registerSystem(new DeathXPSystem());
            this.getEntityStoreRegistry().registerSystem(new PlayerPositionTracker());
            this.getEntityStoreRegistry().registerSystem(new MobVirtualHPSystem());
            this.getEntityStoreRegistry().registerSystem(new MobNameplateUpdateOnDamageSystem());
            this.getEntityStoreRegistry().registerSystem(new MobDamageReductionSystem());
            this.getEntityStoreRegistry().registerSystem(new MobDeathCheckSystem());
            this.getEntityStoreRegistry().registerSystem(new MobNameplateColorSystem());

            this.getEntityStoreRegistry().registerSystem(new DetectionSystem());

            this.getEntityStoreRegistry().registerSystem(new TreasureContainerMonitoringSystem(OPENED_CONTAINER_TYPE));
            this.getEntityStoreRegistry().registerSystem(new TreasureChestInteractEvent());
            this.getEntityStoreRegistry().registerSystem(new TreasureChestPlaceBlockEvent());
            this.getEventRegistry().registerGlobal(StartWorldEvent.class, TreasureStartWorldEventListener::onStartWorldEvent);
            this.getEntityStoreRegistry().registerSystem(new TreasureChestBreakBlockEvent());
            this.getEntityStoreRegistry().registerSystem(new TreasureChestDamageBlockEvent());
            this.getEntityStoreRegistry().registerSystem(new TreasureChestRangeSystem());
            this.getEntityStoreRegistry().registerSystem(new CraftingRestrictionSystem());
            this.getEntityStoreRegistry().registerSystem(new MasterySystem());
            this.getEntityStoreRegistry().registerSystem(new FlySystem());

            LOGGER.atInfo().log("[OK] Systèmes ECS activés.");
        } catch (Exception e) {
            LOGGER.atSevere().log("[ERREUR] Systèmes ECS : " + e.getMessage());
        }

        // 6. Commandes
        this.getCommandRegistry().registerCommand(new ESCommand());
        this.getCommandRegistry().registerCommand(new com.eldanior.system.Inventory.commands.InventoryCommand());

        LOGGER.atInfo().log(">>> ELDANIOR SYSTEM : SETUP TERMINÉ <<<");
    }

    public static EldaniorSystem get() { return instance; }

    public ComponentType<EntityStore, PlayerLevelData> getPlayerLevelDataType() { return playerLevelDataType; }
    public ComponentType<EntityStore, PlayerPersonalChestData> getPlayerPersonalChestDataType() { return playerPersonalChestDataType; }
    public ComponentType<EntityStore, MobLevelData> getMobLevelDataType() { return mobLevelDataType; }
    public ComponentType<EntityStore, PlayerChestData> getPlayerChestDataType() { return playerChestDataType; }
    public Map<UUID, UUID> getLastAttackers() { return lastAttackers; }
}