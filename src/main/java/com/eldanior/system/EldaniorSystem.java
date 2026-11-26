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
        com.eldanior.system.boards.BountyBoard.save();
        com.eldanior.system.boards.GuildBoard.save();
        com.eldanior.system.familiers.FamilierManager.shutdown();   // familiers : sauvegarde immediate
        com.eldanior.system.poche.PocheDimensionnelle.saveAll();    // poches dimensionnelles (2026-11-16)

        // Cleanup farm/mine regen
        com.eldanior.system.territory.systems.FarmRegenSystem.cleanup();

        // Annuler les timers et schedulers
        com.eldanior.system.skills.runes.RuneStats.save();   // lancers de runes
        com.eldanior.system.persistence.PersistenceManager.shutdown();
        EldaniorLogger.SCHEDULER.shutdown();
    }

    @Override
    protected void start() {
        com.eldanior.system.locks.DoorLockManager.init(this.getDataDirectory());   // portes a cle : refermer celles restees ouvertes
        com.eldanior.system.presentoir.PresentoirManager.init(this.getDataDirectory());   // apres le chargement des assets : decode des objets exposes
        com.eldanior.system.anchors.AnchorManager.init(this.getDataDirectory());
        com.eldanior.system.skills.runes.RuneStats.init(this.getDataDirectory());   // lancers de runes (stats admin, 2026-11-25)   // ancres (Cercles d'Ancrage) de la Porte des Mondes
        com.eldanior.system.familiers.FamilierManager.init(this.getDataDirectory());   // familiers (oeufs, Fenrir) : un par joueur
        com.eldanior.system.dojo.Mannequins.init(this.getDataDirectory());   // mannequins d'entrainement du dojo (socles poses)
        com.eldanior.system.classes.observateur.ObservateurManager.init(this.getDataDirectory());   // Observateur : une place par trone
        com.eldanior.system.economy.PriceBook.init(this.getDataDirectory());   // bareme des prix (eldanior/economie.json + prix_jeu.json)
        com.eldanior.system.config.NoPocketCrafting.apply();   // fabrication rapide desactivee pour tous (2026-11-25)
    }

    @Override

    protected void setup() {
        LOGGER.atInfo().log(">>> ELDANIOR SYSTEM : DÉMARRAGE DU SETUP <<<");

        // Générateur de la carte du continent (WorldGen "Eldanior", Server/World/Eldanior) : enregistré avant le
        // chargement des mondes. Au-delà de la carte, il délègue au générateur normal de Hytale.
        com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider.CODEC.register(
                com.eldanior.system.carte.EldaniorWorldGen.ID, com.eldanior.system.carte.EldaniorWorldGen.Provider.class,
                com.eldanior.system.carte.EldaniorWorldGen.Provider.CODEC);

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
            com.eldanior.system.territory.systems.KingdomBarrierSystem.init(this.getDataDirectory()); // mur des royaumes
            com.eldanior.system.territory.ParcelEconomyManager.init();
            com.eldanior.system.territory.ArenaManager.init(this.getDataDirectory());
            com.eldanior.system.shop.ShopManager.init();
            com.eldanior.system.persistence.PersistenceManager.init(this.getDataDirectory());
            com.eldanior.system.hologram.HologramManager.init(this.getDataDirectory());
            com.eldanior.system.skills.runes.spells.TerrainRestore.init(this.getDataDirectory());
            com.eldanior.system.guards.GuardManager.init(this.getDataDirectory());
            com.eldanior.system.guards.NpcPostBlocks.init(this.getDataDirectory());
            com.eldanior.system.territory.borne.Bornes.init();                       // bornes de territoire : marques, borne toujours posee
            com.eldanior.system.guards.NpcRouteBlocks.init(this.getDataDirectory());              // balises de route des PNJ
            com.eldanior.system.titles.nobility.family.FamilyRegistry.init(this.getDataDirectory());   // registres des maisons (intendant)
            com.eldanior.system.boards.BountyBoard.init(this.getDataDirectory());   // tableau de prime : registre sauvegarde, zones
            com.eldanior.system.boards.BoardQuests.init(this.getDataDirectory());      // quetes ajoutees aux tableaux
            com.eldanior.system.quest.chapitres.Primes.init(this.getDataDirectory());   // contrats de prime (quetes PRIME)
            com.eldanior.system.quest.chapitres.HouseQuests.init(this.getDataDirectory());   // quetes des maisons (intendant, 2026-11-18)
            com.eldanior.system.guards.BankLoans.init(this.getDataDirectory());     // prets de la banque : echeances, Criminel ★
            com.eldanior.system.territory.pancarte.Pancartes.init();                 // affiches : etat a vendre / a louer / occupe
            com.eldanior.system.boards.MonsterContracts.init(this.getDataDirectory());   // contrats de monstres du jour
            com.eldanior.system.boards.DungeonBosses.init(this.getDataDirectory());      // boss de donjon (2026-11-16)
            com.eldanior.system.boards.WorldBosses.init(this.getDataDirectory());        // Boss Mondial (2026-11-19)
            com.eldanior.system.territory.laws.Invasions.init(this.getDataDirectory());  // failles d'invasion (2026-11-19)
            com.eldanior.system.failles.FailleManager.init(this, this.getDataDirectory());     // failles-donjons (2026-11-23)
            com.eldanior.system.failles.Generateurs.init(this.getDataDirectory());           // generateurs de monstres (2026-11-23)
            com.eldanior.system.territory.systems.KingdomMap.init(this.getDataDirectory());  // carte du royaume entier (2026-11-19)
            com.eldanior.system.poche.PocheDimensionnelle.init(this.getDataDirectory());  // poches dimensionnelles (2026-11-16)
            com.eldanior.system.boards.GuildBoard.init(this.getDataDirectory());   // contrats de guilde, missions
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
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.skills.runes.DivineDamageSystem()); // sorts divins : briser la glace (Niflheim), ombre (Nyx), protection (Apollon)
            this.getEntityStoreRegistry().registerSystem(new DeathXPSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.guards.GuardAlertSystem()); // gardes : PvP pres d'un garde -> joueur recherche
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.guards.GuardDeathSystem()); // gardes : reapparition au poste
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.guards.GuardOrphanSystem()); // PNJ de poste orphelins (apparitions ratees)
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.guards.NpcInteractSystem()); // PNJ civils : services (commerce, quetes, discussion, recrutement, guilde)
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.guards.AdventurerLootSystem()); // aventuriers : ramassage du butin
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.skills.runes.spells.RoyalMageMagic()); // mage royal : soin, nova de givre, pluie de flammes
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.skills.runes.spells.AdventurerMagic()); // aventuriers recrutes : sorts de leur type
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.skills.runes.spells.MartialNpcs());   // gardes, chevaliers... : rang martial
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.skills.runes.spells.DoppelPoseSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.skills.runes.spells.GungnirRavens()); // Gungnir : corbeaux Hugin et Munin // Doppelganger : pose miroir apres l'IA des PNJ
            this.getEntityStoreRegistry().registerSystem(new PlayerPositionTracker());
            this.getEntityStoreRegistry().registerSystem(new MobVirtualHPSystem());
            this.getEntityStoreRegistry().registerSystem(new MobNameplateUpdateOnDamageSystem());
            this.getEntityStoreRegistry().registerSystem(new MobDamageReductionSystem());
            this.getEntityStoreRegistry().registerSystem(new MobDeathCheckSystem());
            this.getEntityStoreRegistry().registerSystem(new MobNameplateColorSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.boards.EmblemOrphanSystem());   // monstres a embleme sans contrat ni boss

            this.getEntityStoreRegistry().registerSystem(new DetectionSystem());

            this.getEntityStoreRegistry().registerSystem(new TreasureContainerMonitoringSystem(OPENED_CONTAINER_TYPE));
            this.getEntityStoreRegistry().registerSystem(new TreasureChestInteractEvent());
            this.getEntityStoreRegistry().registerSystem(new TreasureChestPlaceBlockEvent());
            // Tableaux de prime et de guilde : touche d'interaction -> fenetre ; pose et retrait reserves aux admins
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.boards.BoardInteractEvent());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.familiers.FamilierInteractEvent());   // F sur son familier
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.boards.BoardPlaceBlockEvent());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.boards.BoardBreakBlockEvent());
            // Presentoirs : exposer armes, artefacts et armures (touche d'interaction)
            com.eldanior.system.presentoir.PresentoirEntity.setType(this.getEntityStoreRegistry().registerComponent(
                    com.eldanior.system.presentoir.PresentoirEntity.class, com.eldanior.system.presentoir.PresentoirEntity::new));
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.presentoir.PresentoirEntitySystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.presentoir.PresentoirUseEvent());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.failles.FailleNpcSystem());        // failles : PNJ des pieces collees retires
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.failles.FailleBlockEvents.Place()); // faille scellee posee
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.failles.GenerateurEvents.Place()); // generateur de monstres pose
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.failles.GenerateurEvents.Use());   // F sur un generateur
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.failles.GenerateurEvents.Break()); // generateur casse
            // Livres poses (histoires, Tomes Imperiaux) : F ou clic droit dessus = lecture
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.histoires.LivreUseEvent());
            // Bibliotheque evolutive : contenant d'ouvrages, etagere vide -> bibliotheque remplie
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.bibliotheque.BibliothequeUseEvent());
            // Portes a cle : salles au tresor des familles, portes de boss
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.locks.DoorLockUseEvent());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.locks.DoorLockBreakEvent());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.guards.NpcPostEvents.Place());   // bloc de poste de PNJ
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.guards.NpcPostEvents.Use());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.guards.NpcPostEvents.Break());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.borne.BorneEvents.Place());   // borne de territoire
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.borne.BorneEvents.Use());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.borne.BorneEvents.Break());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.pancarte.PancarteEvents.Place());   // pancarte / affiche de propriete
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.pancarte.PancarteEvents.Use());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.pancarte.PancarteEvents.Break());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.guards.NpcRouteBlocks.Place());  // balise de route
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.guards.NpcRouteBlocks.Break());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.locks.FamilyKeySystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.luo.LuoPlaqueSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.luo.LuoClanSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.laws.DomainLawSystem());   // lois des domaines (2026-11-18)
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.presentoir.PresentoirBreakEvent());
            // Effets visuels d'armure (fumee noire du casque divin de l'Assassin)
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.armors.ArmorVisualSystem());
            // Bonus de tenue complete : Dieu-Dragon d'Or (vitesse, sauts illimites par echange du hakama eveille)
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.armors.ArmorSetBonusSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.skills.runes.spells.TenryumaruSystem()); // rengainage de Tenryumaru
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.classes.observateur.ObservateurLames()); // fourreaux dans le dos de l'Observateur
            // Cercles d'Ancrage : pose (limite d'ancres, nom), effacement (proprietaire), touche d'interaction (renommer)
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.anchors.AnchorPlaceEvent());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.anchors.AnchorBreakEvent());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.anchors.AnchorUseEvent());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.teleport.TeleporterEvents.GatePlace());   // Portail Continental (2026-11-19)
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.teleport.GateSystem());
            // Borne de ville (2026-11-19) : posee par le Comte, ouvre le registre de la ville
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.guards.CityBornes.Place());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.guards.CityBornes.Use());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.guards.CityBornes.Break());
            // Familiers : oeuf (pose = lien, casse = ramasser, interaction = suivre / rester), entites orphelines
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.familiers.FamilierEvents.Place());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.familiers.FamilierEvents.Break());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.familiers.FamilierEvents.Use());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.familiers.FamilierManager.Orphans());
            // Mannequins d'entrainement du dojo : socle (pose, casse), coups recus (jamais de mort, plaque, energie de signature), doublons
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.dojo.MannequinEvents.Place());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.dojo.MannequinEvents.Break());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.dojo.MannequinDamage.Filter());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.dojo.MannequinDamage.Inspect());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.dojo.MannequinDamage.Entities());
            this.getEventRegistry().registerGlobal(StartWorldEvent.class, TreasureStartWorldEventListener::onStartWorldEvent);
            // Detection interaction NPC de quetes (systeme ECS)
            this.getEntityStoreRegistry().registerSystem(
                    new com.eldanior.system.quest.interaction.NpcQuestDetectionSystem());

            // Trone du Neant : gestes du souverain (clics) et coups portes aux ombres
            this.getEventRegistry().registerGlobal(com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent.class,
                    com.eldanior.system.skills.runes.spells.NyxVeil::onMouseButton);
            // Sorts de rune du moteur commun qui ecoutent les clics (ex. Telekinesie)
            this.getEventRegistry().registerGlobal(com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent.class,
                    com.eldanior.system.skills.runes.spells.RuneSpellRuntime::onMouseButton);
            this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, event -> {
                try {
                    java.util.UUID uuid = UUIDExtractor.getUUID(event.getPlayerRef());
                    PartyManager.handleDisconnect(uuid);
                    com.eldanior.system.duel.DuelManager.handleDisconnect(uuid);
                    com.eldanior.system.trade.TradeManager.handleDisconnect(uuid);
                    com.eldanior.system.territory.systems.ParcelRangeSystem.handleDisconnect(uuid);
                    com.eldanior.system.gui.tabs.QuestTab.cleanupPlayer(uuid);
                    com.eldanior.system.gui.tabs.GuildeTab.cleanupPlayer(uuid);
                    com.eldanior.system.gui.tabs.FamilleTab.cleanupPlayer(uuid);
                    com.eldanior.system.gui.tabs.DuelTab.cleanupPlayer(uuid);
                    com.eldanior.system.gui.tabs.ShopTab.cleanupPlayer(uuid);
                    com.eldanior.system.gui.tabs.BlackMarketTab.cleanupPlayer(uuid);
                    com.eldanior.system.gui.tabs.WikiTab.cleanupPlayer(uuid);
                    com.eldanior.system.gui.tabs.EchangesTab.cleanupPlayer(uuid);
                    com.eldanior.system.gui.tabs.TerritoiresTab.cleanupPlayer(uuid);
                    com.eldanior.system.gui.tabs.ProprietesTab.cleanupPlayer(uuid);
                    com.eldanior.system.gui.tabs.AdminTab.cleanupPlayer(uuid);

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
                    com.eldanior.system.poche.PocheDimensionnelle.unload(uuid);      // poche dimensionnelle : sauvee, dechargee

                    // GuildManager (pendingInvites)
                    GuildManager.handleDisconnect(uuid);

                    // InvisibilityManager (invisiblePlayers)
                    com.eldanior.system.skills.system.InvisibilityManager.handleDisconnect(uuid);

                    // ParcelManager (playerSelections)
                    com.eldanior.system.territory.ParcelManager.handleDisconnect(uuid);

                    // DignityAuraSystem (slowedPlayers + activeEmitters)
                    DignityAuraSystem.handleDisconnect(uuid);

                    // RuneAuraSystem (runeEmitters)
                    com.eldanior.system.titles.nobility.systems.RuneAuraSystem.handleDisconnect(uuid);

                    // RuneManager (cache Rune d'Aura)
                    com.eldanior.system.skills.runes.RuneManager.handleDisconnect(uuid);
                    com.eldanior.system.luo.LuoClans.forget(uuid);
                    com.eldanior.system.anchors.AnchorManager.handleDisconnect(uuid);
                    com.eldanior.system.familiers.FamilierManager.handleDisconnect(uuid);   // le familier rentre avec son maitre
                    com.eldanior.system.skills.runes.spells.MuspelheimWrath.handleDisconnect(uuid);
                    com.eldanior.system.skills.runes.spells.TaranisSanctuary.handleDisconnect(uuid);
                    com.eldanior.system.skills.runes.spells.BahamutAdvent.handleDisconnect(uuid);
                    com.eldanior.system.skills.runes.spells.DragonAvatar.handleDisconnect(uuid);
                    com.eldanior.system.skills.runes.spells.SansVisage.handleDisconnect(uuid);
                    com.eldanior.system.skills.runes.spells.ChronosHourglass.handleDisconnect(uuid);
                    com.eldanior.system.skills.runes.spells.ChaosAbyss.handleDisconnect(uuid);
                    com.eldanior.system.skills.runes.spells.BifrostBridge.handleDisconnect(uuid);
                    com.eldanior.system.skills.runes.spells.NyxVeil.handleDisconnect(uuid);
                    com.eldanior.system.skills.runes.spells.ApolloSanctuary.handleDisconnect(uuid);
                    com.eldanior.system.skills.runes.spells.GaiaAwakening.handleDisconnect(uuid);
                    com.eldanior.system.skills.runes.spells.YggdrasilAwakening.handleDisconnect(uuid);
                    com.eldanior.system.skills.runes.spells.GilgameshBabylon.handleDisconnect(uuid);
                    com.eldanior.system.skills.runes.spells.NiflheimGate.handleDisconnect(uuid);
                    com.eldanior.system.skills.runes.spells.RuneSpellRuntime.handleDisconnect(uuid);
                    com.eldanior.system.skills.runes.spells.GungnirRavens.handleDisconnect(uuid);
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
            try {                                                               // doubles du Sens martial (vus du seul lanceur)
                this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.skills.runes.spells.PrivateVisibility());
            } catch (Exception e) {
                EldaniorLogger.error("PrivateVisibility: enregistrement", e);
            }
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.duel.DuelProtectionSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.quest.QuestHudUpdateSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.hud.XpHud.UpdateSystem()); // barre d'XP en bas a gauche
            com.eldanior.system.gui.InventoryOpenProbe.register();             // sonde : la touche Tab est-elle visible du serveur ?
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.quest.QuestBlockBreakEvent());
            this.getEntityStoreRegistry().registerSystem(new DignityAuraSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.titles.nobility.systems.RuneAuraSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.skills.system.RuneDropProtectionSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.skills.runes.RuneSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.skills.runes.spells.MuspelheimWrathSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.skills.runes.spells.DivineSpellsSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.titles.nobility.systems.DragonicFearSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.titles.systems.TitleCheckSystem());

            // Territoire / Parcelles
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.events.ParcelBreakBlockEvent());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.events.ParcelPlaceBlockEvent());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.events.ParcelDamageBlockEvent());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.events.ParcelInteractEvent());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.systems.ParcelRangeSystem());
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.systems.KingdomBarrierSystem()); // mur Barrier autour des royaumes
            this.getEntityStoreRegistry().registerSystem(new com.eldanior.system.territory.systems.FarmRegenSystem());

            // Fabrication rapide desactivee (2026-11-25) : reappliquee au demarrage de chaque monde (ordre des plugins)
            this.getEventRegistry().registerGlobal(StartWorldEvent.class, event -> com.eldanior.system.config.NoPocketCrafting.apply());

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
        this.getCommandRegistry().registerCommand(new com.eldanior.system.luo.InclinerCommand());

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