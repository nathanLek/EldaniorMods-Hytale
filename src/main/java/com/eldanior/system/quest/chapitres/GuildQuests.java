package com.eldanior.system.quest.chapitres;

import com.eldanior.system.boards.GuildBoard;
import com.eldanior.system.guild.Guild;
import com.eldanior.system.guild.GuildManager;
import com.eldanior.system.quest.PlayerQuest;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.quest.chapitres.ChapterQuests.Act;
import com.eldanior.system.quest.chapitres.ChapterQuests.Art;
import com.eldanior.system.quest.chapitres.ChapterQuests.Chain;
import com.eldanior.system.quest.chapitres.ChapterQuests.Kind;
import com.eldanior.system.quest.chapitres.ChapterQuests.MissionType;
import com.eldanior.system.quest.chapitres.ChapterQuests.Objective;
import com.eldanior.system.quest.chapitres.ChapterQuests.Reward;
import com.eldanior.system.quest.chapitres.ChapterQuests.RewardKind;
import com.eldanior.system.quest.chapitres.ChapterQuests.State;
import com.eldanior.system.shop.CoinWallet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tableau de guilde en cartes facon Chapitres (2026-11-19, demande de l'utilisateur : « transformer la fenetre du tableau de
 * guilde en fenetre comme celle des quetes, les contrats et missions en Quetes de Guilde et Missions de Guilde, cartes d'une
 * nouvelle couleur » : contour rose, teinte « Rose » de gen_chapitres_ui.py). Donnees : boards.GuildBoard.
 *   - Quete de guilde (MissionType.GUILDE) = contrat de la semaine : avancement commun a la guilde (attendu GUILDE, lu dans
 *     GuildBoard), qui avance par les mises a mort de tous les membres, acceptee ou non ; accomplie, le tresor et la
 *     contribution sont verses a la guilde (GuildBoard.onMobKill). Pour un membre la carte est « En cours » d'office ; « Suivre »
 *     la met au HUD (PlayerQuest, hors limite des quetes). Hors guilde : refusee.
 *   - Mission de guilde (MissionType.MISSION_GUILDE) = mission individuelle du jour : vraie quete de chasse (KILL), au plus
 *     GuildBoard.MAX_TAKEN a la fois, hors limite des quetes (comme l'ancien tableau) ; rendue au tableau : pieces et XP
 *     (bonus Fortune et Sagesse de la guilde, familier), contribution pour sa guilde. Abandon possible depuis la carte.
 * Chaines creees a la volee (comme Primes) ; identifiants : chap_guilde_q_<semaine>_<i>_1, chap_guilde_m_<jour>_<i>_1. Une
 * mission prise un autre jour se reconstruit d'apres sa date (GuildBoard.missionsFor).
 */
public final class GuildQuests {

    private GuildQuests() {}

    static final String CONTRACT = "guilde_q_", MISSION = "guilde_m_";
    private static final String PREFIX = "chap_guilde_";

    /** Chaines deja construites, par identifiant de chaine (les memes objets d'un appel a l'autre). */
    private static final Map<String, Chain> CACHE = new ConcurrentHashMap<>();

    // ==================== CARTES ====================

    /**
     * Cartes du tableau pour ce joueur : quetes de guilde de la semaine, missions du jour, puis ses missions d'un autre jour
     * encore en cours. Range au passage ses vieilles quetes de guilde (semaine passee, missions rendues d'un autre jour).
     */
    public static List<Act> acts(UUID me) {
        purge(me);
        List<Act> out = new ArrayList<>();
        String week = GuildBoard.week();
        List<GuildBoard.Contract> weekly = GuildBoard.weekly();
        for (int i = 0; i < weekly.size(); i++) out.add(contract(week, i, weekly.get(i)).acts.get(0));
        long today = LocalDate.now().toEpochDay();
        List<GuildBoard.Mission> missions = GuildBoard.missions();
        for (GuildBoard.Mission m : missions) out.add(mission(m).acts.get(0));
        if (me != null) for (PlayerQuest pq : QuestManager.getPlayerQuests(me)) {
            Act a = isMission(pq.getQuestId()) ? act(pq.getQuestId()) : null;
            if (a != null && !out.contains(a) && dayOf(a) != today) out.add(a);
        }
        return out;
    }

    public static boolean isGuild(String questId) { return questId != null && questId.startsWith(PREFIX); }

    static boolean isMission(String questId) { return questId != null && questId.startsWith("chap_" + MISSION); }

    static boolean isMission(Act a) { return a != null && a.chain.type == MissionType.MISSION_GUILDE; }

    static boolean isContract(Act a) { return a != null && a.chain.type == MissionType.GUILDE; }

    /** Acte d'apres son identifiant (null : inconnu, ou quete de guilde d'une autre semaine). */
    static Act act(String questId) {
        if (!isGuild(questId) || !questId.endsWith("_1")) return null;
        String[] p = questId.substring(PREFIX.length(), questId.length() - 2).split("_");   // q|m, semaine|jour, rang
        if (p.length != 3) return null;
        try {
            int i = Integer.parseInt(p[2]);
            if (p[0].equals("q")) {
                List<GuildBoard.Contract> weekly = GuildBoard.weekly();
                return p[1].equals(GuildBoard.week()) && i < weekly.size() ? contract(p[1], i, weekly.get(i)).acts.get(0) : null;
            }
            if (!p[0].equals("m")) return null;
            List<GuildBoard.Mission> day = GuildBoard.missionsFor(LocalDate.ofEpochDay(Long.parseLong(p[1])));
            return i < day.size() ? mission(day.get(i)).acts.get(0) : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static com.eldanior.system.quest.QuestModel model(String questId) {
        Act a = act(questId);
        return a != null ? a.model : null;
    }

    // ==================== CONSTRUCTION ====================

    private static Chain contract(String week, int index, GuildBoard.Contract c) {
        return CACHE.computeIfAbsent(CONTRACT + week + "_" + index, id -> {
            Chain ch = new Chain(id, ChapterQuests.GUILDE, "Contrat de la semaine", null, MissionType.GUILDE);
            String story = "« Avis à toutes les guildes du royaume : " + c.count() + " " + c.family().label + " doivent tomber "
                    + "avant dimanche minuit. Chaque bête abattue par l'un des vôtres compte pour votre guilde. La première "
                    + "à remplir le contrat n'est pas la seule payée : toute guilde qui le remplit touche la récompense. »";
            List<String> notes = List.of(
                    "« Les mises à mort de tous les membres comptent, même sans avoir accepté la quête. »",
                    "« Récompense versée au trésor de la guilde : " + gold(c.treasury()) + " or et +" + gold(c.contribution()) + " contribution. »",
                    "« Nouveaux contrats chaque lundi ; ceux-ci prennent fin dimanche à minuit. »");
            ch.act(c.title(), art(c.family()), contractRank(c.count()), 0, story, notes,
                    List.of(new Objective(Kind.GUILDE, String.valueOf(index), c.count(), "Tuer " + gold(c.count()) + " " + c.family().label + " (votre guilde)")),
                    List.of(new Reward(RewardKind.MONEY, CoinWallet.coinFor(c.treasury()), c.treasury(),
                            gold(c.treasury()) + " or au trésor de la guilde, +" + gold(c.contribution()) + " contribution")));
            for (Act a : ch.acts) a.model = new ChapterQuests.ActQuest(a);
            return ch;
        });
    }

    private static Chain mission(GuildBoard.Mission m) {
        return CACHE.computeIfAbsent(MISSION + m.id(), id -> {
            Chain ch = new Chain(id, ChapterQuests.GUILDE, "Mission du jour", null, MissionType.MISSION_GUILDE);
            String story = "« On demande un chasseur : " + m.count() + " " + m.family().label + " à abattre. Prenez la mission, "
                    + "faites le travail, puis revenez à un tableau de guilde toucher votre dû. »";
            List<String> notes = List.of(
                    "« Seules les mises à mort après l'avoir prise comptent. »",
                    "« Membre d'une guilde ? Elle gagne +" + gold(m.contribution()) + " contribution, et ses bonus de Fortune et de Sagesse s'appliquent. »",
                    "« Au plus " + GuildBoard.MAX_TAKEN + " missions à la fois. Nouvelles missions chaque jour à minuit. »");
            ch.act(m.title(), art(m.family()), missionRank(m.count()), 0, story, notes,
                    List.of(new Objective(Kind.KILL, m.family().pattern(), m.count(), "Tuer " + m.count() + " " + m.family().label)),
                    List.of(Reward.money(m.gold()), Reward.xp(m.xp())));
            for (Act a : ch.acts) a.model = new ChapterQuests.ActQuest(a);
            return ch;
        });
    }

    /** Illustration selon la famille de monstres (illustrations existantes). */
    private static Art art(GuildBoard.Family f) {
        return switch (f) {
            case UNDEAD -> Art.OMBRE;
            case TRORK -> Art.TRORK;
            case GOBLIN -> Art.BRIGANDS;
            case OUTLANDER -> Art.OUTLANDERS;
            case SCARAK -> Art.DUNES;
            case SPIDER -> Art.MARAIS;
            case BEAST -> Art.LISIERE;
            case ANY -> Art.ARENE;
        };
    }

    private static QuestDifficulty contractRank(int count) {
        if (count >= 1_000) return QuestDifficulty.A;
        if (count >= 400) return QuestDifficulty.B;
        return QuestDifficulty.C;
    }

    private static QuestDifficulty missionRank(int count) {
        if (count >= 80) return QuestDifficulty.C;
        if (count >= 40) return QuestDifficulty.D;
        return QuestDifficulty.E;
    }

    // ==================== ETAT ====================

    /** Index du contrat d'une quete de guilde (attendu GUILDE). */
    private static int index(Act a) {
        Objective o = a.objectives.isEmpty() ? null : a.objectives.get(0);
        try { return o != null ? Integer.parseInt(o.target()) : -1; } catch (NumberFormatException e) { return -1; }
    }

    /** Avancement de la guilde du joueur sur un attendu GUILDE (ChapterQuests.progress). */
    static int progress(UUID me, Objective o) {
        Guild g = me != null ? GuildManager.getPlayerGuild(me) : null;
        int p;
        try { p = GuildBoard.progress(g, Integer.parseInt(o.target())); } catch (NumberFormatException e) { return 0; }
        return p < 0 ? o.amount() : p;
    }

    /**
     * Etat d'une quete de guilde (contrat), null pour toute autre carte : accomplie par la guilde = DONE ; membre = IN_PROGRESS
     * (ses kills comptent deja) ; hors guilde = AVAILABLE (« Accepter » explique qu'il faut une guilde).
     */
    static State state(UUID me, Act a) {
        if (!isContract(a)) return null;
        Guild g = me != null ? GuildManager.getPlayerGuild(me) : null;
        if (g == null) return State.AVAILABLE;
        return GuildBoard.progress(g, index(a)) < 0 ? State.DONE : State.IN_PROGRESS;
    }

    /** Pourquoi ce joueur ne peut pas prendre cette carte (null : il peut, ou ce n'est pas une carte de guilde). */
    static String refusal(UUID me, Act a) {
        if (isContract(a)) return me == null || GuildManager.getPlayerGuild(me) == null
                ? "Les quêtes de guilde sont réservées aux membres d'une guilde. Rejoignez-en une auprès du Maître de guilde." : null;
        if (!isMission(a)) return null;
        if (!GuildBoard.missions().contains(missionOf(a))) return "Cette mission n'est plus proposée.";
        if (missionCount(me) >= GuildBoard.MAX_TAKEN) return "Vous avez déjà " + GuildBoard.MAX_TAKEN + " missions de guilde en cours.";
        return null;
    }

    /** Missions de guilde en cours (non rendues) du joueur. */
    public static int missionCount(UUID me) {
        if (me == null) return 0;
        int n = 0;
        for (PlayerQuest pq : QuestManager.getPlayerQuests(me)) if (isMission(pq.getQuestId()) && !pq.isCompleted()) n++;
        return n;
    }

    /** Aide affichee sous les cartes, a la place de celle par defaut (null : celle par defaut). */
    static String hint(UUID me, Act a, State s) {
        if (isContract(a)) {
            long days = GuildBoard.daysLeft();
            String end = days == 0 ? "Fin ce soir à minuit." : "Fin dans " + days + " jour" + (days > 1 ? "s" : "") + ".";
            return switch (s) {
                case AVAILABLE -> "Réservée aux membres d'une guilde. " + end;
                case IN_PROGRESS -> (ChapterQuests.owned(me, a) != null && ChapterQuests.owned(me, a).isActive()
                        ? "Quête suivie : l'avancement de votre guilde s'affiche à l'écran. " : "Votre guilde y travaille : « Suivre » l'affiche à l'écran. ") + end;
                case DONE -> "Contrat rempli par votre guilde : la récompense a été versée à son trésor.";
                default -> null;
            };
        }
        if (isMission(a) && s == State.AVAILABLE)
            return "Prenez la mission, puis revenez à un tableau de guilde la rendre. Missions en cours : "
                    + missionCount(me) + " / " + GuildBoard.MAX_TAKEN + ".";
        return null;
    }

    // ==================== RENDU ====================

    private static GuildBoard.Mission missionOf(Act a) {
        String id = a.chain.id.substring(MISSION.length());
        try {
            for (GuildBoard.Mission m : GuildBoard.missionsFor(LocalDate.ofEpochDay(Long.parseLong(id.split("_")[0]))))
                if (m.id().equals(id)) return m;
        } catch (RuntimeException ignored) {}
        return null;
    }

    private static long dayOf(Act a) {
        try { return Long.parseLong(a.chain.id.substring(MISSION.length()).split("_")[0]); } catch (RuntimeException e) { return -1; }
    }

    /** Pieces d'une mission rendue : bonus Fortune de la guilde. */
    static long gold(UUID me, long amount) { return com.eldanior.system.guild.GuildUpgrade.applyGold(me, amount); }

    /** XP d'une mission rendue : bonus Sagesse de la guilde, puis familier. */
    static int xp(UUID me, int amount) {
        return com.eldanior.system.familiers.FamilierManager.applyXp(me, com.eldanior.system.guild.GuildUpgrade.applyXp(me, amount));
    }

    /** Mission rendue : contribution pour la guilde du joueur ; renvoie le texte a ajouter au message (vide sans guilde). */
    static String onTurnIn(UUID me, Act a) {
        GuildBoard.Mission m = missionOf(a);
        Guild g = me != null ? GuildManager.getPlayerGuild(me) : null;
        if (m == null || g == null) return "";
        g.addContribution(m.contribution());
        return " +" + gold(m.contribution()) + " contribution pour " + g.getFormattedTag() + ".";
    }

    /** Une mission vient d'etre remplie (ChapterQuests.bump) : le joueur est prevenu. */
    static void onFilled(UUID me, Act a) {
        if (!isMission(a)) return;
        var pr = com.hypixel.hytale.server.core.universe.Universe.get().getPlayer(me);
        if (pr != null) com.eldanior.system.Leveling.utils.NotificationHelper.sendNotification(pr,
                "<color:gold>Mission de guilde accomplie : " + a.name + "</color> <color:gray>(rendez-la à un tableau de guilde)</color>",
                com.hypixel.hytale.protocol.packets.interface_.NotificationStyle.Success);
    }

    /**
     * Range les vieilles quetes de guilde du joueur : quetes de guilde d'une autre semaine ou deja remplies, missions rendues
     * d'un autre jour. Les missions d'un autre jour encore en cours restent (a finir et rendre).
     */
    private static void purge(UUID me) {
        if (me == null) return;
        long today = LocalDate.now().toEpochDay();
        QuestManager.getPlayerQuests(me).removeIf(pq -> {
            String id = pq.getQuestId();
            if (!isGuild(id)) return false;
            Act a = act(id);
            if (a == null) return true;                                                  // semaine passee, illisible
            if (isContract(a)) return state(me, a) != State.IN_PROGRESS;
            return pq.isCompleted() && dayOf(a) != today;
        });
        CACHE.keySet().removeIf(k -> k.startsWith(CONTRACT) && !k.startsWith(CONTRACT + GuildBoard.week() + "_"));
    }

    static String gold(long v) { return HouseQuests.gold(v); }
}
