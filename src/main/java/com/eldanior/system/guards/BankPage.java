package com.eldanior.system.guards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.shop.CoinWallet;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Banque, ouverte par le banquier (NpcTrades.Metier.BANQUIER) : Npc/Banque.ui, generee par scripts/npcs/gen_banque_ui.py.
 * Refaite deux fois le 2026-11-24 a la demande de l'utilisateur (habillage Chapitres, puis « pas assez claire pour l'UX/UI ») :
 * a gauche les avoirs en grand (sac, banque, total, dette) et le message de la banque ; a droite trois onglets :
 *  - Deposer / Retirer : deux colonnes, chacune avec son sens ecrit, « combien » + piece, un apercu en direct (le champ et la
 *    liste previennent le serveur a chaque changement), le bouton et 4 raccourcis ; dessous, les pieces du sac ;
 *  - Change : une ligne par piece (Regrouper 10 -> 1 au-dessus, Casser 1 -> 10 en dessous), Tout regrouper ;
 *  - Pret : plafond ou dette, « combien », apercu de ce qui sera du, Emprunter (second clic pour confirmer) ou Rembourser ;
 *    les regles a cote (BankLoans : 3 fois l'avoir, 20 %, 7 jours, Criminel etoile en cas de defaut).
 *
 * Deux soldes : la bourse (pieces portees, CoinWallet) et le compte (PlayerLevelData.money, en cuivres). Ce qui ne rentre
 * pas dans le sac au retrait reste au compte.
 */
public class BankPage extends InteractiveCustomUIPage<BankPage.BankEventData> {

    /** Pieces, de la plus grosse a la plus petite (Imperiale ... Cuivre). */
    private static final List<String> COINS = new ArrayList<>(CoinWallet.COINS.keySet());
    private static final String[] LABELS = {"Impériale", "Astrale", "Zénith", "Diamant", "Or", "Argent", "Cuivre"};
    /** Raccourcis (Doit correspondre a gen_banque_ui.py) : 0 = tout, puis 1 or, 1 diamant, 1 zenith (en cuivres). */
    private static final long[] QUICK = {-1, 100, 1_000, 10_000};
    private static final int TABS = 3, QUICKS = 4;
    private static final String GOLD = "#f2c45e", GREEN = "#8fd18f", RED = "#e0796b", ORANGE = "#f0a85a", BODY = "#b8c2d0";
    private static final String GOLD_ID = "Elda_Gold_Coins";
    private static final long CONFIRM_MS = 15_000;
    private static final String[][] TAB_TEXT = {
            {"Déposer / Retirer", "Mettre à l'abri ou reprendre son argent", "Elda_Gold_Coins"},
            {"Change", "Regrouper ou casser des pièces", "Elda_Silver_Coins"},
            {"Prêt", "Emprunter ou rembourser", "Elda_Imperial_Coins"}};

    private final String title, sub;
    private String message = "";
    private boolean error;
    /** Onglet : 0 Deposer / Retirer, 1 Change, 2 Pret. */
    private int tab;
    /** Sommes des champs, en cuivres (2026-11-24 : somme exacte au lieu de « nombre + piece »), gardees pour les apercus. */
    private long depN = 100, wdN = 100, loanN = 1_000;
    /** Emprunt en attente de confirmation (second clic sur Emprunter, meme somme, dans CONFIRM_MS). */
    private long pendingBorrow;
    private long pendingAt;

    public BankPage(@Nonnull PlayerRef playerRef, String title, String sub, String theme) {
        super(playerRef, CustomPageLifetime.CanDismiss, BankEventData.CODEC);
        this.title = title;
        this.sub = sub;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Npc/Banque.ui");
        ui.set("#BqTitle.Text", title);
        ui.set("#BqSub.Text", sub != null ? sub : "");
        if (item(GOLD_ID)) ui.set("#BqEmblem.ItemId", GOLD_ID);
        for (int i = 0; i < TABS; i++) {
            if (item(TAB_TEXT[i][2])) ui.set("#BqTabIcon" + i + ".ItemId", TAB_TEXT[i][2]);
            ui.set("#BqTabLabel" + i + ".Text", TAB_TEXT[i][0]);
            ui.set("#BqTabSub" + i + ".Text", TAB_TEXT[i][1]);
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BqTab" + i, EventData.of("Action", "tab").append("Index", String.valueOf(i)), false);
        }
        String[][] fields = {{"BqDep", "dep"}, {"BqWd", "wd"}, {"BqLoan", "loan"}};
        long[] start = {depN, wdN, loanN};
        for (int f = 0; f < fields.length; f++) {
            String p = fields[f][0];
            ui.set("#" + p + "Amount.Value", (int) start[f]);
            events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#" + p + "Amount", EventData.of("Action", "field")
                    .append("Index", fields[f][1]).append("@Amount", "#" + p + "Amount.Value"), false);
        }
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BqDepGo", EventData.of("Action", "deposit")
                .append("@Amount", "#BqDepAmount.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BqWdGo", EventData.of("Action", "withdraw")
                .append("@Amount", "#BqWdAmount.Value"), false);
        for (int i = 0; i < QUICKS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BqDepQ" + i, EventData.of("Action", "depQuick").append("Index", String.valueOf(i)), false);
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BqWdQ" + i, EventData.of("Action", "wdQuick").append("Index", String.valueOf(i)), false);
        }
        for (int i = 0; i < COINS.size(); i++) {
            if (item(COINS.get(i))) ui.set("#BqSacIcon" + i + ".ItemId", COINS.get(i));
            ui.set("#BqSacName" + i + ".Text", LABELS[i]);
        }
        // Change : ligne r = echange entre la petite piece COINS[6 - r] et la grosse COINS[5 - r] (cuivre/argent en haut)
        for (int r = 0; r < COINS.size() - 1; r++) {
            int small = COINS.size() - 1 - r, big = small - 1;
            String s = COINS.get(small), b = COINS.get(big);
            if (item(s)) ui.set("#BqChgSmallIcon" + r + ".ItemId", s);
            if (item(b)) ui.set("#BqChgBigIcon" + r + ".ItemId", b);
            ui.set("#BqChgSmallName" + r + ".Text", LABELS[small]);
            ui.set("#BqChgBigName" + r + ".Text", LABELS[big]);
            ui.set("#BqUp" + r + ".Text", "10 " + CoinWallet.coinName(s, 10) + "  →  1 " + CoinWallet.coinName(b, 1));
            ui.set("#BqDown" + r + ".Text", "1 " + CoinWallet.coinName(b, 1) + "  →  10 " + CoinWallet.coinName(s, 10));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BqUp" + r, EventData.of("Action", "up").append("Index", String.valueOf(small)), false);
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BqDown" + r, EventData.of("Action", "down").append("Index", String.valueOf(big)), false);
        }
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BqTidy", EventData.of("Action", "tidy"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BqBorrow", EventData.of("Action", "borrow")
                .append("@Amount", "#BqLoanAmount.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BqRepay", EventData.of("Action", "repay")
                .append("@Amount", "#BqLoanAmount.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BqRepayAll", EventData.of("Action", "repayAll"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BqClose", EventData.of("Action", "close"), false);
        fill(ui, ref, store);
    }

    private static boolean item(String id) {
        return id != null && com.hypixel.hytale.server.core.asset.type.item.config.Item.getAssetMap().getAsset(id) != null;
    }

    /** Somme exacte, toutes les pieces : « 1 diamant 2 or 5 argent 3 cuivres » (CoinWallet.format s'arrete a deux). */
    private static String exact(long value) {
        if (value <= 0) return "0 cuivre";
        StringBuilder sb = new StringBuilder();
        for (String id : COINS) {
            long n = value / CoinWallet.COINS.get(id);
            if (n == 0) continue;
            value -= n * CoinWallet.COINS.get(id);
            if (sb.length() > 0) sb.append(", ");
            sb.append(n).append(' ').append(CoinWallet.coinName(id, n));
        }
        return sb.toString();
    }

    private static String cuivres(long n) {
        return String.format(java.util.Locale.FRANCE, "%,d", n).replace('\u202f', ' ').replace('\u00a0', ' ') + (n > 1 ? " cuivres" : " cuivre");
    }

    private void fill(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        long purse = player != null ? CoinWallet.value(player) : 0, account = data != null ? data.getMoney() : 0;
        BankLoans.Loan loan = BankLoans.get(playerRef.getUuid());

        // ---- avoirs ----
        ui.set("#BqPurse.Text", exact(purse));
        ui.set("#BqAccount.Text", exact(account));
        if (item(CoinWallet.coinFor(Math.max(1, purse)))) ui.set("#BqPurseIcon.ItemId", CoinWallet.coinFor(Math.max(1, purse)));
        if (item(CoinWallet.coinFor(Math.max(1, account)))) ui.set("#BqAccountIcon.ItemId", CoinWallet.coinFor(Math.max(1, account)));
        ui.set("#BqTotal.Text", "Total : " + exact(purse + account));
        ui.set("#BqDebtBox.Visible", loan != null);
        if (loan != null) {
            ui.set("#BqDebt.Text", CoinWallet.format(loan.due));
            ui.set("#BqDebtTime.Text", loan.defaulted ? "Échéance passée : vous êtes Criminel étoilé"
                    : "À rendre dans " + BankLoans.duration(loan.deadline - System.currentTimeMillis()));
            ui.set("#BqDebtTime.Style.TextColor", loan.defaulted ? RED : BODY);
        }

        // ---- onglets ----
        for (int i = 0; i < TABS; i++) {
            ui.set("#BqPanel" + i + ".Visible", i == tab);
            ui.set("#BqTabGlow" + i + ".Visible", i == tab);
            ui.set("#BqTabOn" + i + ".Visible", i == tab);
            ui.set("#BqTabLabel" + i + ".Style.TextColor", i == tab ? GOLD : "#96a9be");
        }
        ui.set("#BqQuote.Text", "« " + switch (tab) {
            case 0 -> "Tapez la somme exacte en cuivres (100 cuivres = 1 or), puis Déposer pour la mettre à l'abri, ou Retirer pour la reprendre.";
            case 1 -> "Trop de petites pièces ? Regroupez-les. Besoin de monnaie ? Cassez une grosse pièce.";
            default -> loan == null ? "La banque prête jusqu'à trois fois ce que vous possédez. Lisez bien les règles."
                    : "Vous avez une dette : remboursez-la avant l'échéance.";
        } + " »");

        // ---- deposer / retirer : sommes exactes en cuivres ----
        long dep = depN, wd = wdN;
        boolean depOk = dep > 0 && dep <= purse, wdOk = wd > 0 && wd <= account;
        ui.set("#BqDepWorth.Text", "= " + exact(dep));
        ui.set("#BqWdWorth.Text", "= " + exact(wd));
        ui.set("#BqDepPreview.Text", dep <= 0 ? "Tapez une somme d'au moins 1 cuivre."
                : depOk ? "Il restera " + exact(purse - dep) + " dans le sac ; la banque passera à " + exact(account + dep) + "."
                : "Votre sac ne contient que " + exact(purse) + " : il manque " + exact(dep - purse) + ".");
        ui.set("#BqDepPreview.Style.TextColor", depOk ? GREEN : RED);
        ui.set("#BqWdPreview.Text", wd <= 0 ? "Tapez une somme d'au moins 1 cuivre."
                : wdOk ? "Il restera " + exact(account - wd) + " à la banque."
                : "La banque ne garde que " + exact(account) + " pour vous : il manque " + exact(wd - account) + ".");
        ui.set("#BqWdPreview.Style.TextColor", wdOk ? GREEN : RED);
        ui.set("#BqDepGo.Text", dep > 0 ? "Déposer " + cuivres(dep) : "Déposer");
        ui.set("#BqWdGo.Text", wd > 0 ? "Retirer " + cuivres(wd) : "Retirer");
        int[] have = new int[COINS.size()];
        for (int i = 0; i < COINS.size(); i++) {
            have[i] = player != null ? CoinWallet.count(player, COINS.get(i)) : 0;
            ui.set("#BqSacCount" + i + ".Text", "x " + have[i]);
            ui.set("#BqSacCount" + i + ".Style.TextColor", have[i] > 0 ? GOLD : "#56677a");
        }

        // ---- change : une ligne par paire de pieces voisines ; sans assez de pieces, un texte gris remplace le bouton ----
        for (int r = 0; r < COINS.size() - 1; r++) {
            int small = COINS.size() - 1 - r, big = small - 1;
            ui.set("#BqChgSmallCount" + r + ".Text", "x " + have[small] + " dans le sac");
            ui.set("#BqChgSmallCount" + r + ".Style.TextColor", have[small] > 0 ? BODY : "#56677a");
            ui.set("#BqChgBigCount" + r + ".Text", "x " + have[big] + " dans le sac");
            ui.set("#BqChgBigCount" + r + ".Style.TextColor", have[big] > 0 ? BODY : "#56677a");
            boolean up = have[small] >= 10, down = have[big] >= 1;
            ui.set("#BqUp" + r + ".Visible", up);
            ui.set("#BqUpNo" + r + ".Visible", !up);
            ui.set("#BqUpNo" + r + ".Text", "Il faut 10 " + CoinWallet.coinName(COINS.get(small), 10) + " (vous en avez " + have[small] + ")");
            ui.set("#BqDown" + r + ".Visible", down);
            ui.set("#BqDownNo" + r + ".Visible", !down);
            ui.set("#BqDownNo" + r + ".Text", "Aucune pièce " + (big == 4 ? "d'or" : big == 5 ? "d'argent" : "de " + LABELS[big].toLowerCase()) + " à casser");
        }

        // ---- pret ----
        long ceiling = BankLoans.ceiling(purse + account), asked = loanN;
        ui.set("#BqLoanWorth.Text", "= " + exact(asked));
        ui.set("#BqBorrowBox.Visible", loan == null);
        ui.set("#BqRepayBox.Visible", loan != null);
        if (loan == null) {
            boolean armed = pendingBorrow > 0 && System.currentTimeMillis() - pendingAt <= CONFIRM_MS;
            ui.set("#BqLoanHead.Text", "Emprunter");
            ui.set("#BqLoanBigKey.Text", "Vous pouvez emprunter jusqu'à");
            ui.set("#BqLoanBig.Text", cuivres(ceiling));
            ui.set("#BqLoanBig.Style.TextColor", GOLD);
            ui.set("#BqLoanSmall.Text", "Soit " + exact(ceiling) + " : 3 fois ce que vous possédez (" + exact(purse + account)
                    + "). L'argent emprunté est versé à la banque.");
            boolean ok = asked > 0 && asked <= ceiling;
            ui.set("#BqLoanPreview.Text", asked <= 0 ? "Tapez une somme d'au moins 1 cuivre."
                    : ok ? "Vous recevez " + cuivres(asked) + " et devrez rendre " + cuivres(BankLoans.dueFor(asked))
                            + " (+ 20 %) dans 7 jours."
                    : "C'est plus que votre plafond de " + cuivres(ceiling) + ".");
            ui.set("#BqLoanPreview.Style.TextColor", ok ? (armed ? ORANGE : BODY) : RED);
            ui.set("#BqBorrow.Text", armed ? "Confirmer : emprunter " + cuivres(pendingBorrow) : "Emprunter " + cuivres(Math.max(0, asked)));
        } else {
            ui.set("#BqLoanHead.Text", "Votre prêt");
            ui.set("#BqLoanBigKey.Text", loan.defaulted ? "Dette en retard" : "Il vous reste à rendre");
            ui.set("#BqLoanBig.Text", cuivres(loan.due));
            ui.set("#BqLoanBig.Style.TextColor", loan.defaulted ? RED : ORANGE);
            ui.set("#BqLoanSmall.Text", "Soit " + exact(loan.due) + ". Emprunté : " + cuivres(loan.principal) + ". " + (loan.defaulted
                    ? "L'échéance est passée : vous êtes Criminel étoilé." : "Échéance dans " + BankLoans.duration(loan.deadline - System.currentTimeMillis()) + "."));
            long pay = Math.min(Math.min(asked, loan.due), account);
            boolean ok = asked > 0 && account > 0;
            ui.set("#BqLoanPreview.Text", asked <= 0 ? "Tapez une somme d'au moins 1 cuivre."
                    : account <= 0 ? "Votre compte est vide : déposez d'abord des pièces (onglet Déposer / Retirer)."
                    : "Vous remboursez " + cuivres(pay) + " depuis votre compte (" + cuivres(account) + "). Il restera "
                            + cuivres(loan.due - pay) + " à rendre.");
            ui.set("#BqLoanPreview.Style.TextColor", ok ? GREEN : RED);
            ui.set("#BqRepay.Text", "Rembourser " + cuivres(Math.max(0, pay)));
        }

        // ---- message ----
        ui.set("#BqMsgBox.Visible", !message.isEmpty());
        ui.set("#BqMsg.Text", message);
        ui.set("#BqMsg.Style.TextColor", error ? RED : GREEN);
        ui.set("#BqMsgBar.Background", error ? RED : GREEN);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull BankEventData data) {
        try {
            if ("close".equals(data.action)) { close(); return; }
            handle(ref, store, data);
        } catch (Throwable e) {                                                       // toujours repondre, et garder la trace
            EldaniorLogger.error("[Banque] action " + data.action, e);
            message = "Une erreur est survenue (voir le journal du serveur).";
            error = true;
        }
        UICommandBuilder ui = new UICommandBuilder();
        fill(ui, ref, store);
        sendUpdate(ui);
    }

    private void handle(Ref<EntityStore> ref, Store<EntityStore> store, BankEventData d) {
        String action = d.action == null ? "" : d.action;
        int i = -1;
        try { i = Integer.parseInt(d.index); } catch (Exception ignored) { }
        long n = d.amount != null ? Math.max(0, (long) d.amount) : 0;
        switch (action) {
            case "tab" -> {
                if (i >= 0 && i < TABS && i != tab) { tab = i; message = ""; }
                return;
            }
            case "field" -> {                                                          // apercu en direct
                switch (d.index == null ? "" : d.index) {
                    case "dep" -> { if (d.amount != null) depN = n; }
                    case "wd" -> { if (d.amount != null) wdN = n; }
                    case "loan" -> { if (d.amount != null) loanN = n; }
                    default -> { }
                }
                return;
            }
            default -> { }
        }
        message = "";
        error = false;
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (player == null || data == null) return;
        if (!"borrow".equals(action)) pendingBorrow = 0;
        switch (action) {
            case "deposit" -> {
                if (d.amount != null) depN = n;
                deposit(ref, store, player, data, depN);
            }
            case "withdraw" -> {
                if (d.amount != null) wdN = n;
                withdraw(ref, store, player, data, wdN);
            }
            case "depQuick" -> { if (i >= 0 && i < QUICKS) deposit(ref, store, player, data, QUICK[i] < 0 ? CoinWallet.value(player) : QUICK[i]); }
            case "wdQuick" -> { if (i >= 0 && i < QUICKS) withdraw(ref, store, player, data, QUICK[i] < 0 ? data.getMoney() : QUICK[i]); }
            case "up" -> regroup(player, i);
            case "down" -> breakCoin(player, i);
            case "tidy" -> tidy(player);
            case "borrow" -> {
                if (d.amount != null) loanN = n;
                borrow(ref, store, player, data, loanN);
            }
            case "repay" -> {
                if (d.amount != null) loanN = n;
                repay(ref, store, data, loanN);
            }
            case "repayAll" -> repay(ref, store, data, Long.MAX_VALUE);
            default -> { }
        }
    }

    private void fail(String text) { message = text; error = true; }

    /** Emprunter : un premier clic rappelle ce qui sera du et les consequences, le second (meme somme) accorde le pret. */
    private void borrow(Ref<EntityStore> ref, Store<EntityStore> store, Player player, PlayerLevelData data, long amount) {
        long now = System.currentTimeMillis();
        long wealth = CoinWallet.value(player) + data.getMoney();
        if (BankLoans.get(playerRef.getUuid()) == null && amount > 0 && amount <= BankLoans.ceiling(wealth)
                && (pendingBorrow != amount || now - pendingAt > CONFIRM_MS)) {
            pendingBorrow = amount;
            pendingAt = now;
            message = "Cliquez encore sur Emprunter pour confirmer : vous devrez " + exact(BankLoans.dueFor(amount))
                    + " dans 7 jours, sinon Criminel étoilé.";
            error = true;
            return;
        }
        pendingBorrow = 0;
        PlayerLevelData copy = (PlayerLevelData) data.clone();
        String refused = BankLoans.borrow(playerRef, copy, wealth, amount);
        if (refused != null) { fail(refused); return; }
        store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), copy);
        message = "Prêt accordé : " + exact(amount) + " versés au compte. À rendre : "
                + exact(BankLoans.dueFor(amount)) + " avant 7 jours.";
    }

    private void repay(Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData data, long amount) {
        pendingBorrow = 0;
        BankLoans.Loan loan = BankLoans.get(playerRef.getUuid());
        if (loan == null) { fail("Vous ne devez rien à la banque."); return; }
        if (amount <= 0) { fail("Indiquez la somme à rembourser."); return; }
        if (data.getMoney() <= 0) { fail("Votre compte est vide : déposez d'abord des pièces (onglet Déposer / Retirer)."); return; }
        PlayerLevelData copy = (PlayerLevelData) data.clone();
        boolean wasDefaulted = loan.defaulted;
        long paid = BankLoans.repay(playerRef, copy, amount);
        if (paid <= 0) { fail("Rien n'a pu être remboursé."); return; }
        store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), copy);
        BankLoans.Loan left = BankLoans.get(playerRef.getUuid());
        message = left == null ? "Dette soldée (" + exact(paid) + ")." + (wasDefaulted ? " La banque lève sa prime." : "")
                : "Remboursé : " + exact(paid) + ". Il reste " + exact(left.due) + ".";
    }

    private void deposit(Ref<EntityStore> ref, Store<EntityStore> store, Player player, PlayerLevelData data, long amount) {
        if (amount <= 0) { fail("Indiquez un montant à déposer."); return; }
        long purse = CoinWallet.value(player);
        if (purse < amount) { fail("Votre bourse ne contient que " + exact(purse) + "."); return; }
        long change = CoinWallet.pay(player, amount);
        if (change < 0) { fail("Votre bourse ne suffit pas."); return; }
        PlayerLevelData copy = (PlayerLevelData) data.clone();
        copy.addMoney(amount);
        store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), copy);
        com.eldanior.system.quest.QuestManager.onBankDeposit(playerRef.getUuid(), amount);        // quetes d'argent (2026-11-14)
        message = "Déposé : " + exact(amount) + ". Compte : " + exact(copy.getMoney()) + ".";
    }

    private void withdraw(Ref<EntityStore> ref, Store<EntityStore> store, Player player, PlayerLevelData data, long amount) {
        if (amount <= 0) { fail("Indiquez un montant à retirer."); return; }
        if (data.getMoney() < amount) { fail("Votre compte ne contient que " + exact(data.getMoney()) + "."); return; }
        PlayerLevelData copy = (PlayerLevelData) data.clone();
        copy.removeMoney(amount);
        long back = CoinWallet.receiveOrReturn(player, amount);                        // sac plein : le reste reste au compte
        if (back > 0) copy.addMoney(back);
        store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), copy);
        if (back >= amount) { fail("Votre sac est plein : rien n'a été retiré."); return; }
        com.eldanior.system.quest.QuestManager.onBankWithdraw(playerRef.getUuid(), amount - back);  // quetes d'argent (2026-11-14)
        message = "Retiré : " + exact(amount - back) + (back > 0 ? " (sac plein : " + exact(back) + " restent au compte)" : "") + ".";
    }

    /** 10 pieces de COINS[i] -> 1 piece de COINS[i-1] (autant de fois que possible). */
    private void regroup(Player player, int i) {
        if (i <= 0 || i >= COINS.size()) return;
        String coin = COINS.get(i), up = COINS.get(i - 1);
        int have = CoinWallet.count(player, coin), lots = have / 10;
        if (lots <= 0) { fail("Il faut au moins 10 " + CoinWallet.coinName(coin, 10) + " pour en faire 1 " + CoinWallet.coinName(up, 1) + "."); return; }
        CoinWallet.takeCoins(player, coin, lots * 10);
        long back = CoinWallet.receiveOrReturn(player, lots * CoinWallet.COINS.get(up));
        if (back > 0) CoinWallet.receiveOrReturn(player, back);                          // ne peut que reussir : on vient de liberer de la place
        message = lots * 10 + " " + CoinWallet.coinName(coin, 10) + " -> " + lots + " " + CoinWallet.coinName(up, lots) + ".";
    }

    /** 1 piece de COINS[i] -> 10 pieces de COINS[i+1]. */
    private void breakCoin(Player player, int i) {
        if (i < 0 || i >= COINS.size() - 1) return;
        String coin = COINS.get(i), down = COINS.get(i + 1);
        if (CoinWallet.count(player, coin) <= 0) { fail("Vous n'avez pas de pièce " + LABELS[i].toLowerCase() + " à casser."); return; }
        if (!player.getInventory().getCombinedBackpackStorageHotbar().canAddItemStack(new com.hypixel.hytale.server.core.inventory.ItemStack(down, 10))) {
            fail("Votre sac est plein."); return;
        }
        CoinWallet.takeCoins(player, coin, 1);
        player.getInventory().getCombinedBackpackStorageHotbar().addItemStack(new com.hypixel.hytale.server.core.inventory.ItemStack(down, 10));
        message = "1 " + CoinWallet.coinName(coin, 1) + " -> 10 " + CoinWallet.coinName(down, 10) + ".";
    }

    /** Toute la bourse reconvertie dans le moins de pieces possible. */
    private void tidy(Player player) {
        long purse = CoinWallet.value(player);
        if (purse <= 0) { fail("Votre bourse est vide."); return; }
        for (String c : COINS) CoinWallet.takeCoins(player, c, CoinWallet.count(player, c));
        long back = CoinWallet.receiveOrReturn(player, purse);
        if (back > 0) EldaniorLogger.warn("[Banque] regroupement : " + back + " cuivres non ranges pour " + playerRef.getUsername());
        message = "Bourse regroupée : " + exact(purse) + ".";
    }

    public static class BankEventData {
        public static final BuilderCodec<BankEventData> CODEC = BuilderCodec.builder(BankEventData.class, BankEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("Index", Codec.STRING), (d, v) -> d.index = v, d -> d.index).add()
                .append(new KeyedCodec<>("@Amount", Codec.INTEGER), (d, v) -> d.amount = v, d -> d.amount).add()
                .build();
        public String action, index;
        public Integer amount;
    }
}
