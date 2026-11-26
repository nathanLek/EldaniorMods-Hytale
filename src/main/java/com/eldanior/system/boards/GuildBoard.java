package com.eldanior.system.boards;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.PersistenceUtils;
import com.eldanior.system.guild.Guild;
import com.eldanior.system.guild.GuildManager;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Donnees du tableau de guilde :
 *   - contrats de guilde : {@link #WEEKLY} contrats par semaine (tires dans CONTRACTS, les memes pour toutes les guildes) ;
 *     chaque monstre tue par un membre fait avancer ceux de sa guilde (DeathXPSystem -> onMobKill). Accompli : tresor et
 *     contribution pour la guilde, membres connectes prevenus ;
 *   - missions : {@link #MISSIONS_PER_DAY} missions individuelles par jour, tirees de facon deterministe d'apres la date
 *     (missionsFor) ; un joueur en prend au plus {@link #MAX_TAKEN}.
 * 2026-11-19 : le tableau s'ouvre en cartes facon Chapitres (quest/chapitres/GuildQuests) : contrats = « Quetes de guilde »,
 * missions = « Missions de guilde », devenues de vraies quetes (PlayerQuest : avancement, HUD, rendu). Le classement des
 * guildes est passe au tableau des classements (RankingBoardScreen).
 * Sauvegarde : eldanior_data/guild_board.properties (semaine et avancement des contrats ; les anciennes missions prises,
 * « taken.* », ne sont plus lues).
 */
public final class GuildBoard {

    private GuildBoard() {}

    public static final int WEEKLY = 3, MISSIONS_PER_DAY = 6, MAX_TAKEN = 2;

    /** Famille de monstres : mots-cles cherches dans l'identifiant du monstre (vide : tous). */
    public enum Family {
        UNDEAD("morts-vivants", "skeleton zombie wraith ghoul mummy lich"),
        TRORK("trorks", "trork"),
        GOBLIN("gobelins", "goblin"),
        OUTLANDER("outlanders", "outlander"),
        SCARAK("scaraks", "scarak"),
        SPIDER("araignées", "spider"),
        BEAST("bêtes sauvages", "wolf bear boar yeti raptor rex crocodile toad werewolf"),
        ANY("monstres", "");

        public final String label;
        private final String[] keywords;

        Family(String label, String keywords) {
            this.label = label;
            this.keywords = keywords.isEmpty() ? new String[0] : keywords.split(" ");
        }

        boolean matches(String mobTypeId) {
            if (keywords.length == 0) return true;
            for (String k : keywords) if (mobTypeId.contains(k)) return true;
            return false;
        }

        /** Cible d'un attendu KILL des quetes en cartes (ChapterQuests.onMobKill) : « skeleton|zombie », « * » pour tous. */
        public String pattern() { return keywords.length == 0 ? "*" : String.join("|", keywords); }
    }

    /** Contrat de guilde : titre, famille, nombre a tuer, recompenses de la guilde. */
    public record Contract(String title, Family family, int count, long treasury, long contribution) {}

    private static final List<Contract> CONTRACTS = List.of(
            new Contract("Fléau des morts-vivants", Family.UNDEAD, 500, 25_000, 2_500),
            new Contract("Chasse aux trorks", Family.TRORK, 300, 20_000, 2_000),
            new Contract("Razzia gobeline", Family.GOBLIN, 300, 18_000, 1_800),
            new Contract("Purge des outlanders", Family.OUTLANDER, 200, 20_000, 2_000),
            new Contract("Nid de scaraks", Family.SCARAK, 250, 18_000, 1_800),
            new Contract("Toiles et venin", Family.SPIDER, 250, 15_000, 1_500),
            new Contract("Grande battue", Family.BEAST, 400, 15_000, 1_500),
            new Contract("Mille têtes", Family.ANY, 2_000, 40_000, 4_000));

    /** Mission individuelle offerte au tableau ; id = jour (epoch) + « _ » + rang dans le tirage du jour. */
    public record Mission(String id, String title, Family family, int count, long gold, int xp, long contribution) {}

    private static volatile String week;
    private static volatile List<Contract> weekly = List.of();
    /** Avancement des contrats de la semaine, par guilde (-1 : accompli). */
    private static final Map<String, int[]> progress = new ConcurrentHashMap<>();
    private static volatile LocalDate day;
    private static volatile List<Mission> missions = List.of();
    private static Path file;
    private static volatile boolean dirty;

    // ===== Cycle de vie =====

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("guild_board.properties");
        load();
        refresh();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> { if (dirty) save(); }, 60, 60, TimeUnit.SECONDS);
        EldaniorLogger.info("[Boards] contrats de guilde " + week + " : " + weekly.size() + ", missions du jour : " + missions.size());
    }

    /** Nouvelle semaine : nouveaux contrats (avancement remis a zero) ; nouveau jour : nouvelles missions. */
    static synchronized void refresh() {
        LocalDate now = LocalDate.now();
        String w = weekKey(now);
        if (!w.equals(week)) {
            if (week != null) progress.clear();
            week = w;
            List<Contract> pool = new ArrayList<>(CONTRACTS);
            Collections.shuffle(pool, new Random(w.hashCode()));
            weekly = List.copyOf(pool.subList(0, WEEKLY));
            dirty = true;
        }
        if (!now.equals(day)) {
            day = now;
            missions = missionsFor(now);
        }
    }

    /** Semaine d'une date : « 2026W47 » (sans tiret ni deux-points : sert dans les identifiants de quete). */
    public static String weekKey(LocalDate d) {
        return d.get(IsoFields.WEEK_BASED_YEAR) + "W" + d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    /** Semaine en cours. */
    public static String week() { refresh(); return week; }

    /** Missions d'un jour, toujours les memes pour une meme date (une mission prise un autre jour se retrouve ainsi). */
    public static List<Mission> missionsFor(LocalDate d) {
        Random rng = new Random(d.toEpochDay() * 31 + 7);
        List<Mission> list = new ArrayList<>();
        for (int i = 0; i < MISSIONS_PER_DAY; i++) {
            Family f = Family.values()[rng.nextInt(Family.values().length)];
            int count = (f == Family.ANY ? 60 : 20) + rng.nextInt(9) * 5;
            String title = f == Family.ANY ? "Nettoyage des environs" : "Chasse aux " + f.label;
            list.add(new Mission(d.toEpochDay() + "_" + i, title, f, count, count * 40L, count * 30, count));
        }
        return List.copyOf(list);
    }

    public static List<Contract> weekly() { refresh(); return weekly; }

    public static List<Mission> missions() { refresh(); return missions; }

    /** Avancement d'un contrat de la semaine pour une guilde ; -1 : accompli. */
    public static int progress(Guild g, int index) {
        int[] p = g != null ? progress.get(g.getId()) : null;
        return p != null && index < p.length ? p[index] : 0;
    }

    /** Contrat de la semaine vu par une guilde (onglet Guilde du systeme). */
    public record ContractView(String title, String objective, int progress, int count, long treasury, boolean done) {}

    public static List<ContractView> contractsOf(Guild g) {
        List<ContractView> out = new ArrayList<>();
        List<Contract> w = weekly();
        for (int i = 0; i < w.size(); i++) {
            Contract c = w.get(i);
            int p = progress(g, i);
            out.add(new ContractView(c.title(), "Tuer " + c.count() + " " + c.family().label, p < 0 ? c.count() : p, c.count(), c.treasury(), p < 0));
        }
        return out;
    }

    /** Jours avant la fin des contrats de la semaine (0 : ce soir). */
    public static long daysLeft() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), weekEnd());
    }

    /** Fin de la semaine : dimanche a minuit. */
    public static LocalDate weekEnd() {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    // ===== Kills =====

    /** Monstre tue par un joueur (DeathXPSystem) : contrats de sa guilde (ses missions avancent comme des quetes). */
    public static synchronized void onMobKill(UUID killer, String mobTypeId) {
        if (killer == null || mobTypeId == null) return;
        refresh();
        String id = mobTypeId.toLowerCase();
        Guild g = GuildManager.getPlayerGuild(killer);
        if (g != null) {
            int[] p = progress.computeIfAbsent(g.getId(), k -> new int[WEEKLY]);
            for (int i = 0; i < weekly.size(); i++) {
                Contract c = weekly.get(i);
                if (p[i] < 0 || !c.family().matches(id)) continue;
                dirty = true;
                if (++p[i] < c.count()) continue;
                p[i] = -1;                                                                        // accompli
                g.addTreasury(c.treasury());
                g.addContribution(c.contribution());
                notifyGuild(g, "<color:gold>CONTRAT DE GUILDE ACCOMPLI !</color> <color:green>" + c.title() + " : +"
                        + BoardScreen.gold(c.treasury()) + " or au trésor, +" + BoardScreen.gold(c.contribution()) + " contribution</color>");
                EldaniorLogger.info("[Boards] guilde " + g.getTag() + " accomplit " + c.title());
            }
        }
    }

    private static void notifyGuild(Guild g, String text) {
        for (UUID member : g.getMembers()) {
            PlayerRef pr = Universe.get().getPlayer(member);
            if (pr != null) NotificationHelper.sendNotification(pr, text, NotificationStyle.Success);
        }
    }

    // ===== Persistance =====

    public static synchronized void save() {
        if (file == null) return;
        try {
            if (!Files.exists(file.getParent())) Files.createDirectories(file.getParent());
            Properties props = new Properties();
            props.setProperty("_version", "1");
            if (week != null) props.setProperty("week", week);
            for (int i = 0; i < weekly.size(); i++) props.setProperty("weekly." + i, String.valueOf(CONTRACTS.indexOf(weekly.get(i))));
            progress.forEach((g, p) -> props.setProperty("progress." + g, Arrays.toString(p).replaceAll("[\\[\\] ]", "")));
            PersistenceUtils.writeAtomicWithBackup(file, props, "Eldanior - tableau de guilde");
            dirty = false;
        } catch (Exception e) {
            EldaniorLogger.error("[Boards] sauvegarde du tableau de guilde", e);
        }
    }

    private static void load() {
        if (file == null || !Files.exists(file)) return;
        try {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(file)) { props.load(in); }
            week = props.getProperty("week");
            if (week != null) week = week.replace("-W", "W");                                // ancien format « 2026-W47 »
            List<Contract> w = new ArrayList<>();
            for (int i = 0; props.getProperty("weekly." + i) != null; i++) w.add(CONTRACTS.get(Integer.parseInt(props.getProperty("weekly." + i))));
            weekly = List.copyOf(w);
            if (weekly.size() != WEEKLY) week = null;                                         // tirage a refaire
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("progress.")) {
                    int[] p = Arrays.stream(props.getProperty(key).split(",")).mapToInt(Integer::parseInt).toArray();
                    progress.put(key.substring(9), p);
                }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Boards] chargement du tableau de guilde", e);
        }
    }
}
