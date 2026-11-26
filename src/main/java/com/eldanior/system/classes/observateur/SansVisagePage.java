package com.eldanior.system.classes.observateur;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.runes.spells.SansVisage;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Fenetre de la rune Sans Visage (Classes/SansVisage.ui, 2026-11-17). Onglets Joueurs (joueurs du meme monde : apparence et
 * nom copies), PNJ, Animaux, Monstres (catalogue Visages), recherche, 15 visages par page ; un clic choisit et montre le
 * modele en 3D a gauche, « Prendre ce visage » l'applique (SansVisage.COST mana). Aussi : visage de joueur au hasard,
 * nom d'emprunt, classe apparente, retour au vrai visage. Reservee a l'Observateur.
 */
public class SansVisagePage extends InteractiveCustomUIPage<SansVisagePage.Data> {

    static final int TILES = 15;
    static final String[] TABS = {"joueur", "pnj", "animal", "monstre"};
    /** Liseret des tuiles par onglet (teintes des types du HUD d'Arkies Domini, assombries : couleurs pleines seulement). */
    static final String[] TAB_COLORS = {"#8a7036", "#3d6690", "#477a47", "#80392f"};

    /** Visage propose : genre, id (uuid du joueur ou modele), nom, sous-titre, portrait 3D. */
    record Face(String kind, String id, String label, String sub, String portrait) {}

    private int tab = 1, page;
    private String search = "";
    private Face selected;
    private String hint = "";

    public SansVisagePage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
    }

    // ===================== CONSTRUCTION =====================

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Classes/SansVisage.ui");
        UUID uuid = playerRef.getUuid();
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        ui.set("#SvIcon.ItemId", "Rune_SansVisage");
        SansVisage.Mask mask = SansVisage.mask(uuid);
        String alias = ObservateurManager.alias(uuid, data);
        ClassModel shown = SansVisage.fakeClassId(uuid) != null ? SansVisage.shownClass(uuid, data) : null;
        ui.set("#SvState.Text", mask != null ? "Vous portez le visage : " + mask.label() : "Vous portez votre vrai visage");
        ui.set("#SvState2.Text", "Nom : " + (alias != null ? "« " + alias + " »" : "le vôtre")
                + "    ·    Classe montrée : " + (shown != null ? shown.getDisplayName() : "la vraie")
                + "    ·    " + SansVisage.COST + " mana par nouveau visage");

        // ---- apercu
        Face show = selected;
        String portrait = show != null ? show.portrait() : mask != null ? Visages.portrait(mask.modelId()) : Visages.portrait("Player");
        ui.set("#SvPreview.Visible", portrait != null);
        ui.set("#SvNoPreview.Visible", portrait == null);
        if (portrait != null) {
            ui.set("#SvPreview.ItemId", portrait);
            ui.set("#SvPreview.ItemScale", Visages.scaleOfPortrait(portrait));                 // gros modeles reduits
        }
        ui.set("#SvPreviewName.Text", show != null ? show.label() : mask != null ? mask.label() + " (porté)" : "Votre visage");
        ui.set("#SvWear.Text", show != null ? "Prendre ce visage (" + SansVisage.COST + " mana)" : "Choisissez un visage");

        // ---- nom et classe
        ui.set("#SvName.Value", alias != null ? alias : "");
        List<DropdownEntryInfo> classes = new ArrayList<>();
        List<ClassModel> all = new ArrayList<>(ClassManager.getAll());
        all.removeIf(ObservateurManager::isObservateur);
        all.sort(Comparator.comparing(ClassModel::getDisplayName, String.CASE_INSENSITIVE_ORDER));
        for (ClassModel m : all) classes.add(new DropdownEntryInfo(LocalizableString.fromString(m.getDisplayName()), m.getId()));
        ui.set("#SvClass.Entries", classes);
        String fake = SansVisage.fakeClassId(uuid);
        if (fake != null) ui.set("#SvClass.Value", fake);
        else if (!all.isEmpty()) ui.set("#SvClass.Value", all.get(0).getId());

        // ---- onglets et liste
        for (int i = 0; i < TABS.length; i++) {
            ui.set("#SvTab" + i + ".Text", Visages.categoryLabel(TABS[i]));
            ui.set("#SvTabOn" + i + ".Visible", i == tab);
            events.addEventBinding(CustomUIEventBindingType.Activating, "#SvTab" + i, EventData.of("Action", "tab").append("Param", String.valueOf(i)), false);
        }
        ui.set("#SvSearch.Value", search);
        List<Face> faces = faces(ref, store);
        int pages = Math.max(1, (faces.size() + TILES - 1) / TILES);
        page = Math.max(0, Math.min(page, pages - 1));
        ui.set("#SvCount.Text", faces.size() + " visage(s)" + (search.isBlank() ? "" : " pour « " + search + " »")
                + (tab == 0 ? " — joueurs de votre monde : apparence et nom copiés" : ""));
        for (int i = 0; i < TILES; i++) {
            int k = page * TILES + i;
            Face f = k < faces.size() ? faces.get(k) : null;
            ui.set("#SvT" + i + ".Visible", f != null);
            if (f == null) continue;
            ui.set("#SvTL" + i + ".Text", f.label());
            ui.set("#SvTS" + i + ".Text", f.sub());
            boolean on = selected != null && selected.kind().equals(f.kind()) && selected.id().equals(f.id());
            ui.set("#SvTBar" + i + ".Background", on ? "#e8b34f" : TAB_COLORS[tab]);   // liseret : choisi = or vif
            events.addEventBinding(CustomUIEventBindingType.Activating, "#SvT" + i, EventData.of("Action", "pick").append("Param", String.valueOf(k)), false);
        }
        ui.set("#SvPage.Text", "Page " + (page + 1) + " / " + pages);
        ui.set("#SvHint.Text", hint.isEmpty() ? "Choisissez un visage pour le voir en 3D, puis prenez-le. Le masque tombe à votre mort, "
                + "à votre déconnexion et quand vous frappez avec la Sentence Silencieuse." : hint);

        events.addEventBinding(CustomUIEventBindingType.Activating, "#SvPrev", EventData.of("Action", "prev"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SvNext", EventData.of("Action", "next"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SvSearchBtn", EventData.of("Action", "search").append("@Search", "#SvSearch.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SvWear", EventData.of("Action", "wear"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SvRandom", EventData.of("Action", "random"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SvReveal", EventData.of("Action", "reveal"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SvNameOk", EventData.of("Action", "name").append("@Name", "#SvName.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SvNameOff", EventData.of("Action", "name_off"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SvClassOk", EventData.of("Action", "class").append("@Class", "#SvClass.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SvClassOff", EventData.of("Action", "class_off"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SvCour", EventData.of("Action", "cour"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SvClose", EventData.of("Action", "close"), false);
    }

    /** Visages de l'onglet courant, filtres par la recherche. */
    private List<Face> faces(Ref<EntityStore> ref, Store<EntityStore> store) {
        List<Face> out = new ArrayList<>();
        if (tab == 0) {
            String f = search.trim().toLowerCase();
            for (PlayerRef pr : Universe.get().getPlayers()) {
                if (pr == null || pr.getUuid().equals(playerRef.getUuid())) continue;
                Ref<EntityStore> r = pr.getReference();
                if (r == null || !r.isValid() || r.getStore() != store) continue;      // meme monde : composants lisibles ici
                if (!f.isEmpty() && !pr.getUsername().toLowerCase().contains(f)) continue;
                PlayerLevelData d = store.getComponent(r, EldaniorSystem.get().getPlayerLevelDataType());
                ClassModel c = SansVisage.shownClass(pr.getUuid(), d);
                out.add(new Face("joueur", pr.getUuid().toString(), pr.getUsername(),
                        (d != null ? "Niveau " + d.getLevel() + "  ·  " : "") + (c != null ? c.getDisplayName() : "sans classe"), Visages.portrait("Player")));
            }
            out.sort(Comparator.comparing(Face::label, String.CASE_INSENSITIVE_ORDER));
            return out;
        }
        String cat = TABS[tab];
        for (Visages.Entry e : Visages.list(cat, search))
            out.add(new Face(cat, e.modelId(), e.label(), Visages.categoryLabel(cat), Visages.portrait(e.modelId())));
        return out;
    }

    // ===================== ACTIONS =====================

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Data data) {
        try {
            PlayerLevelData pd = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            if (!ObservateurManager.isObservateur(pd)) { close(); return; }
            if ("close".equals(data.action)) { close(); return; }
            if ("cour".equals(data.action)) { ObservateurCourTab.open(ref, store); return; }   // menu Systeme sur la Cour (remplace cette fenetre)
            UUID uuid = playerRef.getUuid();
            String a = data.action == null ? "" : data.action;
            hint = "";
            switch (a) {
                case "tab" -> { tab = clamp(parse(data.param), 0, TABS.length - 1); page = 0; selected = null; }
                case "prev" -> page = Math.max(0, page - 1);
                case "next" -> page++;
                case "search" -> { search = data.search == null ? "" : data.search.trim(); page = 0; }
                case "pick" -> {
                    List<Face> faces = faces(ref, store);
                    int k = parse(data.param);
                    if (k >= 0 && k < faces.size()) selected = faces.get(k);
                }
                case "wear" -> wear(ref, store);
                case "random" -> {
                    if (SansVisage.pay(ref, store) && SansVisage.wearSkin(ref, store, "aleatoire", SansVisage.randomSkin(), "Visage inconnu"))
                        hint = "Un visage que personne n'a jamais vu.";
                    selected = null;
                }
                case "reveal" -> {
                    SansVisage.reveal(ref, uuid, store, null, "Vous reprenez votre vrai visage.");
                    selected = null;
                    hint = "Vous êtes de nouveau vous-même.";
                }
                case "name" -> {
                    String alias = ObservateurManager.cleanAlias(data.name);
                    if (alias == null) hint = "Nom refusé : " + ObservateurManager.ALIAS_MAX + " caractères au plus (lettres, chiffres, espaces, ' - .).";
                    else { ObservateurManager.setAlias(uuid, alias); hint = "Vous vous faites appeler « " + alias + " »."; }
                }
                case "name_off" -> { ObservateurManager.setAlias(uuid, null); hint = "Vous reprenez votre vrai nom."; }
                case "class" -> {
                    ClassModel m = data.cls == null ? null : ClassManager.get(data.cls);
                    if (m == null || ObservateurManager.isObservateur(m)) hint = "Classe introuvable.";
                    else { SansVisage.setFakeClass(uuid, m.getId()); hint = "Les autres vous croient " + m.getDisplayName() + "."; }
                }
                case "class_off" -> { SansVisage.setFakeClass(uuid, null); hint = "Votre vraie classe se voit de nouveau."; }
                default -> { }
            }
            rebuild();
        } catch (Exception e) {
            EldaniorLogger.error("[Sans Visage] fenetre", e);
        }
    }

    private void wear(Ref<EntityStore> ref, Store<EntityStore> store) {
        Face f = selected;
        if (f == null) { hint = "Choisissez d'abord un visage dans la liste."; return; }
        if ("joueur".equals(f.kind())) {
            PlayerRef other = Universe.get().getPlayer(UUID.fromString(f.id()));
            Ref<EntityStore> r = other != null ? other.getReference() : null;
            if (r == null || !r.isValid() || r.getStore() != store) { hint = f.label() + " n'est plus dans votre monde."; return; }
            var skin = SansVisage.currentSkin(r, store);
            if (skin == null) { hint = "Impossible de lire l'apparence de " + f.label() + "."; return; }
            if (!SansVisage.pay(ref, store)) return;
            if (SansVisage.wearSkin(ref, store, "joueur", skin, f.label())) {
                String alias = ObservateurManager.cleanAlias(f.label());
                if (alias != null) ObservateurManager.setAlias(playerRef.getUuid(), alias);
                hint = "Vous avez le visage et le nom de " + f.label() + ".";
            }
            return;
        }
        if (!SansVisage.pay(ref, store)) return;
        hint = SansVisage.wearModel(ref, store, f.kind(), f.id(), f.label())
                ? "Vous voilà " + f.label() + (ObservateurManager.hasAlias(playerRef.getUuid()) ? "." : " — sans nom au-dessus de la tête.")
                : "Ce modèle est introuvable.";
    }

    private static int parse(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("Param", Codec.STRING), (d, v) -> d.param = v, d -> d.param).add()
                .append(new KeyedCodec<>("@Search", Codec.STRING), (d, v) -> d.search = v, d -> d.search).add()
                .append(new KeyedCodec<>("@Name", Codec.STRING), (d, v) -> d.name = v, d -> d.name).add()
                .append(new KeyedCodec<>("@Class", Codec.STRING), (d, v) -> d.cls = v, d -> d.cls).add()
                .build();
        public String action, param, search, name, cls;
    }
}
