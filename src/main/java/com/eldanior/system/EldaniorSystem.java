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
import com.eldanior.system.titles.TitleManager;
import com.eldanior.system.guild.GuildManager;
import com.eldanior.system.party.PartyHudUpdateSystem;
import com.eldanior.system.party.PartyManager;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.eldanior.system.titles.church.ChurchManager;
import com.eldanior.system.titles.nobility.NobilityManager;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.PlayerNameplateSystem;
import com.eldanior.system.titles.nobility.systems.DignityAuraSystem;
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
import com.eldanior.system.skills.system.MovementTrackingSystem;
import com.eldanior.system.skills.system.MorphFlightSystem;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
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
    protected void shutdown() {
        LOGGER.atInfo().log(">>> ELDANIOR SYSTEM : SAUVEGARDE AVANT ARRET <<<");

        // Annuler les sessions actives (rendre les items)
        com.eldanior.system.trade.TradeManager.cancelAllTrades();
        com.eldanior.system.duel.DuelManager.cancelAllDuels();

        // Forcer la serialisation des quetes et cooldowns de tous les joueurs connectes
        com.eldanior.system.quest.QuestManager.saveAllToFile();

        // Sauvegarder toutes les donnees
        com.eldanior.system.persistence.PersistenceManager.saveAll();
        com.eldanior.system.territory.ParcelManager.saveAll();
        com.eldanior.system.territory.ArenaManager.saveAll();

        // Cleanup farm/mine regen
        com.eldanior.system.territory.systems.FarmRegenSystem.cleanup();

        // Annuler les timers et schedulers
        com.eldanior.system.persistence.PersistenceManager.shutdown();
        EldaniorLogger.SCHEDULER.shutdown();
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
            com.eldanior.system.skills.interaction.StatsItemRegistry.init();
            TitleManager.init();
            NobilityManager.init();
            FamilyManager.init();
            ChurchManager.init();
            GuildManager.init();
            PartyManager.init();
            com.eldanior.system.classement.ClassementManager.init();
            com.eldanior.system.duel.DuelManager.init();
            com.eldanior.system.quest.QuestManager.init();
            com.eldanior.system.quest.QuestManager.initDataDir(this.getDataDirectory());

            com.eldanior.system.trade.TradeManager.init();
            com.eldanior.system.territory.ParcelManager.init(this.getDataDirectory());
            com.eldanior.system.territory.ParcelEconomyManager.init();
            com.eldanior.system.territory.ArenaManager.init(this.getDataDirectory());
            com.eldanior.system.shop.ShopManager.init();
            com.eldanior.system.persistence.PersistenceManager.init(this.getDataDirectory());
            com.eldanior.system.hologram.HologramManager.init(this.getDataDirectory());
            LOGGER.atInfo().log("[OK] Managers et Registres initialisés.");
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[ERREUR] Échec init Managers/Registres");
        }

        // Validation des assets d'effets visuels (ELD-11)
        try {
            com.eldanior.system.config.Effects.SkillEffectConfig.validateAtStartup();
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[ERREUR] Validation SkillEffectConfig");
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
            // Detection interaction NPC de quetes (systeme ECS)
            this.getEntityStoreRegistry().registerSystem(
                    new com.eldanior.system.quest.interaction.NpcQuestDetectionSystem());

            this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, event -> {
                try {
                    java.util.UUID uuid = UUIDExtractor.getUUID(event.getPlayerRef());
                    PartyManager.handleDisconnect(uuid);
                    com.eldanior.system.duel.DuelManager.handleDisconnect(uuid);
                    com.eldanior.system.trade.TradeManager.handleDisconnect(uuid);
                    com.eldanior.system.territory.systems.ParcelRangeSystem.handleDisconnect(uuid);
                    com.eldanior.system.gui.tabs.QuestTab.cleanupPlayer(uuid);

                    // Memory leak cleanup — static maps
                    com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_POSITIONS.remove(uuid);
                    com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_LEVELS.remove(uuid);
                    com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_DIGNITY.remove(uuid);
                    com.eldanior.system.config.RateLimiter.cleanup(uuid);

                    // Quest data (playerQuests + cooldowns)
                    com.eldanior.system.quest.QuestManager.handleDisconnect(uuid);

                    // MovementTrackingSystem (10 maps: lastY, wasRising, jumpCounters, etc.)
                    var movementSystem = com.eldanior.system.skills.system.MovementTrackingSystem.getInstance();
                    if (movementSystem != null) movementSystem.removePlayer(uuid);

                    // PlayerLoginSystem (initializedPlayers — permet re-init au reconnect)
                    var loginSystem = PlayerLoginSystem.getInstance();
                    if (loginSystem != null) loginSystem.invalidate(uuid);

                    // GuildManager (pendingInvites)
                    GuildManager.handleDisconnect(uuid);

                    // InvisibilityManager (invisiblePlayers)
                    com.eldanior.system.skills.system.InvisibilityManager.handleDisconnect(uuid);

                    // ParcelManager (playerSelections)
                    com.eldanior.system.territory.ParcelManager.handleDisconnect(uuid);

                    // DignityAuraSystem (slowedPlayers + activeEmitters)
                    DignityAuraSystem.handleDisconnect(uuid);
                    // DivineAuraSystem (previousHP tracking)
                    com.eldanior.system.skills.system.DivineAuraSystem.handleDisconnect(uuid);
                } catch (Exception e) { EldaniorLogger.error("EldaniorSystem", e); }
            });
            this.getEntityStoreRegistry().registerSystem(new TreasureChestBreakBlockEvent());
            this.getEntityStoreRegistry().registerSystem(new TreasureChestDamageBlockEvent());
            this.getEntityStoreRegistry().registerSystem(new TreasureChestRangeSystem());
            this.getEntityStoreRegistry().registerSystem(new CraftingRestrictionSystem());
            this.getEntityStoreRegistry().registerEntityEventType(
                    com.hypixel.hytale.server.core.event.events.ecs.CraftRecipeEvent.Post.class);
            this.getEntityStoreRegistry().registerSystem(new CraftingProgressionSystem());
            this.getEntityStoreRegistry().registerSystem(new MasterySystem());
            this.getEntityStoreRegistry().registerSystem(new FlySystem());
            this.getEntityStoreRegistry().registerSystem(new MovementTrackingSystem());
            this.getEntityStoreRegistry().registerSystem(new MorphFlightSystem());
            this.getEntityStoreRegistry().registerSystem(new PlayerNameplateSystem());
            this.getEntityStoreRegistry().registerSystem(new PartyHudUpdateSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.duel.DuelProtectionSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.quest.QuestHudUpdateSystem());
            this.getEntityStoreRegistry().registerSystem(new DignityAuraSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.skills.system.DivineAuraSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.titles.systems.TitleCheckSystem());

            // Territoire / Parcelles
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.events.ParcelBreakBlockEvent());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.events.ParcelPlaceBlockEvent());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.events.ParcelInteractEvent());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.systems.ParcelRangeSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.systems.FarmRegenSystem());

            // Carte du monde : zones colorees
            this.getEventRegistry().registerGlobal(StartWorldEvent.class,
                    com.eldanior.system.territory.systems.ParcelMapSystem::onWorldStart);

            // Fermes + Donjons : init snapshots et timers au demarrage du monde
            this.getEventRegistry().registerGlobal(StartWorldEvent.class, event -> {
                if (event.getWorld() != null) {
                    // Delai de 5s pour laisser les chunks se charger
                    EldaniorLogger.SCHEDULER.schedule(() -> {
                        com.eldanior.system.territory.systems.FarmRegenSystem.initFarmTimers(event.getWorld());
                        com.eldanior.system.territory.systems.FarmRegenSystem.initDungeonTimers(event.getWorld());
                    }, 5, java.util.concurrent.TimeUnit.SECONDS);
                }
            });

            LOGGER.atInfo().log("[OK] Systèmes ECS activés.");
        } catch (Exception e) {
            LOGGER.atSevere().log("[ERREUR] Systèmes ECS : " + e.getMessage());
        }

        // 6. Commandes
        this.getCommandRegistry().registerCommand(new ESCommand());

        // Spawn hologrammes (delai pour attendre que les mondes soient prets)
        new java.util.Timer("EldaniorHologramSpawn", true).schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                try {
                    // Attendre que le monde default soit disponible
                    for (int attempt = 0; attempt < 30; attempt++) {
                        var world = com.hypixel.hytale.server.core.universe.Universe.get().getWorld("default");
                        if (world != null) {
                            com.eldanior.system.hologram.HologramManager.spawnAll();
                            // Demarrer les hologrammes dynamiques (classements arene)
                            com.eldanior.system.hologram.DynamicHologramManager.startUpdateTimer();
                            return;
                        }
                        Thread.sleep(1000);
                    }
                    System.err.println("[Hologram] World 'default' jamais trouve apres 30 tentatives.");
                } catch (Exception e) {
                    System.err.println("[Hologram] Erreur spawn au demarrage: " + e.getMessage());
                }
            }
        }, 3000L);

        LOGGER.atInfo().log(">>> ELDANIOR SYSTEM : SETUP TERMINÉ <<<");
    }

    public static EldaniorSystem get() { return instance; }

    public ComponentType<EntityStore, PlayerLevelData> getPlayerLevelDataType() { return playerLevelDataType; }
    public ComponentType<EntityStore, PlayerPersonalChestData> getPlayerPersonalChestDataType() { return playerPersonalChestDataType; }
    public ComponentType<EntityStore, MobLevelData> getMobLevelDataType() { return mobLevelDataType; }
    public ComponentType<EntityStore, PlayerChestData> getPlayerChestDataType() { return playerChestDataType; }
    public Map<UUID, UUID> getLastAttackers() { return lastAttackers; }
}