package com.eldanior.system.boards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;

/**
 * Fenetre d'un tableau (Board/Board.ui : carte large au style de la fenetre de recrutement), commune aux tableaux de prime et de
 * guilde : embleme, titre, lieu, replique, tuiles de chiffres, onglets ; a gauche la liste paginee (rang, nom, detail,
 * valeur, bouton), a droite la fiche de la ligne choisie (ou l'aide de l'onglet) avec montant et cout optionnels, boutons et
 * message de retour ; bourse, bouton d'action du bas. Chaque clic met la fenetre a jour sans la fermer. Le contenu vient des
 * sous-classes (BountyBoardScreen, GuildBoardScreen).
 */
public abstract class BoardScreen extends InteractiveCustomUIPage<BoardScreen.BoardEventData> {

    /** Onglets, tuiles et lignes par page prevus dans Board/Board.ui. */
    static final int TABS = 3, STATS = 4, ROWS = 7;
    static final String GREEN = "#00e7b6", RED = "#cc3361", GOLD = "#E8A93B", MUTED = "#6f8296";

    /** Tuile de chiffre. */
    public record Stat(String value, String label) {}

    /**
     * Ligne de la liste. rank : a gauche (1, 2, 3 en or, argent, bronze) ; valueColor null : or ; details : oeil qui ouvre sa
     * fiche ; mine : ligne du joueur (verte) ; selected : ligne dont la fiche est ouverte (bleue).
     */
    public record Row(String rank, String name, String meta, String value, String valueColor, boolean details, boolean mine,
                      boolean selected) {}

    /**
     * Onglet : titre de section, lignes (toutes : la fenetre les pagine), texte si vide, note et bouton du bas (null : aucun),
     * aide affichee dans la fiche quand aucune ligne n'est choisie.
     */
    public record Tab(String section, List<Row> rows, String empty, String footer, String action, String help) {}

    /**
     * Fiche (panneau de droite) : titre, sous-titre, texte ; fieldLabel null : pas de montant (sinon libelle, cout et sa
     * couleur) ; confirm / cancel null : pas de bouton ; names : champs nom et tag (fonder une guilde).
     */
    public record Side(String title, String subtitle, String text, String fieldLabel, String cost, String costColor,
                       String confirm, String cancel, boolean names) {
        public Side(String title, String subtitle, String text, String fieldLabel, String cost, String costColor,
                    String confirm, String cancel) {
            this(title, subtitle, text, fieldLabel, cost, costColor, confirm, cancel, false);
        }

        public static Side info(String title, String subtitle, String text) {
            return new Side(title, subtitle, text, null, null, null, null, "Fermer");
        }
    }

    protected final BoardType type;
    protected final String place;
    protected int tab, page;
    private String status, statusColor;
    /** Nom et tag tapes dans la fiche (lus au clic de confirmation). */
    protected String inputName, inputTag;
    /** Montant a ecrire dans le champ a la prochaine mise a jour (a l'ouverture du formulaire seulement). */
    private Long pushAmount;

    protected BoardScreen(@Nonnull PlayerRef playerRef, BoardType type, String place) {
        super(playerRef, CustomPageLifetime.CanDismiss, BoardEventData.CODEC);
        this.type = type;
        this.place = place;
    }

    // ===== Contenu (sous-classes) =====

    protected abstract List<Stat> stats(Ref<EntityStore> ref, Store<EntityStore> store);

    protected abstract Tab tab(int index, Ref<EntityStore> ref, Store<EntityStore> store);

    /** Fiche de la ligne choisie (null : l'aide de l'onglet). */
    protected Side side(Ref<EntityStore> ref, Store<EntityStore> store) { return null; }

    /** Bouton d'une ligne (index dans toute la liste de l'onglet). */
    protected void onRow(int index, Ref<EntityStore> ref, Store<EntityStore> store) {}

    /** Bouton d'action du bas. */
    protected void onAction(Ref<EntityStore> ref, Store<EntityStore> store) {}

    /** Montant tape dans la fiche (a chaque changement). */
    protected void onAmount(long amount) {}

    protected void onConfirm(long amount, Ref<EntityStore> ref, Store<EntityStore> store) {}

    protected void onCancel() {}

    /** Onglet change : les sous-classes oublient leurs selections. */
    protected void onTab() {}

    // ===== Outils des sous-classes =====

    protected void ok(String msg) { status = msg; statusColor = GREEN; }

    protected void error(String msg) { status = msg; statusColor = RED; }

    /** Le champ de la fiche prendra cette valeur a la prochaine mise a jour. */
    protected void setAmount(long amount) { pushAmount = amount; }

    protected static PlayerLevelData data(Ref<EntityStore> ref, Store<EntityStore> store) {
        return store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
    }

    /** 12 500 (espaces fines comme separateur). */
    protected static String gold(long amount) {
        return String.format(Locale.FRANCE, "%,d", amount).replace(' ', ' ').replace(' ', ' ');
    }

    // ===== Affichage =====

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events,
                      @Nonnull Store<EntityStore> store) {
        ui.append("Board/Board.ui");
        ui.set("#BoardEmblemPrime.Visible", type == BoardType.PRIME);
        ui.set("#BoardEmblemGuilde.Visible", type != BoardType.PRIME);
        ui.set("#BoardTitle.Text", type.getLabel());
        ui.set("#BoardPlace.Text", "•  " + (place != null ? place : "Hors des villes"));
        ui.set("#BoardQuote.Text", "« " + type.getQuote() + " »");
        ui.set("#BoardNote.Text", type == BoardType.PRIME ? "Affiché par ordre de la couronne" : "Registre des guildes du royaume");
        List<String> tabs = type.getTabs();
        for (int i = 0; i < TABS; i++) {
            boolean shown = i < tabs.size();
            ui.set("#BoardTabBox" + i + ".Visible", shown);
            if (!shown) continue;
            ui.set("#BoardTab" + i + ".Text", tabs.get(i));
            bind(events, "#BoardTab" + i, EventData.of("Action", "tab").append("Index", String.valueOf(i)));
        }
        for (int i = 0; i < ROWS; i++)
            bind(events, "#BoardRowBtn" + i, EventData.of("Action", "row").append("Index", String.valueOf(i)));
        bind(events, "#BoardPrev", EventData.of("Action", "prev"));
        bind(events, "#BoardNext", EventData.of("Action", "next"));
        bind(events, "#BoardBtnOk", EventData.of("Action", "action"));
        bind(events, "#BoardSideOk", EventData.of("Action", "confirm").append("@Amount", "#BoardAmount.Value")
                .append("@Name", "#BoardInputName.Value").append("@Tag", "#BoardInputTag.Value"));
        bind(events, "#BoardSideCancel", EventData.of("Action", "cancel"));
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#BoardAmount",
                EventData.of("Action", "amount").append("@Amount", "#BoardAmount.Value"), false);
        show(ui, ref, store);
    }

    private static void bind(UIEventBuilder events, String id, EventData data) {
        events.addEventBinding(CustomUIEventBindingType.Activating, id, data);
    }

    /** Tout le contenu variable : chiffres, onglet, liste, fiche, message, bourse. */
    private void show(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        List<Stat> stats = stats(ref, store);
        for (int i = 0; i < STATS; i++) {
            Stat s = i < stats.size() ? stats.get(i) : new Stat("—", "");
            ui.set("#BoardStatValue" + i + ".Text", s.value());
            ui.set("#BoardStatLabel" + i + ".Text", s.label());
        }
        for (int i = 0; i < type.getTabs().size() && i < TABS; i++) ui.set("#BoardTabLine" + i + ".Visible", i == tab);
        Tab content = tab(tab, ref, store);
        ui.set("#BoardSection.Text", content.section());
        showRows(ui, content);
        Side side = side(ref, store);
        showSide(ui, side != null ? side : new Side(content.section(), type.getLabel(), content.help(), null, null, null, null, null),
                side != null);
        ui.set("#BoardStatus.Visible", status != null);
        if (status != null) {
            ui.set("#BoardStatus.Text", status);
            ui.set("#BoardStatus.Style.TextColor", statusColor);
        }
        ui.set("#BoardFooter.Text", content.footer() != null ? content.footer() : "");
        String action = content.action();
        ui.set("#BoardBtnBox.Visible", action != null);
        if (action != null) ui.set("#BoardBtnOk.Text", action);
        PlayerLevelData data = data(ref, store);
        ui.set("#BoardMoney.Text", gold(data != null ? data.getMoney() : 0) + " or");
    }

    private void showRows(UICommandBuilder ui, Tab content) {
        List<Row> rows = content.rows();
        int pages = Math.max(1, (rows.size() + ROWS - 1) / ROWS);
        page = Math.clamp(page, 0, pages - 1);
        ui.set("#BoardEmpty.Visible", rows.isEmpty());
        ui.set("#BoardEmpty.Text", content.empty() != null ? content.empty() : "");
        for (int i = 0; i < ROWS; i++) {
            int index = page * ROWS + i;
            boolean shown = index < rows.size();
            ui.set("#BoardRow" + i + ".Visible", shown);
            if (!shown) continue;
            Row r = rows.get(index);
            ui.set("#BoardRow" + i + ".Background", r.selected() ? "#1a2b40" : r.mine() ? "#1c2a1f" : "#101c28");
            ui.set("#BoardRowStrip" + i + ".Background", r.selected() ? "#7a9cc6" : r.mine() ? GREEN : "#e8b34f");
            ui.set("#BoardRowRank" + i + ".Text", r.rank() != null ? r.rank() : "");
            ui.set("#BoardRowRank" + i + ".Style.TextColor", rankColor(r.rank()));
            ui.set("#BoardRowName" + i + ".Text", r.name());
            ui.set("#BoardRowMeta" + i + ".Text", r.meta() != null ? r.meta() : "");
            ui.set("#BoardRowValue" + i + ".Text", r.value() != null ? r.value() : "");
            ui.set("#BoardRowValue" + i + ".Style.TextColor", r.valueColor() != null ? r.valueColor() : GOLD);
            ui.set("#BoardRowAct" + i + ".Visible", r.details());
        }
        ui.set("#BoardPager.Visible", pages > 1);
        ui.set("#BoardPage.Text", (page + 1) + " / " + pages);
    }

    /** Fiche : chosen (une ligne choisie : liseré bleu) ou l'aide de l'onglet (liseré doré). */
    private void showSide(UICommandBuilder ui, Side side, boolean chosen) {
        ui.set("#BoardSideStrip.Background", chosen ? "#7a9cc6" : "#e8b34f");
        ui.set("#BoardSideTitle.Text", side.title() != null ? side.title() : "");
        ui.set("#BoardSideSub.Text", side.subtitle() != null ? side.subtitle() : "");
        ui.set("#BoardSideText.Text", side.text() != null ? side.text() : "");
        ui.set("#BoardSideNames.Visible", side.names());
        boolean field = side.fieldLabel() != null;
        ui.set("#BoardSideField.Visible", field);
        if (field) {
            ui.set("#BoardSideFieldLabel.Text", side.fieldLabel());
            ui.set("#BoardSideCost.Text", side.cost() != null ? side.cost() : "");
            ui.set("#BoardSideCost.Style.TextColor", side.costColor() != null ? side.costColor() : GOLD);
        }
        ui.set("#BoardSideOkBox.Visible", side.confirm() != null);
        if (side.confirm() != null) ui.set("#BoardSideOk.Text", side.confirm());
        ui.set("#BoardSideCancelBox.Visible", side.cancel() != null);
        if (side.cancel() != null) ui.set("#BoardSideCancel.Text", side.cancel());
        if (field && pushAmount != null) {
            ui.set("#BoardAmount.Value", pushAmount.intValue());
            pushAmount = null;
        }
    }

    /** Podium : or, argent, bronze ; les autres en gris. */
    private static String rankColor(String rank) {
        return switch (rank != null ? rank : "") {
            case "1" -> "#F1C40F";
            case "2" -> "#C9D1DA";
            case "3" -> "#CD8A4E";
            default -> MUTED;
        };
    }

    // ===== Evenements =====

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull BoardEventData data) {
        if (data.action == null) return;
        boolean keepStatus = false;
        try {
            switch (data.action) {
                case "tab" -> {
                    int index = parse(data.index);
                    if (index < 0 || index >= type.getTabs().size() || index == tab) return;
                    tab = index;
                    page = 0;
                    onTab();
                }
                case "prev" -> page--;
                case "next" -> page++;
                case "row" -> {
                    int index = parse(data.index);
                    if (index < 0) return;
                    onRow(page * ROWS + index, ref, store);
                    keepStatus = true;
                }
                case "action" -> { onAction(ref, store); keepStatus = true; }
                case "amount" -> { onAmount(data.amount != null ? data.amount : 0); keepStatus = true; }
                case "confirm" -> {
                    inputName = data.name != null ? data.name.trim() : "";
                    inputTag = data.tag != null ? data.tag.trim() : "";
                    onConfirm(data.amount != null ? data.amount : 0, ref, store);
                    keepStatus = true;
                }
                case "cancel" -> onCancel();
                default -> { return; }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Boards] " + type + " " + data.action, e);
            error("Une erreur est survenue.");
            keepStatus = true;
        }
        if (!keepStatus) status = null;
        UICommandBuilder update = new UICommandBuilder();
        show(update, ref, store);
        this.sendUpdate(update);
    }

    private static int parse(String s) {
        try { return s != null ? Integer.parseInt(s) : -1; } catch (NumberFormatException e) { return -1; }
    }

    public static class BoardEventData {
        public static final BuilderCodec<BoardEventData> CODEC = BuilderCodec.builder(BoardEventData.class, BoardEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("Index", Codec.STRING), (d, v) -> d.index = v, d -> d.index).add()
                .append(new KeyedCodec<>("@Amount", Codec.INTEGER), (d, v) -> d.amount = v, d -> d.amount).add()
                .append(new KeyedCodec<>("@Name", Codec.STRING), (d, v) -> d.name = v, d -> d.name).add()
                .append(new KeyedCodec<>("@Tag", Codec.STRING), (d, v) -> d.tag = v, d -> d.tag).add()
                .build();
        public String action;
        public String index;
        public Integer amount;
        public String name, tag;
    }
}
