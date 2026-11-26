package com.eldanior.system.accueil;

import com.eldanior.system.classes.gui.ClassInfo;
import com.eldanior.system.config.EldaniorLogger;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Fenetre a cartes d'Eldanior (2026-11-25), sur Accueil/Accueil.ui (scripts/accueil/gen_accueil_ui.py) : bandeau Eldanior et
 * etapes en haut, carrousel de cartes illustrees a droite, detail de la carte designee a gauche (texte, points, passifs avec
 * leur effet, runes avec leur icone, bonus de caracteristiques, kit avec les icones des objets).
 * Deux usages : l'accueil des nouveaux joueurs (WelcomePage, plusieurs etapes) et le choix de classe de la statue
 * (classes.gui.ClassChoicePage, une seule etape, relance des evolutions).
 */
public abstract class CardsPage extends InteractiveCustomUIPage<CardsPage.Data> {

    // Doit correspondre a scripts/accueil/gen_accueil_ui.py
    public static final int CARDS = 3, NOTES = 5, STATS = 6, STEPS = 4, PASSIVES = 4, RUNES = 3, KIT = 14;
    /** Contours des cartes : « Base » = cadre d'origine (gris), « Blanc » = rarete Commune (Accueil/) ; les autres, cadres teintes des Chapitres. */
    public static final String[] TINTS = {"Base", "Blanc", "Or", "Bleu", "Violet", "Vert", "Rouge", "Cyan"};
    /** Illustrations des cartes (Accueil/ChapArt*.png, ou ../Quest/ChapArt*.png pour Dojo, Aube, Arene, Rumeurs). */
    public static final String[] ARTS = {"Royaume", "Grimoire", "Guerrier", "Mage", "Archer", "Assassin", "Marchand",
            "Runes", "Faille", "Guildes", "Banque", "Dojo", "Aube", "Arene", "Rumeurs"};
    private static final String[] STAT_LABELS = {"Force", "Vitalité", "Intellect", "Endurance", "Agilité", "Chance"};
    protected static final String GOLD = "#f2c45e", GREEN = "#7ed37e", GREY = "#77879d", TEXT = "#d1dae6", RED = "#e0574a";

    /** Passif affiche : nom, rang, effet. */
    public record Passive(String name, String rank, String desc) {}

    /** Une carte : illustration et titre sous la carte, puis tout le detail affiche a gauche. */
    public static final class Card {
        public final String key, art, title;
        public String titleColor = GOLD, state = "", mark = "", kicker = "", heading = "", tagline = "", story = "";
        public List<String> notes = List.of();
        public int[] stats;
        public String statHead = "Bonus de classe";
        public List<Passive> passives = List.of();
        public List<ClassInfo.RuneInfo> runes = List.of();
        public List<StarterKits.Entry> kit = List.of();
        public String kitHead = "Kit de départ";
        /** Contour propre a la carte (TINTS), null : celui de la page. Classes : selon la rarete (ClassCards.tint). */
        public String tint;
        /**
         * Illustration propre a la carte (2026-11-26) : document sans suffixe, ex. « Accueil/Classes/Art_berserker » ; on
         * ajoute dans #AccArtSlot<i> sa variante C (choisie) ou G (grisee). null : l'illustration commune {@link #art}.
         */
        public String artUi;

        public Card(String key, String art, String title) { this.key = key; this.art = art; this.title = title; }

        public Card text(String mark, String kicker, String heading, String tagline, String story) {
            this.mark = mark; this.kicker = kicker; this.heading = heading; this.tagline = tagline; this.story = story;
            return this;
        }

        public Card notes(String... n) { this.notes = List.of(n); return this; }
    }

    protected int focus = 0, offset = 0;
    protected String hint;

    protected CardsPage(@Nonnull PlayerRef playerRef, CustomPageLifetime lifetime) {
        super(playerRef, lifetime, Data.CODEC);
    }

    // ==================== A FOURNIR ====================

    protected abstract List<Card> cards();

    /** Etape affichee (0..STEPS-1), ou -1 sans barre d'etapes (alors headline() s'affiche a sa place). */
    protected int step() { return -1; }
    protected String stepName(int k) { return ""; }
    protected boolean stepReachable(int k) { return false; }
    protected boolean stepDone(int k) { return false; }
    protected String headline() { return ""; }
    protected String brandSub() { return "Royaume d'Eldanior  ·  Empire Luo"; }
    /** Teinte du contour des cartes (TINTS). */
    protected String tint() { return "Or"; }
    protected boolean chosen(Card c) { return false; }

    protected abstract String mainLabel(Card c);
    protected String backLabel() { return null; }        // null : bouton cache
    protected String altLabel() { return null; }
    protected abstract String defaultHint();

    /** Boutons : true si la fenetre a ete fermee (plus rien a envoyer). */
    protected abstract boolean onMain(Ref<EntityStore> ref, Store<EntityStore> store, Card c);
    protected boolean onBack(Ref<EntityStore> ref, Store<EntityStore> store) { return false; }
    protected boolean onAlt(Ref<EntityStore> ref, Store<EntityStore> store) { return false; }
    protected void onStep(int k) {}

    /** Carte designee apres un changement de contenu : la carte choisie s'il y en a une, sinon la premiere. */
    protected void resetFocus() {
        focus = 0;
        offset = 0;
        List<Card> cards = cards();
        for (int n = 0; n < cards.size(); n++) if (chosen(cards.get(n))) focus = n;
    }

    // ==================== AFFICHAGE ====================

    /** Document deja pose dans chaque #AccArtSlot (null : vide). */
    private final String[] slotShown = new String[CARDS];

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        java.util.Arrays.fill(slotShown, null);                                   // page neuve : emplacements vides
        ui.append("Accueil/Accueil.ui");
        for (int i = 0; i < CARDS; i++)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#AccCard" + i, EventData.of("Action", "card").append("Index", String.valueOf(i)), false);
        for (int k = 0; k < STEPS; k++)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#AccStep" + k, EventData.of("Action", "step").append("Index", String.valueOf(k)), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AccPrev", EventData.of("Action", "prev"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AccNext", EventData.of("Action", "next"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AccMain", EventData.of("Action", "main"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AccBack", EventData.of("Action", "back"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AccAlt", EventData.of("Action", "alt"), false);
        fill(ui);
    }

    protected void fill(UICommandBuilder ui) {
        List<Card> cards = cards();
        focus = Math.max(0, Math.min(focus, cards.size() - 1));
        offset = Math.max(0, Math.min(offset, Math.max(0, cards.size() - CARDS)));
        if (focus < offset) offset = focus;
        if (focus >= offset + CARDS) offset = focus - CARDS + 1;
        String tint = tint();

        // ---- bandeau et etapes ----
        ui.set("#AccBrandSub.Text", brandSub());
        int step = step();
        ui.set("#AccSteps.Visible", step >= 0);
        ui.set("#AccHeadline.Visible", step < 0);
        if (step < 0) ui.set("#AccHeadline.Text", headline());
        else for (int k = 0; k < STEPS; k++) {
            boolean cur = k == step, done = stepDone(k);
            ui.set("#AccStepLbl" + k + ".Text", stepName(k));
            ui.set("#AccStepLbl" + k + ".Style.TextColor", cur ? GOLD : stepReachable(k) ? TEXT : GREY);
            ui.set("#AccStepNum" + k + ".Text", String.valueOf(k + 1));
            ui.set("#AccStepOn" + k + ".Visible", cur || done);
            ui.set("#AccStepOff" + k + ".Visible", !(cur || done));
            ui.set("#AccStepBar" + k + ".Visible", cur);
        }

        // ---- carrousel ----
        for (int i = 0; i < CARDS; i++) {
            int n = offset + i;
            boolean shown = n < cards.size();
            ui.set("#AccCard" + i + ".Visible", shown);
            if (!shown) continue;
            Card c = cards.get(n);
            boolean sel = n == focus;
            String cardTint = c.tint != null ? c.tint : tint;
            boolean own = c.artUi != null;
            for (String art : ARTS) {
                ui.set("#AccArt" + art + "C" + i + ".Visible", !own && sel && art.equals(c.art));
                ui.set("#AccArt" + art + "G" + i + ".Visible", !own && !sel && art.equals(c.art));
            }
            String slotDoc = own ? c.artUi + (sel ? "C" : "G") + ".ui" : null;
            if (!java.util.Objects.equals(slotDoc, slotShown[i])) {               // ne renvoyer l'image que si elle change
                ui.clear("#AccArtSlot" + i);
                if (slotDoc != null) ui.append("#AccArtSlot" + i, slotDoc);
                slotShown[i] = slotDoc;
            }
            for (String t : TINTS) {
                ui.set("#AccFrameD" + i + t + ".Visible", !sel && t.equals(cardTint));
                ui.set("#AccFrameS" + i + t + ".Visible", sel && t.equals(cardTint));
                ui.set("#AccShards" + i + t + ".Visible", sel && t.equals(cardTint));
            }
            ui.set("#AccCardTitle" + i + ".Text", c.title);
            ui.set("#AccCardTitle" + i + ".Style.TextColor", c.titleColor);
            ui.set("#AccCardState" + i + ".Text", chosen(c) ? "Votre choix" : c.state);
        }
        ui.set("#AccPrev.Visible", offset > 0);
        ui.set("#AccNext.Visible", offset + CARDS < cards.size());
        ui.set("#AccCount.Text", cards.size() > CARDS ? (focus + 1) + " / " + cards.size() : "");

        // ---- detail ----
        Card c = cards.isEmpty() ? null : cards.get(focus);
        ui.set("#AccDetail.Visible", c != null);
        if (c != null) detail(ui, c);

        // ---- boutons ----
        String back = backLabel(), alt = altLabel();
        ui.set("#AccBack.Visible", back != null);
        if (back != null) ui.set("#AccBack.Text", back);
        ui.set("#AccAlt.Visible", alt != null);
        if (alt != null) ui.set("#AccAlt.Text", alt);
        ui.set("#AccMain.Visible", c != null);
        if (c != null) ui.set("#AccMain.Text", mainLabel(c));
        ui.set("#AccHint.Text", hint != null ? hint : defaultHint());
    }

    private void detail(UICommandBuilder ui, Card c) {
        ui.set("#AccMark.Text", c.mark);
        ui.set("#AccKicker.Text", c.kicker);
        ui.set("#AccTitle.Text", c.heading);
        ui.set("#AccTitle.Style.TextColor", c.titleColor);
        ui.set("#AccTagline.Text", c.tagline);
        ui.set("#AccStory.Text", c.story);
        for (int k = 0; k < NOTES; k++) {
            boolean row = k < c.notes.size();
            ui.set("#AccNote" + k + ".Visible", row);
            if (row) ui.set("#AccNoteText" + k + ".Text", c.notes.get(k));
        }
        // passifs
        ui.set("#AccPasHead.Visible", !c.passives.isEmpty());
        for (int k = 0; k < PASSIVES; k++) {
            boolean row = k < c.passives.size();
            ui.set("#AccPas" + k + ".Visible", row);
            if (!row) continue;
            Passive p = c.passives.get(k);
            ui.set("#AccPasName" + k + ".Text", p.name() + (p.rank() == null || p.rank().isEmpty() ? "" : "  ·  " + p.rank()));
            ui.set("#AccPasDesc" + k + ".Text", p.desc());
        }
        // runes
        ui.set("#AccRuneHead.Visible", !c.runes.isEmpty());
        for (int k = 0; k < RUNES; k++) {
            boolean row = k < c.runes.size();
            ui.set("#AccRune" + k + ".Visible", row);
            if (!row) continue;
            ClassInfo.RuneInfo r = c.runes.get(k);
            ui.set("#AccRuneIcon" + k + ".ItemId", r.itemId());
            ui.set("#AccRuneFrame" + k + ".Background", r.color());
            ui.set("#AccRuneName" + k + ".Text", r.name());
            ui.set("#AccRuneMeta" + k + ".Text", r.meta());
            ui.set("#AccRuneMeta" + k + ".Style.TextColor", r.color());
            ui.set("#AccRuneDesc" + k + ".Text", r.effect());
        }
        // caracteristiques
        boolean stats = c.stats != null;
        ui.set("#AccStatHead.Visible", stats);
        ui.set("#AccStats.Visible", stats);
        if (stats) {
            ui.set("#AccStatHeadText.Text", c.statHead);
            for (int k = 0; k < STATS; k++) {
                ui.set("#AccStatVal" + k + ".Text", "+" + c.stats[k]);
                ui.set("#AccStatVal" + k + ".Style.TextColor", c.stats[k] > 0 ? GOLD : GREY);
                ui.set("#AccStatLbl" + k + ".Text", STAT_LABELS[k]);
            }
        }
        // kit
        boolean kit = !c.kit.isEmpty();
        ui.set("#AccKitHead.Visible", kit);
        ui.set("#AccKitRow0.Visible", kit);
        ui.set("#AccKitRow1.Visible", c.kit.size() > KIT / 2);
        if (kit) ui.set("#AccKitHeadText.Text", c.kitHead);
        for (int k = 0; k < KIT; k++) {
            boolean slot = k < c.kit.size();
            ui.set("#AccKit" + k + ".Visible", slot);
            if (!slot) continue;
            StarterKits.Entry e = c.kit.get(k);
            ui.set("#AccKitIcon" + k + ".ItemId", e.itemId());
            ui.set("#AccKitQty" + k + ".Text", e.qty() > 1 ? String.valueOf(e.qty()) : "");
            ui.set("#AccKit" + k + ".TooltipText", e.label() + (e.qty() > 1 ? " ×" + e.qty() : ""));
        }
    }

    // ==================== ACTIONS ====================

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        try {
            if (handle(ref, store, data)) return;
        } catch (Throwable e) {
            EldaniorLogger.error("[Cartes] action " + data.action, e);
        }
        UICommandBuilder ui = new UICommandBuilder();
        fill(ui);
        sendUpdate(ui);
    }

    private boolean handle(Ref<EntityStore> ref, Store<EntityStore> store, Data d) {
        hint = null;
        List<Card> cards = cards();
        switch (d.action == null ? "" : d.action) {
            case "card" -> {
                int i = parse(d.index);
                if (i >= 0 && offset + i < cards.size()) focus = offset + i;
            }
            case "prev" -> { if (offset > 0) { offset--; focus = Math.min(focus, offset + CARDS - 1); } }
            case "next" -> { if (offset + CARDS < cards.size()) { offset++; focus = Math.max(focus, offset); } }
            case "step" -> {
                int k = parse(d.index);
                if (k >= 0 && k != step() && stepReachable(k)) onStep(k);
            }
            case "back" -> { return onBack(ref, store); }
            case "alt" -> { return onAlt(ref, store); }
            case "main" -> {
                if (cards.isEmpty()) return false;
                return onMain(ref, store, cards.get(Math.min(focus, cards.size() - 1)));
            }
            default -> { return false; }
        }
        return false;
    }

    protected static int parse(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }

    /** Copie modifiable d'une liste. */
    protected static <T> List<T> list(List<T> in) { return new ArrayList<>(in); }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("Index", Codec.STRING), (d, v) -> d.index = v, d -> d.index).add()
                .build();
        public String action, index;
    }
}
