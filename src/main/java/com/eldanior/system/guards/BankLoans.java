package com.eldanior.system.guards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.shop.CoinWallet;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Prets de la banque (demande de l'utilisateur, 2026-11-12), onglet « Prets » du banquier (BankPage) :
 *  - on emprunte au plus {@link #MULTIPLIER} fois ce que l'on possede (compte + bourse), un seul pret a la fois ;
 *  - on doit la somme + {@link #INTEREST} d'interets, a rembourser depuis le compte en {@link #TERM_MS} (une semaine) ;
 *  - a l'echeance, sans remboursement complet : la banque met une prime sur la tete du joueur ({@link #BOUNTY_RATIO} de la
 *    somme empruntee), il devient « Criminel ★ » (PK force : les chasseurs de prime l'attaquent, les gardes aussi) et figure au
 *    tableau des primes avec l'etoile ; s'il meurt dans cet etat, il perd TOUT (niveau 1, classe Novice, caracteristiques,
 *    competences, runes, objets, argent, famille, guilde, noblesse, titres, quetes) et reparait au point d'apparition ;
 *  - il peut encore rembourser au banquier : la dette soldee, la banque leve sa prime et l'etoile.
 * Rappel a la connexion et la veille de l'echeance. Etat sauvegarde dans eldanior_data/bank_loans.properties (joueurs hors
 * ligne compris : la prime de la banque les attend au tableau et a leur connexion, BountyBoard).
 */
public final class BankLoans {

    private BankLoans() {}

    public static final double MULTIPLIER = 3.0, INTEREST = 0.20, BOUNTY_RATIO = 0.5;
    public static final long TERM_MS = 7L * 24 * 3_600_000, REMIND_BEFORE_MS = 24L * 3_600_000;
    private static final int TICK_SECONDS = 10;

    /** Un pret. due : ce qui reste a rendre (interets compris). */
    public static final class Loan {
        public final UUID player;
        public String name;
        public int level;
        public long principal, due, takenAt, deadline, bankBounty;
        public boolean defaulted, starApplied, wipePending, reminded;

        Loan(UUID player) { this.player = player; }
    }

    private static final Map<UUID, Loan> loans = new ConcurrentHashMap<>();
    private static Path file;

    // ===== Cycle de vie =====

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("bank_loans.properties");
        load();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { tick(); } catch (Exception e) { EldaniorLogger.error("[Banque] suivi des prets", e); }
        }, TICK_SECONDS, TICK_SECONDS, TimeUnit.SECONDS);
        EldaniorLogger.info("[Banque] " + loans.size() + " pret(s) en cours.");
    }

    public static Loan get(UUID player) { return player == null ? null : loans.get(player); }

    /** Criminel ★ : debiteur dont l'echeance est passee (tableau des primes, menu). */
    public static boolean isStarred(UUID player) {
        Loan l = get(player);
        return l != null && l.defaulted;
    }

    /** Plafond d'un nouveau pret : MULTIPLIER x (compte + bourse). */
    public static long ceiling(long wealth) { return (long) Math.floor(Math.max(0, wealth) * MULTIPLIER); }

    public static long dueFor(long principal) { return Math.round(principal * (1 + INTEREST)); }

    // ===== Emprunter, rembourser (BankPage, thread du monde du joueur) =====

    /** Accorde le pret : verse amount au compte. Message d'erreur, ou null si c'est fait. */
    static String borrow(PlayerRef pr, PlayerLevelData copy, long wealth, long amount) {
        if (get(pr.getUuid()) != null) return "Vous avez déjà un prêt en cours : remboursez-le d'abord.";
        if (amount <= 0) return "Indiquez la somme à emprunter.";
        long max = ceiling(wealth);
        if (max <= 0) return "La banque ne prête qu'à ceux qui possèdent déjà de l'argent (compte ou bourse).";
        if (amount > max) return "Au plus " + CoinWallet.format(max) + " (3 fois ce que vous possédez).";
        Loan l = new Loan(pr.getUuid());
        l.name = pr.getUsername();
        l.level = copy.getLevel();
        l.principal = amount;
        l.due = dueFor(amount);
        l.takenAt = System.currentTimeMillis();
        l.deadline = l.takenAt + TERM_MS;
        loans.put(l.player, l);
        copy.addMoney(amount);
        save();
        EldaniorLogger.info("[Banque] pret de " + amount + " a " + l.name + " (doit " + l.due + ")");
        return null;
    }

    /**
     * Rembourse jusqu'a amount depuis le compte. Dette soldee : pret efface ; s'il etait en defaut, la banque leve sa prime et
     * l'etoile (PK force retire). Retourne ce qui a ete paye (0 : rien).
     */
    static long repay(PlayerRef pr, PlayerLevelData copy, long amount) {
        Loan l = get(pr.getUuid());
        if (l == null || amount <= 0) return 0;
        long pay = Math.min(Math.min(amount, l.due), copy.getMoney());
        if (pay <= 0) return 0;
        copy.removeMoney(pay);
        l.due -= pay;
        if (l.due <= 0) {
            loans.remove(l.player);
            if (l.defaulted) liftStar(pr, copy, l);
        }
        save();
        return pay;
    }

    /** Dette soldee apres l'echeance : la prime de la banque et l'etoile tombent. */
    private static void liftStar(PlayerRef pr, PlayerLevelData copy, Loan l) {
        if (l.starApplied) copy.setForcePK(false);
        long remove = Math.min(l.bankBounty, copy.getBounty());
        if (remove > 0) copy.addBounty(-remove);
        if (copy.getBounty() > 0) com.eldanior.system.quest.QuestManager.registerBounty(pr.getUuid(), pr.getUsername(), copy.getLevel(), copy.getBounty());
        else com.eldanior.system.quest.QuestManager.unregisterBounty(pr.getUuid());
        announce("[Banque] " + pr.getUsername() + " a soldé sa dette : la banque lève sa prime.");
    }

    // ===== Mort, suivi =====

    /** Un joueur meurt (DeathXPSystem) : un debiteur en defaut perdra tout a sa reapparition. */
    public static void onDeath(UUID player) {
        Loan l = get(player);
        if (l != null && l.defaulted && !l.wipePending) {
            l.wipePending = true;
            save();
        }
    }

    /** Toutes les TICK_SECONDS : echeances (joueurs hors ligne compris), puis chaque debiteur connecte dans son monde. */
    private static void tick() {
        long now = System.currentTimeMillis();
        for (Loan l : loans.values()) if (!l.defaulted && now >= l.deadline) defaultLoan(l);
        for (Loan l : loans.values()) {
            PlayerRef pr = Universe.get().getPlayer(l.player);
            Ref<EntityStore> ref = pr != null ? pr.getReference() : null;
            if (ref == null || !ref.isValid()) continue;
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            if (world != null) world.execute(() -> {
                try { follow(pr, ref, store, world, l); } catch (Exception e) { EldaniorLogger.error("[Banque] debiteur " + l.name, e); }
            });
        }
    }

    /** Echeance depassee : prime de la banque (au tableau, et a la connexion s'il est absent), annonce. */
    private static void defaultLoan(Loan l) {
        l.defaulted = true;
        l.bankBounty = Math.max(com.eldanior.system.boards.BountyBoard.MIN_PLAYER_BOUNTY, Math.round(l.principal * BOUNTY_RATIO));
        save();
        com.eldanior.system.boards.BountyBoard.placeOnPlayer(l.player, l.name, l.level, l.bankBounty);
        announce("[Banque] " + l.name + " n'a pas remboursé sa dette : criminel étoilé, prime de " + CoinWallet.format(l.bankBounty)
                + " sur sa tête.");
        EldaniorLogger.info("[Banque] defaut de " + l.name + " : prime " + l.bankBounty);
    }

    /** Debiteur connecte (thread de son monde) : etoile, rappel, perte totale apres une mort. */
    private static void follow(PlayerRef pr, Ref<EntityStore> ref, Store<EntityStore> store, World world, Loan l) {
        if (!ref.isValid()) return;
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return;
        if (l.wipePending) {
            if (store.getComponent(ref, DeathComponent.getComponentType()) != null) return;     // pas encore reapparu
            wipe(pr, ref, store, world);
            return;
        }
        if (l.defaulted && !l.starApplied) {
            PlayerLevelData copy = (PlayerLevelData) data.clone();
            copy.setForcePK(true);
            store.putComponent(ref, type, copy);
            l.starApplied = true;
            save();
            NotificationHelper.sendNotification(pr, "<color:red>Vous n'avez pas remboursé la banque : vous êtes Criminel étoilé.</color> "
                    + "<color:gray>Une prime est sur votre tête et les chasseurs de prime vous traquent. Si vous mourez, vous perdez "
                    + "tout. Soldez votre dette chez un banquier pour lever la prime.</color>", NotificationStyle.Danger);
        }
        long left = l.deadline - System.currentTimeMillis();
        if (!l.defaulted && !l.reminded && left <= REMIND_BEFORE_MS) {
            l.reminded = true;
            save();
            NotificationHelper.sendNotification(pr, "<color:gold>Votre prêt arrive à échéance dans " + duration(left) + " :</color> "
                    + "<color:gray>il reste " + CoinWallet.format(l.due) + " à rembourser chez un banquier.</color>", NotificationStyle.Warning);
        }
    }

    /** Rappel a la connexion (PlayerLoginSystem). */
    public static void remind(PlayerRef pr) {
        Loan l = get(pr.getUuid());
        if (l == null || l.defaulted) return;
        NotificationHelper.sendNotification(pr, "<color:gold>Prêt en cours :</color> <color:gray>" + CoinWallet.format(l.due)
                + " à rembourser dans " + duration(l.deadline - System.currentTimeMillis()) + ".</color>", NotificationStyle.Warning);
    }

    /**
     * Perte totale du debiteur mort : comme le serment de la voie martiale, mais sans rien garder (niveau 1, Novice,
     * caracteristiques de depart, compte vide, inventaire entier vide), puis le point d'apparition du monde.
     */
    private static void wipe(PlayerRef pr, Ref<EntityStore> ref, Store<EntityStore> store, World world) {
        if (!com.eldanior.system.Leveling.utils.CharacterWipe.wipe(pr, ref, store, world)) return;   // perte totale (partagee)
        loans.remove(pr.getUuid());
        save();
        NotificationHelper.sendNotification(pr, "<color:red>La banque s'est payée sur votre vie : vous avez tout perdu.</color> "
                + "<color:gray>Vous recommencez au niveau 1.</color>", NotificationStyle.Danger);
        announce("[Banque] " + pr.getUsername() + ", criminel étoilé endetté, est mort : il a tout perdu.");
    }

    // ===== Outils =====

    public static String duration(long ms) {
        if (ms <= 0) return "0 h";
        long h = ms / 3_600_000, d = h / 24;
        if (d > 0) return d + " j " + (h % 24) + " h";
        long m = (ms / 60_000) % 60;
        return h > 0 ? h + " h " + m + " min" : Math.max(1, m) + " min";
    }

    private static void announce(String text) {
        for (PlayerRef pr : Universe.get().getPlayers()) pr.sendMessage(Message.raw(text).color(Color.ORANGE));
    }

    // ===== Sauvegarde =====

    private static synchronized void load() {
        loans.clear();
        if (file == null || !Files.exists(file)) return;
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) { p.load(r); }
        catch (IOException e) { EldaniorLogger.error("[Banque] lecture des prets", e); return; }
        for (String key : p.stringPropertyNames()) {
            try {
                String[] f = p.getProperty(key).split("\\|", -1);
                Loan l = new Loan(UUID.fromString(key));
                l.name = f[0];
                l.level = Integer.parseInt(f[1]);
                l.principal = Long.parseLong(f[2]);
                l.due = Long.parseLong(f[3]);
                l.takenAt = Long.parseLong(f[4]);
                l.deadline = Long.parseLong(f[5]);
                l.bankBounty = Long.parseLong(f[6]);
                l.defaulted = Boolean.parseBoolean(f[7]);
                l.starApplied = Boolean.parseBoolean(f[8]);
                l.wipePending = Boolean.parseBoolean(f[9]);
                l.reminded = Boolean.parseBoolean(f[10]);
                loans.put(l.player, l);
            } catch (Exception e) { EldaniorLogger.warn("[Banque] pret illisible : " + key); }
        }
    }

    static synchronized void save() {
        if (file == null) return;
        Properties p = new Properties();
        for (Loan l : loans.values())
            p.setProperty(l.player.toString(), String.join("|", l.name == null ? "" : l.name.replace("|", " "), String.valueOf(l.level),
                    String.valueOf(l.principal), String.valueOf(l.due), String.valueOf(l.takenAt), String.valueOf(l.deadline),
                    String.valueOf(l.bankBounty), String.valueOf(l.defaulted), String.valueOf(l.starApplied),
                    String.valueOf(l.wipePending), String.valueOf(l.reminded)));
        try {
            Files.createDirectories(file.getParent());
            try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) { p.store(w, "Prets de la banque (BankLoans)"); }
        } catch (IOException e) { EldaniorLogger.error("[Banque] sauvegarde des prets", e); }
    }
}
