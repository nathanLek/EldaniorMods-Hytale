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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Donnees du tableau de guilde :
 *   - contrats de guilde : {@link #WEEKLY} contrats par semaine (tires dans CONTRACTS, les memes pour toutes les guildes) ;
 *     chaque monstre tue par un membre fait avancer ceux de sa guilde (DeathXPSystem -> onMobKill). Accompli : tresor et
 *     contribution pour la guilde, membres connectes prevenus ;
 *   - missions : {@link #MISSIONS_PER_DAY} missions individuelles par jour ; un joueur en prend au plus {@link #MAX_TAKEN},
 *     les accomplit par ses kills et revient au tableau toucher or et XP (et de la contribution pour sa guilde).
 * Sauvegarde : eldanior_data/guild_board.properties.
 */
public final class GuildBoard {

    private GuildBoard() {}

    static final int WEEKLY = 3, MISSIONS_PER_DAY = 6, MAX_TAKEN = 2;

    /** Famille de monstres : mots-cles cherches dans l'identifiant du monstre (vide : tous). */
    enum Family {
        UNDEAD("morts-vivants", "skeleton zombie wraith ghoul mummy lich"),
        TRORK("trorks", "trork"),
        GOBLIN("gobelins", "goblin"),
        OUTLANDER("outlanders", "outlander"),
        SCARAK("scaraks", "scarak"),
        SPIDER("araignées", "spider"),
        BEAST("bêtes sauvages", "wolf bear boar yeti raptor rex crocodile toad werewolf"),
        ANY("monstres", "");

        final String label;
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
    }

    /** Contrat de guilde : titre, famille, nombre a tuer, recompenses de la guilde. */
    record Contract(String title, Family family, int count, long treasury, long contribution) {}

    private static final List<Contract> CONTRACTS = List.of(
            new Contract("Fléau des morts-vivants", Family.UNDEAD, 500, 25_000, 2_500),
            new Contract("Chasse aux trorks", Family.TRORK, 300, 20_000, 2_000),
            new Contract("Razzia gobeline", Family.GOBLIN, 300, 18_000, 1_800),
            new Contract("Purge des outlanders", Family.OUTLANDER, 200, 20_000, 2_000),
            new Contract("Nid de scaraks", Family.SCARAK, 250, 18_000, 1_800),
            new Contract("Toiles et venin", Family.SPIDER, 250, 15_000, 1_500),
            new Contract("Grande battue", Family.BEAST, 400, 15_000, 1_500),
            new Contract("Mille têtes", Family.ANY, 2_000, 40_000, 4_000));

    /** Mission individuelle offerte au tableau. */
    record Mission(String id, String title, Family family, int count, long gold, int xp, long contribution) {}

    /** Mission prise par un joueur. */
    static final class Taken {
        final Mission mission;
        volatile int progress;

        Taken(Mission mission, int progress) { this.mission = mission; this.progress = progress; }

        boolean done() { return progress >= mission.count(); }
    }

    private static volatile String week;
    private static volatile List<Contract> weekly = List.of();
    /** Avancement des contrats de la semaine, par guilde (-1 : accompli). */
    private static final Map<String, int[]> progress = new ConcurrentHashMap<>();
    private static volatile LocalDate day;
    private static volatile List<Mission> missions = List.of();
    private static final Map<UUID, List<Taken>> taken = new ConcurrentHashMap<>();
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
        String w = now.get(IsoFields.WEEK_BASED_YEAR) + "-W" + now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
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
            Random rng = new Random(now.toEpochDay() * 31 + 7);
            List<Mission> list = new ArrayList<>();
            for (int i = 0; i < MISSIONS_PER_DAY; i++) {
                Family f = Family.values()[rng.nextInt(Family.values().length)];
                int count = (f == Family.ANY ? 60 : 20) + rng.nextInt(9) * 5;
                String title = f == Family.ANY ? "Nettoyage des environs" : "Chasse aux " + f.label;
                list.add(new Mission(now + "#" + i, title, f, count, count * 40L, count * 30, count));
            }
            missions = List.copyOf(list);
            dirty = true;
        }
    }

    static List<Contract> weekly() { refresh(); return weekly; }

    static List<Mission> missions() { refresh(); return missions; }

    /** Avancement d'un contrat de la semaine pour une guilde ; -1 : accompli. */
    static int progress(Guild g, int index) {
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
    static LocalDate weekEnd() {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    static List<Taken> takenBy(UUID player) {
        return player != null ? taken.getOrDefault(player, List.of()) : List.of();
    }

    static Taken taken(UUID player, String missionId) {
        for (Taken t : takenBy(player)) if (t.mission.id().equals(missionId)) return t;
        return null;
    }

    // ===== Missions =====

    /** Mission prise, vue de l'onglet Quetes du systeme (QuestTab). */
    public record MissionView(String id, String title, String objective, int progress, int count, long gold, int xp,
                              long contribution, boolean done) {}

    public static List<MissionView> missionsOf(UUID player) {
        List<MissionView> out = new ArrayList<>();
        for (Taken t : takenBy(player)) {
            Mission m = t.mission;
            out.add(new MissionView(m.id(), m.title(), "Tuer " + m.count() + " " + m.family().label, Math.min(t.progress, m.count()),
                    m.count(), m.gold(), m.xp(), m.contribution(), t.done()));
        }
        return out;
    }

    public static void abandonMission(UUID player, String missionId) { abandon(player, missionId); }

    /** Prend une mission du jour ; message d'erreur, ou null si c'est fait. */
    static synchronized String take(UUID player, Mission m) {
        List<Taken> mine = taken.computeIfAbsent(player, k -> new CopyOnWriteArrayList<>());
        if (taken(player, m.id()) != null) return "Vous avez déjà cette mission.";
        if (mine.size() >= MAX_TAKEN) return "Vous avez déjà " + MAX_TAKEN + " missions en cours.";
        mine.add(new Taken(m, 0));
        dirty = true;
        return null;
    }

    static synchronized void abandon(UUID player, String missionId) {
        List<Taken> mine = taken.get(player);
        if (mine != null && mine.removeIf(t -> t.mission.id().equals(missionId))) dirty = true;
    }

    /** Retire une mission accomplie (la recompense est versee par la fenetre) ; null si elle n'est pas accomplie. */
    static synchronized Mission claim(UUID player, String missionId) {
        Taken t = taken(player, missionId);
        if (t == null || !t.done()) return null;
        taken.get(player).remove(t);
        dirty = true;
        save();
        return t.mission;
    }

    // ===== Kills =====

    /** Monstre tue par un joueur (DeathXPSystem) : contrats de sa guilde, ses missions. */
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
        for (Taken t : takenBy(killer)) {
            if (t.done() || !t.mission.family().matches(id)) continue;
            t.progress++;
            dirty = true;
            if (!t.done()) continue;
            PlayerRef pr = Universe.get().getPlayer(killer);
            if (pr != null) NotificationHelper.sendNotification(pr, "<color:gold>Mission accomplie : " + t.mission.title()
                    + "</color> <color:gray>(revenez au tableau de guilde toucher la récompense)</color>", NotificationStyle.Success);
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
            taken.forEach((u, list) -> {
                for (int i = 0; i < list.size(); i++) {
                    Taken t = list.get(i);
                    Mission m = t.mission;
                    props.setProperty("taken." + u + "." + i, String.join("|", m.id(), m.title(), m.family().name(),
                            String.valueOf(m.count()), String.valueOf(m.gold()), String.valueOf(m.xp()),
                            String.valueOf(m.contribution()), String.valueOf(t.progress)));
                }
            });
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
            List<Contract> w = new ArrayList<>();
            for (int i = 0; props.getProperty("weekly." + i) != null; i++) w.add(CONTRACTS.get(Integer.parseInt(props.getProperty("weekly." + i))));
            weekly = List.copyOf(w);
            if (weekly.size() != WEEKLY) week = null;                                         // tirage a refaire
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("progress.")) {
                    int[] p = Arrays.stream(props.getProperty(key).split(",")).mapToInt(Integer::parseInt).toArray();
                    progress.put(key.substring(9), p);
                } else if (key.startsWith("taken.")) {
                    String[] k = key.split("\\.");
                    String[] v = props.getProperty(key).split("\\|");
                    Mission m = new Mission(v[0], v[1], Family.valueOf(v[2]), Integer.parseInt(v[3]), Long.parseLong(v[4]),
                            Integer.parseInt(v[5]), Long.parseLong(v[6]));
                    taken.computeIfAbsent(UUID.fromString(k[1]), u -> new CopyOnWriteArrayList<>()).add(new Taken(m, Integer.parseInt(v[7])));
                }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Boards] chargement du tableau de guilde", e);
        }
    }
}
