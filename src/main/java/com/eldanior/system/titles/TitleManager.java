package com.eldanior.system.titles;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.definitions.*;
import com.eldanior.system.titles.models.TitleModel;

import java.util.*;

public class TitleManager {

    private static final Map<String, TitleModel> titles = new HashMap<>();

    public static void init() {
        // Base
        register(new NoviceTitle());

        // Squelettes (Tier 1)
        register(new SkeletonHunter());
        register(new SkeletonCrusher());
        register(new SkeletonAnnihilator());

        // Gobelins (Tier 2)
        register(new GoblinSlayer());
        register(new GoblinNemesis());
        register(new GoblinGenocide());

        // Zombies (Tier 7)
        register(new ZombieSurvivor());
        register(new ZombiePurifier());
        register(new ZombieEradicator());

        // Trorks (Tier 8)
        register(new TrorkFighter());
        register(new TrorkWarbane());
        register(new TrorkConqueror());

        // Esprits (Tier 11)
        register(new SpiritTouched());
        register(new SpiritMaster());
        register(new SpiritSovereign());

        // Scaraks (Tier 14)
        register(new ScarakExterminator());
        register(new ScarakHivebane());
        register(new ScarakQueenslayer());

        // Dragons (Tier 15)
        register(new DragonChallenger());
        register(new DragonSlayerTitle());
        register(new DragonGod());

        // Boss
        register(new WraithBanisher());
        register(new WerewolfHunter());
        register(new YetiConqueror());
        register(new ShadowVanquisher());
        register(new VoidChampion());

        // Progression (Level)
        register(new Adventurer());
        register(new Veteran());
        register(new EliteTitle());
        register(new MasterTitle());
        register(new LegendTitle());
        register(new Transcendent());
        register(new MaxPower());

        // Kills totaux
        register(new FirstBlood());
        register(new Blooded());
        register(new WarriorSoul());
        register(new DeathIncarnate());
        register(new GodOfWar());

        // Sociaux / Rang
        register(new FirstKing());
        register(new NobleBlood());
        register(new PatriarchFounder());
        register(new HolyServant());
        register(new PopeBlessed());

        // Economie
        register(new RichTitle());
        register(new Millionaire());

        // Sauriens (Tier 3)
        register(new SaurianTracker());
        register(new SaurianDominator());
        register(new SaurianExtinction());

        // Outlanders (Tier 9)
        register(new OutlanderResistant());
        register(new OutlanderScourge());
        register(new OutlanderAnnihilator());

        // Void (Tier 10)
        register(new VoidWalker());
        register(new VoidConqueror());
        register(new VoidSovereign());

        // Golems (Tier 12)
        register(new GolemBreaker());
        register(new GolemDestroyer());
        register(new GolemAncientBane());

        // Dinosaures (Tier 13)
        register(new DinoHunter());
        register(new DinoExtinctor());

        // Speciaux (admin only)
        register(new Untouchable());
        register(new Immortal());
        register(new FirstPope());

        // Combinaisons / Stats
        register(new Berserker());
        register(new Polyvalent());
        register(new Completionist());
        register(new ManaAddict());
        register(new IronWall());
        register(new LuckyStar());

        // Coffres
        register(new ChestFinder());
        register(new ChestExplorer());
        register(new ChestSeeker());
        register(new ChestHoarder());
        register(new ChestMaster());

        // Niveaux supplementaires
        register(new Newcomer());
        register(new Awakened());
        register(new Skilled());
        register(new Experienced());
        register(new Seasoned());
        register(new ChampionTitle());
        register(new Warlord());
        register(new Mythic());
        register(new Ascended());
        register(new Demigod());

        // Argent supplementaire
        register(new Trader());
        register(new Wealthy());
        register(new Billionaire());

        // Kills supplementaires
        register(new HundredKills());
        register(new ThousandKills());
        register(new Slayer());
        register(new MassDestroyer());
        register(new DeathLord());
        register(new Apocalypse());

        // Stats
        register(new StrongTitle());
        register(new AgileTitle());
        register(new VitalTitle());
        register(new BalancedTitle());
        register(new PerfectStats());
        register(new GodlyStats());
        register(new GlassCannon());
        register(new TankTitle());
        register(new Speedster());

        // Classes
        register(new TrueWarrior());
        register(new TrueMage());
        register(new TrueAssassin());
        register(new TrueArcher());
        register(new TrueMerchant());
        register(new ClassMaster());

        // Races pacifiques
        register(new FeranFriend());
        register(new SlothianAlly());
        register(new Pacifist());
        register(new FeranHunter());
        register(new SlothianBetrayer());
        register(new DarkPath());

        // Boss supplementaires
        register(new HederaSlayer());
        register(new WhaleHunter());
        register(new BossHunter());
        register(new BossMaster());

        // Variantes squelettes
        register(new FrostPurifier());
        register(new PirateScourge());

        // Multi-conditions
        register(new NobleWarrior());
        register(new HolyWarrior());
        register(new RichNoble());
        register(new ExplorerWarrior());
        register(new ShadowLord());
        register(new EternalChampion());
        register(new WorldConqueror());
        register(new ChosenOne());

        // Rangs / Status
        register(new FirstMarquis());
        register(new FirstDuc());
        register(new FirstPope());
        register(new RoyalGuard());
        register(new FamilyLegacy());
        register(new DoubleTitle());

        // Combat mixte
        register(new NightHunter());
        register(new UndeadPurger());
        register(new VoidTouched());
        register(new BeastTamer());

        // PvP Kills
        register(new PvpInitiate());
        register(new PvpFighter());
        register(new PvpVeteran());
        register(new PvpElite());
        register(new PvpGladiator());
        register(new PvpWarlord());
        register(new PvpTyrant());
        register(new PvpDemigod());

        // Kill Streaks
        register(new StreakHot());
        register(new StreakRampage());
        register(new StreakDomination());
        register(new StreakUnstoppable());
        register(new StreakGodlike());

        // KDR
        register(new KdrPositive());
        register(new KdrDominator());
        register(new KdrPerfect());

        // PvP + Rang
        register(new NobleKiller());
        register(new WarKing());
        register(new ArenaChampion());

        // PvP Kills extra
        register(new PvpBrawler());
        register(new PvpDestroyer());
        register(new PvpExecutioner());
        register(new PvpLegend());
        register(new PvpExterminator());

        // Streaks extra
        register(new StreakWarmed());
        register(new StreakMassacre());
        register(new StreakSlaughter());
        register(new StreakLegendary());
        register(new StreakImmortal());

        // Deaths
        register(new FirstDeath());
        register(new Resilient());
        register(new Stubborn());
        register(new Undying());
        register(new PhoenixRising());
        register(new EternalSufferer());

        // PvP Combos
        register(new Duelist());
        register(new Avenger());
        register(new PvpAddict());
        register(new HolyAvenger());
        register(new PvpMaster());
        register(new Predator());
        register(new FearInspiring());

        // ========== NOUVEAUX TITRES ==========

        // Progression
        register(new Level5());
        register(new Level15());
        register(new Level25());
        register(new Level30());
        register(new Level40());
        register(new Level60());
        register(new Level75());
        register(new Level125());
        register(new Level150());
        register(new Level200());
        register(new Level250());
        register(new Level300());
        register(new Level400());
        register(new Level500());
        register(new Level750());
        // Economie
        register(new Prosperous());
        register(new Bourgeois());
        register(new Fortunate());
        register(new Magnate());
        register(new Tycoon());
        register(new Croesus());
        register(new GoldenEmperor());
        // Dignite
        register(new DignityAwakening());
        register(new DignityRising());
        register(new DignityImposing());
        register(new DignityAbsolute());
        register(new DignityRoyal());
        register(new DignitySupreme());
        register(new DignityDraconic());
        // Foi
        register(new FaithSpark());
        register(new FaithDevout());
        register(new FaithPious());
        register(new FaithBlessed());
        register(new FaithSaint());
        register(new FaithProphet());
        register(new FaithDivine());
        register(new FaithMessiah());
        // Duel
        register(new DuelFirst());
        register(new DuelFighter());
        register(new DuelVeteran());
        register(new DuelChampion());
        register(new DuelMaster());
        register(new DuelGrandmaster());
        register(new DuelLegend());
        register(new DuelStreak3());
        register(new DuelStreak5());
        register(new DuelStreak10());
        register(new DuelStreak20());
        register(new DuelPerfect());
        // Stats
        register(new Str10());
        register(new Str25());
        register(new Str75());
        register(new Str100());
        register(new Str200());
        register(new Vit10());
        register(new Vit25());
        register(new Vit75());
        register(new Vit100());
        register(new Vit200());
        register(new Int10());
        register(new Int25());
        register(new Int75());
        register(new Int100());
        register(new Int200());
        register(new End10());
        register(new End25());
        register(new End75());
        register(new End100());
        register(new Agi10());
        register(new Agi25());
        register(new Agi75());
        register(new Agi100());
        register(new Lck10());
        register(new Lck25());
        register(new Lck75());
        register(new Lck100());
        register(new AllStats50());
        register(new AllStats100());
        register(new AllStats200());
        // Survie
        register(new Death5());
        register(new Death25());
        register(new Death75());
        register(new Death150());
        register(new Death250());
        register(new Death750());
        register(new Death2000());
        register(new Death5000());
        // Collection
        register(new Collector5());
        register(new Collector10());
        register(new Collector25());
        register(new Collector50());
        register(new Collector75());
        register(new Collector100());
        register(new Collector150());
        register(new Collector200());
        register(new Collector300());
        register(new Collector400());
        // Classe
        register(new ClassWarrior());
        register(new ClassMageC());
        register(new ClassAssassinC());
        register(new ClassArcherC());
        register(new ClassMerchantC());
        register(new ClassDragon());
        register(new ClassLevel50());
        register(new ClassLevel100());
        // Competences
        register(new Skill5());
        register(new Skill10());
        register(new Skill20());
        register(new Skill30());
        register(new Skill50());
        // Guilde
        register(new GuildMember());
        register(new GuildOfficer());
        register(new GuildLeader());
        // Famille
        register(new FamilyMember());
        // Legendaire
        register(new LegendRichWarrior());
        register(new LegendPerfectKnight());
        register(new LegendTrueHero());
        register(new LegendDarkLord());
        register(new LegendSageSupreme());
        register(new LegendUltimateWarrior());
        register(new LegendAbsolute());
        register(new LegendGodSlayer());
        register(new LegendEternal());
        register(new LegendOmega());
        // Combat extra
        register(new Spider50());
        register(new Spider500());
        register(new Spider5000());
        register(new Wolf50());
        register(new Wolf500());
        register(new Bear50());
        register(new Bear500());
        register(new Bandit50());
        register(new Bandit500());
        register(new Pirate50());
        register(new Pirate500());
        register(new Undead100());
        register(new Undead1000());
        register(new Undead10000());
        register(new TotalKills250());
        register(new TotalKills750());
        register(new TotalKills2500());
        register(new TotalKills7500());
        register(new TotalKills25000());
        register(new TotalKills75000());
        register(new TotalKills250000());
        // PvP extra
        register(new PvpFirst());
        register(new Pvp25());
        register(new Pvp75());
        register(new Pvp150());
        register(new Pvp750());
        register(new Pvp2000());
        register(new Pvp5000());
        register(new BountyHunter());
        register(new MostWanted());
        register(new PublicEnemy());
        // Exploration extra
        register(new Chest10());
        register(new Chest25());
        register(new Chest75());
        register(new Chest150());
        register(new Chest250());
        register(new Chest750());
        register(new Chest1000());
        register(new Chest2500());

        // PvE extra (mobs par type + combos)
        register(new AnimalHunter10());
        register(new AnimalHunter100());
        register(new AnimalHunter1000());
        register(new Elemental50());
        register(new Elemental500());
        register(new Elemental5000());
        register(new Fen50());
        register(new Fen500());
        register(new Fen5000());
        register(new Kweebec50());
        register(new Kweebec500());
        register(new KweebecFriend());
        register(new Feran50());
        register(new Feran500());
        register(new Feran5000());
        register(new Slothian50());
        register(new Slothian500());
        register(new Slothian5000());
        register(new Saurian50());
        register(new Saurian500());
        register(new Saurian10000());
        register(new Goblin50());
        register(new Goblin500());
        register(new Goblin5000());
        register(new Skeleton50());
        register(new Skeleton500());
        register(new Skeleton5000());
        register(new Zombie50());
        register(new Zombie500());
        register(new Zombie5000());
        register(new Trork50());
        register(new Trork500());
        register(new Trork5000());
        register(new Spirit10());
        register(new Spirit200());
        register(new Spirit1000());
        register(new Outlander50());
        register(new Outlander500());
        register(new Outlander5000());
        register(new Void10());
        register(new Void200());
        register(new Void1000());
        register(new Void10000());
        register(new Golem10());
        register(new Golem200());
        register(new Golem1000());
        register(new Golem5000());
        register(new Scarak10());
        register(new Scarak200());
        register(new Scarak1000());
        register(new Scarak10000());
        register(new Dragon50());
        register(new Dragon500());
        register(new Dragon5000());
        register(new Dragon10000());
        register(new Dino10());
        register(new Dino100());
        register(new Dino1000());
        register(new Wraith50());
        register(new Wraith100());
        register(new Werewolf50());
        register(new Werewolf100());
        register(new Yeti50());
        register(new Yeti100());
        register(new Hedera50());
        register(new Hedera100());
        register(new Aquatic50());
        register(new Aquatic500());
        register(new Aquatic5000());
        register(new Wolf5000());
        register(new Bear5000());
        register(new Bandit5000());
        register(new Pirate5000());
        register(new AllRounder());
        register(new MasterHunterAll());
        register(new GodHunter());

        // Territoire / Propriete
        register(new LandOwner());
        register(new CityOwner());
        register(new TerritoryOwner());
        register(new LandBaron());
        register(new LandMogul());
        register(new LandEmperor());
        // Territoire / Exploration
        register(new Explorer1());
        register(new Explorer5());
        register(new Explorer10());
        register(new Explorer25());
        register(new Explorer50());
        register(new Explorer75());
        register(new Explorer100());
        register(new Explorer150());
        register(new Explorer200());
        register(new Explorer500());
        register(new Dungeon1());
        register(new Dungeon3());
        register(new Dungeon5());
        register(new Dungeon10());
        register(new Dungeon20());
        register(new Dungeon30());
        register(new Dungeon50());
        register(new DungeonWarrior());
        register(new WorldExplorer());
        register(new UltimateExplorer());

        System.out.println("[Eldanior] " + titles.size() + " titres charges.");
    }

    public static void register(TitleModel model) {
        if (model != null) {
            titles.put(model.getId(), model);
        }
    }

    public static TitleModel get(String id) {
        return titles.get(id);
    }

    public static TitleModel getByDisplayName(String name) {
        for (TitleModel model : titles.values()) {
            if (model.getDisplayName().equalsIgnoreCase(name)) {
                return model;
            }
        }
        return null;
    }

    public static Collection<TitleModel> getAll() {
        return titles.values();
    }

    public static String getAvailableIds() {
        if (titles.isEmpty()) return "AUCUN (Erreur d'init)";
        return String.join(", ", titles.keySet());
    }

    /**
     * Verifie tous les titres et retourne ceux que le joueur vient de debloquer.
     */
    public static List<TitleModel> checkTitleUnlocks(PlayerLevelData data) {
        List<TitleModel> newlyUnlocked = new ArrayList<>();
        List<String> alreadyUnlocked = data.getUnlockedTitles();

        for (TitleModel title : titles.values()) {
            if (!alreadyUnlocked.contains(title.getId()) && title.checkUnlockCondition(data)) {
                newlyUnlocked.add(title);
            }
        }

        return newlyUnlocked;
    }
}