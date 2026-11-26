package com.eldanior.system.quest.chapitres;

import com.eldanior.system.boards.BountyBoard;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.guards.Criminals;
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
import com.eldanior.system.shop.CoinWallet;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Primes en quetes (2026-11-13, regles de l'utilisateur) : chaque avis de recherche (joueur PK ou avec une prime, criminel
 * PNJ : boards.BountyBoard, guards.Criminals) devient une quete de type PRIME, et chaque contrat de monstre du jour
 * (boards.MonsterContracts) une quete CONTRAT (gratuite, le premier qui l'abat gagne), au tableau de prime seulement.
 *   - tout le monde peut prendre le contrat, en payant 5 % de la prime (pieces portees) ;
 *   - chaque contrat pris fait monter la prime de la cible de 20 % ;
 *   - un seul vainqueur : le premier qui abat la cible avec le contrat en cours touche la prime (DeathXPSystem) ; les contrats
 *     des autres sont dechires (frais perdus) ; tuer une cible sans contrat ne rapporte pas sa prime ;
 *   - la cible n'est plus recherchee (prime effacee par un chasseur PNJ, plus PK) : contrats annules.
 * Les quetes sont creees a la volee (pas dans les registres statiques de ChapterQuests / QuestManager, lus par plusieurs fils) :
 * QuestManager.getQuest et ChapterQuests.act les cherchent ici en repli. Identifiant d'une quete :
 * chap_prime_<cible>_<generation>_1 ; la generation change a chaque nouvel avis sur la meme cible (sauvegardee), pour qu'un
 * vieux contrat ne revive pas.
 */
public final class Primes {

    private Primes() {}

    static final double FEE = 0.05, RAISE = 0.20;
    private static final int REFRESH_SECONDS = 15;

    /** Avis : prime (wanted : la cible) ou contrat de monstre (contract). key : cle de la cible ; bounty : prime ou recompense. */
    private record Entry(Chain chain, Act act, String key, String name, long bounty, BountyBoard.Wanted wanted,
                         com.eldanior.system.boards.MonsterContracts.Contract contract) {}

    /** Par identifiant de chaine. */
    private static final Map<String, Entry> ENTRIES = new ConcurrentHashMap<>();
    /** Cible (cle de l'avis) -> generation de son avis en cours. */
    private static final Map<String, String> GENERATIONS = new ConcurrentHashMap<>();
    private static Path file;

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("primes.properties");
        load();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { refresh(); } catch (Exception e) { EldaniorLogger.error("[Primes] mise a jour", e); }
        }, 5, REFRESH_SECONDS, TimeUnit.SECONDS);
    }

    // ==================== AVIS -> QUETES ====================

    /** Contrats affiches au tableau, de la plus grosse prime a la plus petite. */
    public static List<Act> acts() {
        refresh();
        List<Entry> all = new ArrayList<>(ENTRIES.values());
        all.sort(Comparator.comparing((Entry e) -> e.contract() != null).thenComparing(Comparator.comparingLong(Entry::bounty).reversed()));
        List<Act> out = new ArrayList<>();
        for (Entry e : all) out.add(e.act());
        return out;
    }

    static Act act(String actId) {
        if (actId == null || !actId.startsWith("chap_prime_")) return null;
        for (Entry e : ENTRIES.values()) if (e.act().id.equals(actId)) return e.act();
        return null;
    }

    /** Modele de quete d'un contrat (QuestManager.getQuest en repli), null si inconnu. */
    public static com.eldanior.system.quest.QuestModel model(String questId) {
        Act a = act(questId);
        return a != null ? a.model : null;
    }

    /**
     * Recherches actuels : joueurs du tableau, et criminels EN VIE (2026-11-14, demande de l'utilisateur : la prime d'un criminel
     * abattu ne reste plus affichee ; sa prime de base, liee a son niveau, ne tombe jamais a zero, d'ou le test sur alive).
     * Revenu a la vie, il a un nouvel avis (nouvelle generation : les anciens contrats ne revivent pas).
     */
    private static List<BountyBoard.Wanted> wanted() {
        List<BountyBoard.Wanted> out = new ArrayList<>();
        for (BountyBoard.Wanted w : BountyBoard.list()) if (!w.isCriminal() && w.bounty() > 0) out.add(w);
        for (Criminals.Wanted c : Criminals.list()) {
            if (c.bounty() <= 0 || !c.alive()) continue;
            out.add(new BountyBoard.Wanted(null, c.postId(), c.name(), c.level(), c.bounty(), c.alive(),
                    "Repaire : " + BountyBoard.zoneName(c.world(), c.lair()), 0, c.respawnAt()));
        }
        return out;
    }

    static synchronized void refresh() {
        Map<String, Object> live = new HashMap<>();
        for (BountyBoard.Wanted w : wanted()) live.put(w.key(), w);
        for (com.eldanior.system.boards.MonsterContracts.Contract c : com.eldanior.system.boards.MonsterContracts.getContracts())
            if (c.getClaimedBy() == null && c.zone() != null) live.put(c.key(), c);
        boolean changed = false;
        for (Map.Entry<String, Object> l : live.entrySet()) {
            String key = l.getKey();
            String gen = GENERATIONS.get(key);
            if (gen == null) {
                gen = Long.toString(System.currentTimeMillis(), 36);
                GENERATIONS.put(key, gen);
                changed = true;
            }
            boolean contract = l.getValue() instanceof com.eldanior.system.boards.MonsterContracts.Contract;
            String cid = (contract ? "contrat_" : "prime_") + key.replaceAll("[^A-Za-z0-9]", "") + "_" + gen;
            Entry e;
            if (contract) {
                var mc = (com.eldanior.system.boards.MonsterContracts.Contract) l.getValue();
                Chain c = buildContract(cid, mc);
                e = new Entry(c, c.acts.get(0), key, mc.getName(), mc.getReward(), null, mc);
            } else {
                BountyBoard.Wanted w = (BountyBoard.Wanted) l.getValue();
                Chain c = build(cid, w);
                e = new Entry(c, c.acts.get(0), key, w.name(), w.bounty(), w, null);
            }
            e.act().model = new ChapterQuests.ActQuest(e.act());
            ENTRIES.put(cid, e);
        }
        for (Entry e : new ArrayList<>(ENTRIES.values())) {
            if (live.containsKey(e.key())) continue;
            end(e, null, e.contract() != null ? "Le contrat sur " + e.name() + " est terminé."
                    : e.wanted() != null && e.wanted().isCriminal() ? "Le contrat sur " + e.name() + " est annulé : il a été abattu."
                    : "Le contrat sur " + e.name() + " est annulé : il n'est plus recherché.");
            changed = true;
        }
        if (changed) save();
    }

    /** Contrat de monstre d'elite du jour : gratuit, le premier qui l'abat touche la recompense (contrat ou pas). */
    private static Chain buildContract(String cid, com.eldanior.system.boards.MonsterContracts.Contract mc) {
        var z = mc.zone();
        String story = (mc.getStars() > 0 ? "« Un monstre étoilé (" + com.eldanior.system.boards.MonsterContracts.stars(mc.getStars()) + ") rôde près de "
                : "« Un monstre d'élite rôde près de ") + z.name() + " : " + mc.getName() + ", " + mc.getSpecies().toLowerCase()
                + " de niveau " + mc.getLevel() + ", deux fois plus grand que ses semblables. Le royaume paie qui le ramène à terre. »";
        List<String> notes = List.of(
                "« Prendre le contrat est gratuit : il s'affiche à l'écran et vous guide. »",
                "« Le premier qui l'abat touche la récompense. »",
                "« Les contrats changent chaque jour à minuit. »");
        return new Chain(cid, ChapterQuests.PRIMES, "Contrat de chasse", "Elda_Gold_Coins", MissionType.CONTRAT)
                .act(mc.title(), Art.NUIT, QuestDifficulty.max(rank(mc.getReward()), QuestDifficulty.forMonsterLevel(mc.getLevel())), 0, story, notes,
                        List.of(new Objective(Kind.PRIME, mc.key(), 1, "Abattre " + mc.getName())),
                        List.of(Reward.money(mc.getReward())));
    }

    private static Chain build(String cid, BountyBoard.Wanted w) {
        long bounty = w.bounty();
        String who = w.isCriminal() ? "le criminel " + w.name() : w.name();
        String where = w.zone() != null ? w.zone() : "inconnue";
        String story = "« Avis de recherche : " + who + ", niveau " + w.level() + ". Dernière trace : " + where + ". "
                + "Prenez le contrat, trouvez-le, abattez-le : sa prime est à vous, pourvu que vous soyez le premier. »";
        List<String> notes = List.of(
                "« Prendre ce contrat coûte 5 % de la prime : " + CoinWallet.format(fee(bounty)) + ". »",
                "« Chaque contrat pris fait monter sa prime de 20 %. »",
                "« Un seul vainqueur : le premier à l'abattre. Les autres contrats sont déchirés. »");
        return new Chain(cid, ChapterQuests.PRIMES, "Avis de recherche", "Elda_Gold_Coins", MissionType.PRIME)
                .act(w.name(), Art.DUEL, rank(bounty), 0, story, notes,
                        List.of(new Objective(Kind.PRIME, w.key(), 1, (w.isCriminal() ? "Abattre " : "Vaincre ") + w.name())),
                        List.of(Reward.money(bounty)));
    }

    static long fee(long bounty) { return Math.max(1, Math.round(bounty * FEE)); }

    /** Rang de mission selon la prime (en cuivres). */
    static QuestDifficulty rank(long bounty) {
        if (bounty >= 150_000) return QuestDifficulty.S;
        if (bounty >= 50_000) return QuestDifficulty.A;
        if (bounty >= 15_000) return QuestDifficulty.B;
        if (bounty >= 5_000) return QuestDifficulty.C;
        if (bounty >= 2_000) return QuestDifficulty.D;
        if (bounty >= 500) return QuestDifficulty.E;
        return QuestDifficulty.F;
    }

    // ==================== PRENDRE UN CONTRAT ====================

    /**
     * Prise d'un contrat (QuestChaptersPage, avant de l'accepter) : 5 % de la prime preleves dans les pieces portees, prime de
     * la cible +20 %. Renvoie le message de refus, ou null si c'est fait.
     */
    /** Avis d'une carte de prime (null : contrat de monstre ou carte inconnue). */
    public static BountyBoard.Wanted wantedOf(String chainId) {
        Entry e = chainId != null ? ENTRIES.get(chainId) : null;
        return e != null ? e.wanted() : null;
    }

    /**
     * Paiement (2026-11-13, regle de l'utilisateur) : les pieces portees d'abord, puis le compte en banque (data, modifie sur
     * place : l'appelant sauvegarde ce meme objet). False (rien preleve) si les deux ne suffisent pas.
     */
    public static boolean payWithBank(Player player, com.eldanior.system.config.Player.PlayerLevelData data, long cost) {
        if (cost <= 0) return true;
        long purse = player != null ? CoinWallet.value(player) : 0, bank = data != null ? data.getMoney() : 0;
        if (purse + bank < cost) return false;
        long fromPurse = Math.min(purse, cost);
        if (fromPurse > 0 && CoinWallet.pay(player, fromPurse) < 0) return false;
        long rest = cost - fromPurse;
        if (rest > 0) data.removeMoney(rest);
        return true;
    }

    static String take(UUID me, Player player, com.eldanior.system.config.Player.PlayerLevelData data, Act a) {
        Entry e = ENTRIES.get(a.chain.id);
        if (e == null) return "Cet avis n'est plus affiché.";
        if (e.contract() != null) return null;                                        // contrat de monstre : gratuit
        BountyBoard.Wanted w = e.wanted();
        if (w.player() != null && w.player().equals(me)) return "Vous ne pouvez pas prendre le contrat sur votre propre tête.";
        long fee = fee(w.bounty());
        if (!payWithBank(player, data, fee))                                           // pieces, puis banque ; jamais rendus
            return "Prendre ce contrat coûte " + CoinWallet.format(fee) + " (5 % de la prime) : ni vos pièces ni votre compte n'y suffisent.";
        long raise = Math.max(1, Math.round(w.bounty() * RAISE));
        BountyBoard.addBounty(w, raise);
        EldaniorLogger.info("[Primes] contrat pris sur " + w.name() + " (frais " + fee + ", prime +" + raise + ")");
        return null;
    }

    // ==================== VICTOIRE ====================

    /**
     * La cible (cle de l'avis : UUID du joueur, ou poste du criminel) est abattue par killer (DeathXPSystem). True si killer
     * avait le contrat : il gagne (la prime lui est versee par l'appelant), les autres contrats sont dechires.
     */
    public static boolean claim(UUID killer, String targetKey, String killerName) {
        if (killer == null || targetKey == null) return false;
        for (Entry e : new ArrayList<>(ENTRIES.values())) {
            if (!e.key().equals(targetKey)) continue;
            PlayerQuest mine = holder(killer, e.act());
            if (e.contract() != null) {                                              // contrat de monstre : fini pour tous
                end(e, killer, "Le contrat sur " + e.name() + " est terminé : " + killerName + " l'a abattu.");
                save();
                return mine != null;
            }
            if (mine == null) return false;
            end(e, killer, null);
            String text = "[Prime] " + killerName + " remporte le contrat sur " + e.name() + " !";
            for (PlayerRef pr : Universe.get().getPlayers()) pr.sendMessage(Message.raw(text).color("#e0574a"));
            save();
            return true;
        }
        return false;
    }

    /** Contrat en cours (non rendu) de ce joueur sur cet acte. */
    private static PlayerQuest holder(UUID me, Act a) {
        for (PlayerQuest pq : QuestManager.getPlayerQuests(me))
            if (pq.getQuestId().equals(a.id) && !pq.isCompleted()) return pq;
        return null;
    }

    /**
     * Fin d'un avis : contrats retires a tous (le vainqueur, winner, ne recoit pas de message ici), avis oublie (la prochaine
     * prime sur cette cible sera une nouvelle generation). cancelText : message aux porteurs (annulation).
     */
    private static void end(Entry e, UUID winner, String cancelText) {
        ENTRIES.remove(e.chain().id);
        GENERATIONS.remove(e.key());
        String lost = cancelText != null ? cancelText : "Votre contrat sur " + e.name() + " est déchiré : quelqu'un d'autre l'a abattu.";
        QuestManager.forEachPlayerQuests((uuid, list) -> {
            boolean had = list.removeIf(pq -> pq.getQuestId().equals(e.act().id));
            if (had && !uuid.equals(winner)) {
                PlayerRef pr = Universe.get().getPlayer(uuid);
                if (pr != null) pr.sendMessage(Message.raw("[Prime] " + lost).color("#e8a060"));
            }
        });
    }

    // ==================== SAUVEGARDE ====================

    private static synchronized void save() {
        if (file == null) return;
        try {
            Files.createDirectories(file.getParent());
            Properties p = new Properties();
            GENERATIONS.forEach(p::setProperty);
            com.eldanior.system.config.PersistenceUtils.writeAtomicWithBackup(file, p, "Eldanior - generations des avis de prime");
        } catch (Exception ex) {
            EldaniorLogger.error("[Primes] sauvegarde", ex);
        }
    }

    private static void load() {
        if (file == null || !Files.exists(file)) return;
        try (InputStream in = Files.newInputStream(file)) {
            Properties p = new Properties();
            p.load(in);
            for (String k : p.stringPropertyNames()) GENERATIONS.put(k, p.getProperty(k));
        } catch (Exception ex) {
            EldaniorLogger.error("[Primes] chargement", ex);
        }
    }
}
