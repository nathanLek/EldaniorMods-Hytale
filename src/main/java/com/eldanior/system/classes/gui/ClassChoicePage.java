package com.eldanior.system.classes.gui;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.accueil.CardsPage;
import com.eldanior.system.accueil.ClassCards;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Choix de classe de la statue (2026-11-25), sur la meme fenetre a cartes que l'accueil (accueil.CardsPage) : une carte par
 * classe proposee (illustration de sa famille, passifs, runes, bonus), « Choisir », « Relancer » pour les evolutions.
 * Remplace ClassIntroScreen / ClassEvolutionIntroScreen / ClassSelectionScreen (meme logique : tirage sauvegarde, 2 relances,
 * illimitees pour un admin, filtre de la voie martiale du Clan Luo). Pas de kit : il n'est donne qu'a l'accueil.
 */
public class ClassChoicePage extends CardsPage {

    private static final int MAX_REROLLS = 2;

    private List<String> ids;
    private final boolean evolution, admin;
    /** Choix libre (palier 2 et au-dela, 2026-11-26) : toutes les evolutions, pas de relance. */
    private final boolean free;
    private int rerollsUsed;
    private boolean armed;                 // premier clic sur « Choisir » : demande de confirmation

    public ClassChoicePage(@Nonnull PlayerRef playerRef, @Nonnull List<String> classIds, boolean evolution, boolean free,
                           int rerollsUsed, boolean admin) {
        super(playerRef, CustomPageLifetime.CanDismiss);
        this.ids = new ArrayList<>(classIds);
        this.evolution = evolution;
        this.free = free;
        this.rerollsUsed = rerollsUsed;
        this.admin = admin;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        super.build(ref, ui, events, store);
        if (evolution && !free) saveChoices(ref, store);         // retrouver le meme tirage (et la pitie) si le joueur ferme
    }

    private void saveChoices(Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData d = store.getComponent(ref, type);
            if (d == null) return;
            d.setSavedEvolutionChoices(ids);
            store.putComponent(ref, type, d);
        } catch (Exception e) {
            EldaniorLogger.error("[Classes] sauvegarde du tirage", e);
        }
    }

    // ==================== CONTENU ====================

    @Override
    protected List<Card> cards() {
        List<Card> out = new ArrayList<>();
        for (String id : ids) {
            ClassModel m = ClassManager.get(id);
            if (m == null || m.isAdminAccess()) continue;
            Card c = ClassCards.of(m, false);
            c.kicker = (evolution ? "Évolution  ·  " : "Classe de base  ·  ") + c.kicker;
            out.add(c);
        }
        return out;
    }

    @Override protected String headline() { return evolution ? "Évolution de classe" : "Choix de classe"; }
    @Override protected String brandSub() { return "Statue des classes  ·  " + (evolution ? "une nouvelle voie s'ouvre" : "choisissez votre voie"); }
    @Override protected String tint() { return evolution ? "Violet" : "Or"; }

    private int rerollsLeft() { return admin ? Integer.MAX_VALUE : Math.max(0, MAX_REROLLS - rerollsUsed); }

    @Override
    protected String mainLabel(Card c) { return armed ? "Confirmer : " + c.title : "Choisir : " + c.title; }

    @Override protected String backLabel() { return "Fermer"; }

    @Override
    protected String altLabel() {
        if (!evolution || free) return null;
        return admin ? "Relancer (admin)" : "Relancer (" + rerollsLeft() + ")";
    }

    @Override
    protected String defaultHint() {
        if (armed) return "Cliquez encore pour confirmer : vos compétences de classe actuelles seront remplacées.";
        if (free) return "Choisissez librement la suite de votre lignée.";
        return evolution ? "Trois voies tirées au sort selon leur rareté. Une relance en tire de nouvelles."
                : "Comparez les passifs, les runes et les bonus, puis choisissez.";
    }

    @Override
    protected boolean onBack(Ref<EntityStore> ref, Store<EntityStore> store) {
        close();
        return true;
    }

    @Override
    protected boolean onAlt(Ref<EntityStore> ref, Store<EntityStore> store) {
        armed = false;
        if (!evolution || free) return false;
        if (rerollsLeft() <= 0) { hint = "Plus de relance disponible."; return false; }
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData d = store.getComponent(ref, type);
        if (d == null) return false;
        ClassModel current = ClassManager.get(d.getPlayerClassId());
        if (current == null) return false;
        List<String> pool = com.eldanior.system.classes.definitions.luo.VoieMartiale.filter(current.getNextClassId(), d);
        if (pool == null || pool.isEmpty()) return false;
        ids = com.eldanior.system.classes.definitions.luo.VoieMartiale.ensure(
                OpenClassSelectionInteraction.roll(pool, d), pool, d);        // tirage avec pitie (mise a jour)
        rerollsUsed++;
        d.setSavedEvolutionChoices(ids);
        d.useReroll();
        store.putComponent(ref, type, d);
        resetFocus();
        hint = "Nouveau tirage.";
        return false;
    }

    @Override
    protected boolean onMain(Ref<EntityStore> ref, Store<EntityStore> store, Card c) {
        ClassModel model = ClassManager.get(c.key);
        if (model == null) return false;
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData d = store.getComponent(ref, type);
        if (d == null) return false;
        if (!evolution && model.getId().equalsIgnoreCase(d.getPlayerClassId())) { hint = "Vous êtes déjà " + model.getDisplayName() + "."; return false; }
        if (!armed) { armed = true; return false; }
        armed = false;
        apply(ref, store, d, model);
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (pr != null) NotificationHelper.showEventTitle(pr, evolution ? "ÉVOLUTION" : "NOUVELLE CLASSE", model.getDisplayName(), true);
        close();
        return true;
    }

    /** Meme effet que l'ancien ClassSelectionScreen : classe, runes interdites retirees, competences actives, Dragon. */
    static void apply(Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData d, ClassModel model) {
        d.setPlayerClass(model.getDisplayName());
        d.setPlayerClassId(model.getId());
        com.eldanior.system.skills.runes.RuneManager.purgeDisallowedRunes(ref, store, d);
        d.clearSavedEvolutionChoices();
        for (String skill : model.getActiveSkillIds()) {
            d.learnSkill(skill);
            d.enableSkill(skill);
        }
        if ("dragon".equalsIgnoreCase(model.getId())) d.setDignity(1000);
        StatCalculator.updatePlayerStats(ref, store, d);
        store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), d);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        if (!"main".equals(data.action)) armed = false;     // toute autre action annule la confirmation
        super.handleDataEvent(ref, store, data);
    }
}
