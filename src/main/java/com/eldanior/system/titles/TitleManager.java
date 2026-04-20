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