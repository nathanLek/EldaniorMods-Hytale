package com.eldanior.system.gui;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
// AdminTab moved to AdminScreen
import com.eldanior.system.gui.tabs.BlackMarketTab;
import com.eldanior.system.gui.tabs.ShopTab;
import com.eldanior.system.gui.tabs.FamilleTab;
import com.eldanior.system.gui.tabs.WikiTab;
import com.eldanior.system.gui.tabs.CompetencesTab;
import com.eldanior.system.gui.tabs.GuildeTab;
import com.eldanior.system.gui.tabs.GroupeTab;
import com.eldanior.system.gui.tabs.EchangesTab;
import com.eldanior.system.gui.tabs.ProfilTab;
import com.eldanior.system.gui.tabs.TitresTab;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class SystemScreen extends InteractiveCustomUIPage<SystemScreen.SystemEventData> {

    private static final String[] TAB_IDS = {
            "Competences", "Quetes", "Guilde", "Groupe", "Famille", "Titres", "Shop", "BlackMarket", "Observateur", "Echanges", "Wiki"
    };

    /** Titre du panneau de contenu par onglet (refonte du 2026-11-12, categories facon inventaire). */
    private static final java.util.Map<String, String> TAB_LABELS = java.util.Map.of(
            "Quetes", "Quêtes", "Competences", "Compétences", "Guilde", "Guilde", "Groupe", "Groupe", "Famille", "Famille",
            "Titres", "Titres", "Shop", "Marché", "BlackMarket", "Marché noir", "Echanges", "Échanges", "Wiki", "Wiki");
    /**
     * Icone (objet du jeu) du bouton de chaque categorie, choisie le 2026-11-12 pour sa lisibilite a 52 px (planches
     * comparees ; Guilde = banniere, Famille = banniere de famille, demandes de l'utilisateur). Verifier que l'objet EXISTE (fichier Server/Item/Items/...) et pas seulement son image : sinon rien ne
     * s'affiche (cas de Weapon_Dual_Sword_Shield_Iron). Groupe : pas d'objet, image TabIconGroupe.png dans SystemPage.ui.
     * Famille : banniere de la famille du joueur si elle existe.
     */
    private static final java.util.Map<String, String> TAB_ICONS = java.util.Map.of(
            "Competences", "Rune_Fireball", "Quetes", "Deco_Map", "Guilde", "Furniture_Human_Ruins_Banner",
            "Famille", "Eldanior_Banniere_Famille_Eldanior", "Titres", "Butin_Couronne_Trork", "Shop", "Butin_Bourse_Volee",
            "BlackMarket", "Weapon_Daggers_Doomed", "Echanges", "Furniture_Temple_Light_Chest_Small", "Wiki", "Butin_Grimoire_Dechire");

    private static boolean itemExists(String id) {
        return id != null && com.hypixel.hytale.server.core.asset.type.item.config.Item.getAssetMap().getAsset(id) != null;
    }

    /** Categorie active : fond allume du bouton et titre du panneau de contenu. */
    private static void markTab(UICommandBuilder ui, String active) {
        for (String tab : TAB_IDS) ui.set("#TabOn" + tab + ".Visible", tab.equals(active));
        ui.set("#ContentTitle.Text", TAB_LABELS.getOrDefault(active, active));
    }

    /** Cadre des deux fenetres (profil, contenu) : or, ou rouge pour un criminel (PK) ; couches #Frame* / #Frame*Red de SystemPage.ui. */
    private static void frame(UICommandBuilder ui, boolean pk) {
        for (String win : new String[]{"#Sidebar", "#ContentPanel"}) {               // la navigation n'a plus de cadre
            for (String part : new String[]{"Title", "Body", "DecoTop", "DecoBottom"}) {
                ui.set(win + " #Frame" + part + ".Visible", !pk);
                ui.set(win + " #Frame" + part + "Red.Visible", pk);
            }
        }
    }

    /** Famille : seulement pour choisir sa famille (noble sans famille) ; sa gestion est chez l'intendant (2026-11-12). */
    /** 2026-11-12 : tout ce qui touche a la famille passe par l'intendant (y compris prendre la tete d'une maison libre). */
    /**
     * 2026-11-13 (demande de l'utilisateur) : l'onglet Famille ne sert plus qu'au Marquis sans maison, pour acheter un grand
     * territoire (un Marquisat et sa maison). Le Roi est Patriarche d'Eldanior d'office (RoyalHouse) ; Ducs et Comtes achetent
     * un Territoire ou une ville chez l'intendant (LandOfficeScreen).
     */
    private boolean showFamily(PlayerLevelData d) {
        if (steward) return true;
        boolean house = d != null && d.getNobleFamilyId() != null && !d.getNobleFamilyId().isEmpty();
        return d != null && !house && com.eldanior.system.titles.nobility.NobilityRank.fromString(d.getNobilityRank())
                == com.eldanior.system.titles.nobility.NobilityRank.MARQUIS;
    }

    /** Distance maximale d'un autre joueur pour afficher l'onglet Echanges. */
    private static final double TRADE_RANGE = 10.0;

    /** Un autre joueur se tient-il pres de celui-ci (meme monde, <= TRADE_RANGE blocs) ? */
    private static boolean nearPlayer(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef me = store.getComponent(ref, PlayerRef.getComponentType());
        if (me == null) return false;
        var pos = com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_POSITIONS;
        org.joml.Vector3d mine = pos.get(me.getUuid());
        if (mine == null) return false;
        for (PlayerRef other : com.hypixel.hytale.server.core.universe.Universe.get().getPlayers()) {
            if (other == null || other.getUuid().equals(me.getUuid())) continue;
            Ref<EntityStore> r = other.getReference();
            if (r == null || !r.isValid() || r.getStore() != store) continue;               // autre monde
            org.joml.Vector3d at = pos.get(other.getUuid());
            if (at != null && at.distanceSquared(mine) <= TRADE_RANGE * TRADE_RANGE) return true;
        }
        return false;
    }

    /** Onglets qui dependent du joueur : Famille (choix), Guilde (membre), Echanges (joueur proche). */
    private void tabVisibility(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData d) {
        ui.set("#TabBtnFamille.Visible", showFamily(d));
        ui.set("#TabBtnGuilde.Visible", d.hasGuild());
        ui.set("#TabBtnEchanges.Visible", nearPlayer(ref, store));
    }

    // ===== statistiques en temps reel (vie, endurance, mana) tant que le menu est ouvert (2026-11-12) =====
    private java.util.concurrent.ScheduledFuture<?> liveTask;
    private String liveKey = "";

    private void startLive(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (liveTask != null || steward) return;
        com.hypixel.hytale.server.core.universe.world.World world = store.getExternalData().getWorld();
        if (world == null) return;
        liveTask = EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> world.execute(() -> {
            try {
                var p = ref.isValid() ? store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType()) : null;
                if (p == null || p.getPageManager().getCustomPage() != this) { stopLive(); return; }   // menu ferme ou remplace
                String key = ProfilTab.resourceKey(ref, store);
                if (key.equals(liveKey)) return;
                liveKey = key;
                UICommandBuilder u = new UICommandBuilder();
                ProfilTab.populateResourceBars(u, ref, store);
                sendUpdate(u);
            } catch (Exception e) {
                stopLive();
            }
        }), 500, 500, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private void stopLive() {
        if (liveTask != null) { liveTask.cancel(false); liveTask = null; }
    }

    private static String money(long v) {
        return String.format(java.util.Locale.FRANCE, "%,d", v).replace('\u202f', ' ').replace('\u00a0', ' ') + " or";
    }

    // Timer pour refresh automatique des cooldowns dans l'onglet compétences
    private java.util.concurrent.ScheduledFuture<?> cooldownRefreshTask = null;
    private Ref<EntityStore> cachedRef = null;
    private Store<EntityStore> cachedStore = null;

    /**
     * Mode intendant (guards/LuoMerchants, PNJ intendant du territoire) : seul l'onglet Famille, sans la barre laterale, avec
     * la gestion de la famille (FamilleTab.STEWARD). Le menu systeme normal ne fait que la consulter.
     */
    private final boolean steward;

    public SystemScreen(@Nonnull PlayerRef playerRef) {
        this(playerRef, false);
    }

    public SystemScreen(@Nonnull PlayerRef playerRef, boolean steward) {
        super(playerRef, CustomPageLifetime.CanDismiss, SystemEventData.CODEC);
        this.steward = steward;
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        if (steward) FamilleTab.STEWARD.remove(com.eldanior.system.config.UUIDExtractor.getUUID(playerRef));
        stopLive();
        stopCooldownRefresh();
        super.onDismiss(ref, store);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("System/SystemPage.ui");

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) {
            data = new PlayerLevelData();
            store.putComponent(ref, type, data);
        }

        String playerName = getPlayerName(ref, store);

        frame(ui, data.isPK());                                            // cadre rouge pour un criminel (2026-11-12)

        // Icones des categories
        for (String tab : TAB_IDS) {
            String icon = TAB_ICONS.get(tab);
            if (icon == null) continue;                                    // Groupe : image fixe, pas d'ItemIcon
            if ("Famille".equals(tab) && data.getNobleFamilyId() != null && !data.getNobleFamilyId().isEmpty()) {
                String fid = data.getNobleFamilyId();
                String banner = "Eldanior_Banniere_Famille_" + Character.toUpperCase(fid.charAt(0)) + fid.substring(1);
                if (itemExists(banner)) icon = banner;
            }
            if (itemExists(icon)) ui.set("#TabIcon" + tab + ".ItemId", icon);
        }
        // Poche dimensionnelle (2026-11-16) : sa case sous le personnage, ses deux grilles (PocheTab)
        com.eldanior.system.poche.PocheTab.slot(ui, store.getComponent(ref, PlayerRef.getComponentType()));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PocheSlot", EventData.of("Action", "open_poche"), false);
        events.addEventBinding(CustomUIEventBindingType.SlotClicking, "#PocheGrid", EventData.of("Action", "poche_click"), false);
        events.addEventBinding(CustomUIEventBindingType.SlotClicking, "#PocheInvGrid", EventData.of("Action", "poche_inv_click"), false);
        events.addEventBinding(CustomUIEventBindingType.Dropped, "#PocheGrid", EventData.of("Action", "poche_drop"), false);
        events.addEventBinding(CustomUIEventBindingType.Dropped, "#PocheInvGrid", EventData.of("Action", "poche_inv_drop"), false);

        // Sidebar info
        ui.set("#SidebarPlayerName.Text", playerName);
        ui.set("#SidebarMoney.Text", money(data.getMoney()));
        ui.set("#SidebarLevel.Text", "Niveau " + data.getLevel());

        // Admin separe — via /es admin
        PlayerRef pRefCheck = store.getComponent(ref, PlayerRef.getComponentType());
        com.hypixel.hytale.server.core.entity.entities.Player playerCheck = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());

        // Famille visible si Duc, Marquis ou Roi (ou si a deja une famille)
        if (steward) FamilleTab.STEWARD.add(com.eldanior.system.config.UUIDExtractor.getUUID(playerRef));
        else FamilleTab.STEWARD.remove(com.eldanior.system.config.UUIDExtractor.getUUID(playerRef));
        if (showFamily(data)) FamilleTab.populate(ui, ref, store);
        tabVisibility(ui, ref, store, data);
        if (steward) {                                                     // fenetre de l'intendant : la famille seule
            ui.set("#Sidebar.Visible", false);
            ui.set("#CategoryBar.Visible", false);
            for (String tab : TAB_IDS) ui.set("#Tab" + tab + ".Visible", "Famille".equals(tab));
            markTab(ui, "Famille");
        }

        // Echanges : onglet visible seulement pres d'un autre joueur (tabVisibility)
        EchangesTab.populate(ui, ref, store, playerCheck != null && playerCheck.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION));

        // Quetes en cours (lecture seule)
        com.eldanior.system.gui.tabs.QuetesEnCoursTab.populate(ui, ref, store);

        // Populate profil tab (default)
        ProfilTab.populate(ui, ref, store, data, playerName);

        // Populate titres tab
        TitresTab.populate(ui, ref, store);

        // Populate competences tab
        CompetencesTab.populate(ui, ref, store);
        for (int i = 0; i < CompetencesTab.MAX_SKILL_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#SkillToggle" + i,
                    EventData.of("Action", "skill_toggle").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#SkillForget" + i,
                    EventData.of("Action", "skill_forget").append("Param", String.valueOf(i)));
        }
        for (int k = 0; k < 10; k++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#SkillFilter" + k,
                    EventData.of("Action", "skill_filter").append("Param", String.valueOf(k)));
        }
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SkillBtnPrev", EventData.of("Action", "skill_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SkillBtnNext", EventData.of("Action", "skill_next"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SkillTabPassive", EventData.of("Action", "skill_tab_passive"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SkillTabActive", EventData.of("Action", "skill_tab_active"));
        // Runes : clic et glisser-deposer sur les deux grilles (RunesTab.handle)
        events.addEventBinding(CustomUIEventBindingType.SlotClicking, "#RuneBagGrid", EventData.of("Action", "rune_bag_click"), false);
        events.addEventBinding(CustomUIEventBindingType.SlotClicking, "#RuneAbilityGrid", EventData.of("Action", "rune_slot_click"), false);
        events.addEventBinding(CustomUIEventBindingType.Dropped, "#RuneAbilityGrid", EventData.of("Action", "rune_slot_drop"), false);
        events.addEventBinding(CustomUIEventBindingType.Dropped, "#RuneBagGrid", EventData.of("Action", "rune_bag_drop"), false);

        // Populate guilde tab
        GuildeTab.populate(ui, ref, store);

        // Guilde buttons
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GuildeBtnLeave", EventData.of("Action", "guild_leave"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GuildeBtnDisband", EventData.of("Action", "guild_disband"));
        for (int i = 0; i < GuildeTab.MAX_INVITE_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#GInviteBtn" + i, EventData.of("Action", "guild_invite").append("Param", String.valueOf(i)));
        }
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GuildeBtnToggleRecruitment", EventData.of("Action", "guild_toggle_recruitment"));
        for (int i = 0; i < GuildeTab.MAX_OPEN_GUILD_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#GOpenGuildBtn" + i, EventData.of("Action", "guild_join").append("Param", String.valueOf(i)));
        }

        // Guild accordion toggles
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GuildeBtnToggleMembers", EventData.of("Action", "guild_toggle_members"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GuildeBtnToggleInvite", EventData.of("Action", "guild_toggle_invite"));

        // Guild member pagination
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GMemberBtnPrev", EventData.of("Action", "guild_member_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GMemberBtnNext", EventData.of("Action", "guild_member_next"));

        // Guild member actions (5 slots)
        for (int i = 0; i < GuildeTab.MAX_MEMBER_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#GMemberBtnPromote" + i, EventData.of("Action", "guild_promote").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#GMemberBtnDemote" + i, EventData.of("Action", "guild_demote").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#GMemberBtnKick" + i, EventData.of("Action", "guild_kick").append("Param", String.valueOf(i)));
        }

        // Guild invite pagination
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GInviteBtnPrev", EventData.of("Action", "guild_invite_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GInviteBtnNext", EventData.of("Action", "guild_invite_next"));

        // Guild open guilds pagination
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GOpenBtnPrev", EventData.of("Action", "guild_open_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GOpenBtnNext", EventData.of("Action", "guild_open_next"));

        // Populate groupe tab
        GroupeTab.populate(ui, ref, store);

        // Groupe buttons
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GroupeBtnCreate", EventData.of("Action", "grp_create"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GroupeBtnLeave", EventData.of("Action", "grp_leave"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GroupeBtnDisband", EventData.of("Action", "grp_disband"));
        for (int i = 0; i < GroupeTab.MAX_MEMBER_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#GrpBtnKick" + i, EventData.of("Action", "grp_kick").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#GrpBtnPromote" + i, EventData.of("Action", "grp_promote").append("Param", String.valueOf(i)));
        }
        for (int i = 0; i < GroupeTab.MAX_INVITE_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#InviteBtn" + i, EventData.of("Action", "grp_invite").append("Param", String.valueOf(i)));
        }

        // Titres equip buttons
        for (int i = 0; i < TitresTab.MAX_TITLE_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#TitleBtn" + i,
                    EventData.of("Action", "title_equip").append("Param", String.valueOf(i)));
        }

        // Famille/Guilde deposit buttons
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GuildeBtnDeposit", EventData.of("Action", "guild_deposit"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GuildeBtnWithdraw", EventData.of("Action", "guild_withdraw"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FamBtnDeposit", EventData.of("Action", "fam_deposit"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FamBtnWithdraw", EventData.of("Action", "fam_withdraw"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FamBtnLeave", EventData.of("Action", "fam_leave"));

        // Famille choose buttons
        for (int i = 0; i < FamilleTab.MAX_FAMILY_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#FamBtn" + i,
                    EventData.of("Action", "fam_choose").append("Param", String.valueOf(i)));
        }
        // Famille invite buttons
        for (int i = 0; i < FamilleTab.MAX_INVITE_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#FInviteBtn" + i,
                    EventData.of("Action", "fam_invite").append("Param", String.valueOf(i)));
        }

        // Famille accordion toggles
        // categories de la fenetre de l'intendant et reserve de la famille (2026-11-10)
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FamCatListes", EventData.of("Action", "fam_cat_listes"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FamCatOrdres", EventData.of("Action", "fam_cat_ordres"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FamCatGestion", EventData.of("Action", "fam_cat_gestion"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FamCatAchats", EventData.of("Action", "fam_cat_achats"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FamBtnReserve", EventData.of("Action", "fam_reserve"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FamBtnToggleMembers", EventData.of("Action", "fam_toggle_members"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FamBtnToggleOrdres", EventData.of("Action", "fam_toggle_ordres"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FamBtnToggleInvite", EventData.of("Action", "fam_toggle_invite"));

        // Famille member pagination
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FMemberBtnPrev", EventData.of("Action", "fam_member_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FMemberBtnNext", EventData.of("Action", "fam_member_next"));

        // Famille member actions (5 slots)
        for (int i = 0; i < FamilleTab.MAX_MEMBER_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#FMemberBtnPromote" + i, EventData.of("Action", "fam_promote").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#FMemberBtnDemote" + i, EventData.of("Action", "fam_demote").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#FMemberBtnKick" + i, EventData.of("Action", "fam_kick").append("Param", String.valueOf(i)));
        }

        // Famille invite pagination
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FInviteBtnPrev", EventData.of("Action", "fam_invite_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FInviteBtnNext", EventData.of("Action", "fam_invite_next"));

        // Famille disband button
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FamBtnDisband", EventData.of("Action", "fam_disband"));

        // Knight Order cards (3 max)
        for (int i = 0; i < 3; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#OrdreBtnDetail" + i, EventData.of("Action", "fam_ordre_detail").append("Param", String.valueOf(i)));
        }
        events.addEventBinding(CustomUIEventBindingType.Activating, "#FamBtnCreateOrdre", EventData.of("Action", "fam_create_ordre"));

        // Order detail view
        events.addEventBinding(CustomUIEventBindingType.Activating, "#OrdreBtnBack", EventData.of("Action", "fam_ordre_back"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#OrdreBtnInvite", EventData.of("Action", "ordre_invite"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#OrdreBtnDisband", EventData.of("Action", "ordre_disband"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#OrdreBtnJoin", EventData.of("Action", "ordre_join"));

        // Order member pagination
        events.addEventBinding(CustomUIEventBindingType.Activating, "#OMemberBtnPrev", EventData.of("Action", "ordre_member_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#OMemberBtnNext", EventData.of("Action", "ordre_member_next"));

        // Order member actions (5 slots)
        for (int i = 0; i < 5; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#OMemberBtnLt" + i, EventData.of("Action", "ordre_promote_lt").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#OMemberBtnDemote" + i, EventData.of("Action", "ordre_demote").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#OMemberBtnKick" + i, EventData.of("Action", "ordre_kick").append("Param", String.valueOf(i)));
        }

        // Populate shop tab (PK ne voient pas le shop normal)
        boolean isPK = data.isPK();
        if (isPK) {
            ui.set("#TabBtnShop.Visible", false);
        } else {
            ShopTab.populate(ui, ref, store);
        }
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ShopBtnPrev", EventData.of("Action", "shop_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ShopBtnNext", EventData.of("Action", "shop_next"));
        for (int i = 0; i < ShopTab.MAX_SHOP_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#ShopBtnBuy" + i,
                    EventData.of("Action", "shop_buy").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#ShopBtnCancel" + i,
                    EventData.of("Action", "shop_cancel").append("Param", String.valueOf(i)));
        }

        // Populate black market (visible si PK ou admin)
        boolean showBM = data.isPK() || com.eldanior.system.classes.observateur.ObservateurManager.isObservateur(data)   // Observateur : il voit le marche noir
                || (playerCheck != null && playerCheck.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION));
        ui.set("#TabBtnBlackMarket.Visible", showBM);
        if (showBM) {
            BlackMarketTab.populate(ui, ref, store);
        }
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnBlackMarket", EventData.of("Action", "tab_blackmarket"));
        // Cour de l'Observateur (2026-11-17) : le Roi et l'Empereur Luo gerent leur Observateur sans commande ;
        // 2026-11-19 : panneau #TabObservateur a droite des onglets (ObservateurCourTab), plus une fenetre a part
        boolean showCour = com.eldanior.system.classes.observateur.ObservateurCourTab.canOpen(
                playerCheck != null ? playerCheck.getPlayerRef() : null, data);
        ui.set("#TabBtnObservateur.Visible", showCour && !steward);
        if (showCour && itemExists("Rune_ArkiesDomini")) ui.set("#TabIconObservateur.ItemId", "Rune_ArkiesDomini");
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnObservateur", EventData.of("Action", "tab_observateur"), false);
        com.eldanior.system.classes.observateur.ObservateurCourTab.bind(events);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BMBtnPrev", EventData.of("Action", "bm_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BMBtnNext", EventData.of("Action", "bm_next"));
        for (int i = 0; i < BlackMarketTab.MAX_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BMBtnBuy" + i, EventData.of("Action", "bm_buy").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BMBtnCancel" + i, EventData.of("Action", "bm_cancel").append("Param", String.valueOf(i)));
        }

        // Echanges invite buttons
        for (int i = 0; i < EchangesTab.MAX_TRADE_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#EchBtn" + i,
                    EventData.of("Action", "ech_invite").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#EchForceBtn" + i,
                    EventData.of("Action", "ech_force").append("Param", String.valueOf(i)));
        }

        // Admin separe — via /es admin

        // === EVENT BINDINGS ===

        // Tab navigation
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnCompetences", EventData.of("Action", "tab_competences"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnGuilde",      EventData.of("Action", "tab_guilde"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnGroupe",      EventData.of("Action", "tab_groupe"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnFamille",     EventData.of("Action", "tab_famille"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnTitres",      EventData.of("Action", "tab_titres"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnShop",        EventData.of("Action", "tab_shop"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnEchanges",   EventData.of("Action", "tab_echanges"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnWiki",       EventData.of("Action", "tab_wiki"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnQuetes",     EventData.of("Action", "tab_quetes"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#WikiBtnPrev",      EventData.of("Action", "wiki_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#WikiBtnNext",      EventData.of("Action", "wiki_next"));
        // Admin tab removed — /es admin

        // Attribute buttons (+1)
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnStr1", EventData.of("Action", "attr_str_1"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnVit1", EventData.of("Action", "attr_vit_1"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnInt1", EventData.of("Action", "attr_int_1"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnEnd1", EventData.of("Action", "attr_end_1"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnAgl1", EventData.of("Action", "attr_agl_1"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnLck1", EventData.of("Action", "attr_lck_1"));

        // Attribute buttons (+5)
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnStr5", EventData.of("Action", "attr_str_5"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnVit5", EventData.of("Action", "attr_vit_5"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnInt5", EventData.of("Action", "attr_int_5"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnEnd5", EventData.of("Action", "attr_end_5"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnAgl5", EventData.of("Action", "attr_agl_5"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnLck5", EventData.of("Action", "attr_lck_5"));

        if (pocheOnOpen && !steward) showPoche(ui, ref, store);            // ouvert par l'objet : directement sur la poche
        else if (observateurOnOpen && !steward) showObservateur(ui, ref, store, data);   // Sans Visage « Ma charge », commande
        startLive(ref, store);
    }

    /** JSON brut du dernier evenement recu : les grilles de runes y lisent les cles du client (SlotIndex, DragSourceSlotId...). */
    private String lastRawEvent;

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, String rawData) {
        this.lastRawEvent = rawData;
        super.handleDataEvent(ref, store, rawData);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull SystemEventData eventData) {
        if (eventData.action == null) return;
        if (!steward && FamilleTab.MANAGEMENT_ACTIONS.contains(eventData.action)) return;   // gestion : chez l'intendant
        if (steward && eventData.action.startsWith("tab_") && !"tab_famille".equals(eventData.action)) return;

        // Tab switching
        if (eventData.action.startsWith("tab_")) {
            switchTab(eventData.action.substring(4), ref, store);
            return;
        }

        // Attribute points
        if (eventData.action.startsWith("attr_")) {
            if (ProfilTab.handleAttribute(eventData.action, ref, store)) {
                refreshProfilTab(ref, store);
            }
            return;
        }

        // Deposits
        if ("guild_deposit".equals(eventData.action)) {
            if (GuildeTab.handleDeposit(ref, store)) {
                refreshGuildeTab(ref, store);
                refreshProfilTab(ref, store); // update money in sidebar
            }
            return;
        }
        if ("guild_withdraw".equals(eventData.action)) {
            if (GuildeTab.handleWithdraw(ref, store)) {
                refreshGuildeTab(ref, store);
                refreshProfilTab(ref, store);
            }
            return;
        }
        if ("fam_deposit".equals(eventData.action)) {
            if (FamilleTab.handleDeposit(ref, store)) {
                refreshFamilleTab(ref, store);
                refreshProfilTab(ref, store);
            }
            return;
        }
        if ("fam_withdraw".equals(eventData.action)) {
            if (FamilleTab.handleWithdraw(ref, store)) {
                refreshFamilleTab(ref, store);
                refreshProfilTab(ref, store);
            }
            return;
        }

        // Famille leave
        if ("fam_leave".equals(eventData.action)) {
            if (FamilleTab.handleLeave(ref, store)) {
                refreshFamilleTab(ref, store);
                refreshProfilTab(ref, store);
            }
            return;
        }

        // Famille invite
        if ("fam_invite".equals(eventData.action) && eventData.param != null) {
            if (FamilleTab.handleInviteByIndex(eventData.param, ref, store)) {
                refreshFamilleTab(ref, store);
            }
            return;
        }

        // Famille choose
        if ("fam_choose".equals(eventData.action) && eventData.param != null) {
            if (FamilleTab.handleChoose(eventData.param, ref, store)) {
                refreshFamilleTab(ref, store);
                refreshProfilTab(ref, store);
            }
            return;
        }

        // Intendant : categories et reserve
        if (eventData.action.startsWith("fam_cat_")) {
            FamilleTab.setCategory(getPlayerUUID(ref, store), eventData.action.substring("fam_cat_".length()));
            refreshFamilleTab(ref, store);
            return;
        }
        if ("open_poche".equals(eventData.action) || eventData.action.startsWith("poche_")) {   // poche : panneau dans le menu
            try {
                if (!"open_poche".equals(eventData.action))
                    com.eldanior.system.poche.PocheTab.handle(ref, store, eventData.action, lastRawEvent);
                UICommandBuilder u = new UICommandBuilder();
                showPoche(u, ref, store);
                sendUpdate(u);
            } catch (Exception e) {
                EldaniorLogger.error("[Poche] menu systeme", e);
            }
            return;
        }
        if (eventData.action.startsWith(com.eldanior.system.classes.observateur.ObservateurCourTab.PREFIX)) {   // Cour : panneau
            if (steward) return;
            try {
                cour().handle(eventData.action.substring(com.eldanior.system.classes.observateur.ObservateurCourTab.PREFIX.length()),
                        eventData.param, eventData.ocAlias, ref, store);
                UICommandBuilder u = new UICommandBuilder();
                cour().populate(u, ref, store);
                sendUpdate(u);
            } catch (Exception e) {
                EldaniorLogger.error("[Observateur] cour", e);
            }
            return;
        }
        if ("fam_reserve".equals(eventData.action)) {
            try {
                PlayerLevelData d = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
                String shop = d != null ? FamilleTab.reserveShop(d.getNobleFamilyId()) : null;
                var pl = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                if (shop != null && pl != null)                                 // openCustomPage remplace cette page (ne pas fermer avant)
                    pl.getPageManager().openCustomPage(ref, store, com.eldanior.system.guards.ShopPage.reserve(playerRef, shop));
            } catch (Exception e) {
                EldaniorLogger.error("[Intendant] reserve", e);
            }
            return;
        }

        // Famille accordion toggles
        if ("fam_toggle_members".equals(eventData.action)) {
            FamilleTab.handleToggleMembers(getPlayerUUID(ref, store));
            refreshFamilleTab(ref, store);
            return;
        }
        if ("fam_toggle_ordres".equals(eventData.action)) {
            FamilleTab.handleToggleOrdres(getPlayerUUID(ref, store));
            refreshFamilleTab(ref, store);
            return;
        }
        if ("fam_toggle_invite".equals(eventData.action)) {
            FamilleTab.handleToggleInvite(getPlayerUUID(ref, store));
            refreshFamilleTab(ref, store);
            return;
        }

        // Famille member pagination
        if ("fam_member_prev".equals(eventData.action)) {
            FamilleTab.handleMemberPrev(getPlayerUUID(ref, store));
            refreshFamilleTab(ref, store);
            return;
        }
        if ("fam_member_next".equals(eventData.action)) {
            FamilleTab.handleMemberNext(getPlayerUUID(ref, store));
            refreshFamilleTab(ref, store);
            return;
        }

        // Famille invite pagination
        if ("fam_invite_prev".equals(eventData.action)) {
            FamilleTab.handleInvitePrev(getPlayerUUID(ref, store));
            refreshFamilleTab(ref, store);
            return;
        }
        if ("fam_invite_next".equals(eventData.action)) {
            FamilleTab.handleInviteNext(getPlayerUUID(ref, store));
            refreshFamilleTab(ref, store);
            return;
        }

        // Famille member actions
        if ("fam_promote".equals(eventData.action) && eventData.param != null) {
            if (FamilleTab.handlePromote(eventData.param, ref, store)) refreshFamilleTab(ref, store);
            return;
        }
        if ("fam_demote".equals(eventData.action) && eventData.param != null) {
            if (FamilleTab.handleDemote(eventData.param, ref, store)) refreshFamilleTab(ref, store);
            return;
        }
        if ("fam_kick".equals(eventData.action) && eventData.param != null) {
            if (FamilleTab.handleKick(eventData.param, ref, store)) refreshFamilleTab(ref, store);
            return;
        }

        // Famille disband
        if ("fam_disband".equals(eventData.action)) {
            if (FamilleTab.handleDisband(ref, store)) {
                refreshFamilleTab(ref, store);
                refreshProfilTab(ref, store);
            }
            return;
        }

        // Knight Order navigation
        if ("fam_ordre_detail".equals(eventData.action) && eventData.param != null) {
            if (FamilleTab.handleOrdreDetail(eventData.param, getPlayerUUID(ref, store))) refreshFamilleTab(ref, store);
            return;
        }
        if ("fam_ordre_back".equals(eventData.action)) {
            if (FamilleTab.handleOrdreBack(getPlayerUUID(ref, store))) refreshFamilleTab(ref, store);
            return;
        }
        if ("fam_create_ordre".equals(eventData.action)) {
            com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            if (player != null) {
                player.getPlayerRef().sendMessage(Message.raw("Les ordres de chevalier se créent auprès de l'intendant de votre famille."));
            }
            return;
        }

        // Order member pagination
        if ("ordre_member_prev".equals(eventData.action)) {
            FamilleTab.handleOrdreMemberPrev(getPlayerUUID(ref, store));
            refreshFamilleTab(ref, store);
            return;
        }
        if ("ordre_member_next".equals(eventData.action)) {
            FamilleTab.handleOrdreMemberNext(getPlayerUUID(ref, store));
            refreshFamilleTab(ref, store);
            return;
        }

        // Order member actions
        if ("ordre_promote_lt".equals(eventData.action) && eventData.param != null) {
            if (FamilleTab.handleOrdrePromoteLt(eventData.param, ref, store)) refreshFamilleTab(ref, store);
            return;
        }
        if ("ordre_demote".equals(eventData.action) && eventData.param != null) {
            if (FamilleTab.handleOrdreDemote(eventData.param, ref, store)) refreshFamilleTab(ref, store);
            return;
        }
        if ("ordre_kick".equals(eventData.action) && eventData.param != null) {
            if (FamilleTab.handleOrdreKick(eventData.param, ref, store)) refreshFamilleTab(ref, store);
            return;
        }

        // Order management
        if ("ordre_disband".equals(eventData.action)) {
            if (FamilleTab.handleOrdreDisband(ref, store)) refreshFamilleTab(ref, store);
            return;
        }
        if ("ordre_join".equals(eventData.action)) {
            if (FamilleTab.handleOrdreJoin(ref, store)) refreshFamilleTab(ref, store);
            return;
        }
        if ("ordre_invite".equals(eventData.action)) {
            com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            if (player != null) {
                player.getPlayerRef().sendMessage(Message.raw("Les invitations dans un ordre se font auprès de l'intendant de votre famille."));
            }
            return;
        }

        // Shop pagination
        if ("shop_prev".equals(eventData.action)) {
            ShopTab.prevPage(getPlayerUUID(ref, store));
            refreshShopTab(ref, store);
            return;
        }
        if ("shop_next".equals(eventData.action)) {
            ShopTab.nextPage(getPlayerUUID(ref, store));
            refreshShopTab(ref, store);
            return;
        }

        // Shop
        if ("shop_buy".equals(eventData.action) && eventData.param != null) {
            if (ShopTab.handleBuy(eventData.param, ref, store)) refreshShopTab(ref, store);
            return;
        }
        if ("shop_cancel".equals(eventData.action) && eventData.param != null) {
            if (ShopTab.handleCancel(eventData.param, ref, store)) refreshShopTab(ref, store);
            return;
        }

        // Black Market
        if ("bm_prev".equals(eventData.action)) { BlackMarketTab.prevPage(getPlayerUUID(ref, store)); refreshBlackMarketTab(ref, store); return; }
        if ("bm_next".equals(eventData.action)) { BlackMarketTab.nextPage(getPlayerUUID(ref, store)); refreshBlackMarketTab(ref, store); return; }
        if ("bm_buy".equals(eventData.action) && eventData.param != null) {
            if (BlackMarketTab.handleBuy(eventData.param, ref, store)) refreshBlackMarketTab(ref, store); return;
        }
        if ("bm_cancel".equals(eventData.action) && eventData.param != null) {
            if (BlackMarketTab.handleCancel(eventData.param, ref, store)) refreshBlackMarketTab(ref, store); return;
        }

        // Echange invite
        if ("ech_invite".equals(eventData.action) && eventData.param != null) {
            if (EchangesTab.handleInvite(EldaniorLogger.parseSafeInt(eventData.param, -1), ref, store)) {
                refreshEchangesTab(ref, store);
            }
            return;
        }

        // Echange force (admin)
        if ("ech_force".equals(eventData.action) && eventData.param != null) {
            EchangesTab.handleForceOpen(EldaniorLogger.parseSafeInt(eventData.param, -1), ref, store);
            return;
        }

        // Wiki - pagination
        if ("wiki_prev".equals(eventData.action)) {
            if (WikiTab.handlePrev(getPlayerUUID(ref, store))) { switchTab("wiki", ref, store); }
            return;
        }
        if ("wiki_next".equals(eventData.action)) {
            if (WikiTab.handleNext(getPlayerUUID(ref, store))) { switchTab("wiki", ref, store); }
            return;
        }

        // Admin actions
        if (eventData.action.startsWith("admin_")) {
            // Admin actions moved to AdminScreen (/es admin)
            return;
        }

        // Runes (onglet Competences, vue Runes actives)
        if (eventData.action.startsWith("rune_")) {
            try {
                com.eldanior.system.gui.tabs.RunesTab.handle(ref, store, eventData.action, lastRawEvent);
            } catch (Exception e) {
                EldaniorLogger.error("[Runes] menu", e);
            }
            refreshCompetencesTab(ref, store);
            return;
        }

        // Skill pagination
        if ("skill_prev".equals(eventData.action)) {
            CompetencesTab.prevPage(getPlayerUUID(ref, store));
            refreshCompetencesTab(ref, store);
            return;
        }
        if ("skill_next".equals(eventData.action)) {
            CompetencesTab.nextPage(getPlayerUUID(ref, store));
            refreshCompetencesTab(ref, store);
            return;
        }
        // Skill tab switch (passive / active)
        if ("skill_tab_passive".equals(eventData.action)) {
            CompetencesTab.switchToPassives(getPlayerUUID(ref, store));
            refreshCompetencesTab(ref, store);
            return;
        }
        if ("skill_tab_active".equals(eventData.action)) {
            CompetencesTab.switchToActives(getPlayerUUID(ref, store));
            refreshCompetencesTab(ref, store);
            return;
        }
        // Filtre par theme de l'onglet Competences
        if ("skill_filter".equals(eventData.action) && eventData.param != null) {
            CompetencesTab.setFilter(getPlayerUUID(ref, store), eventData.param);
            refreshCompetencesTab(ref, store);
            return;
        }
        // Oublier un passif appris (deux clics)
        if ("skill_forget".equals(eventData.action) && eventData.param != null) {
            if (CompetencesTab.handleForget(eventData.param, ref, store)) {
                refreshCompetencesTab(ref, store);
            }
            return;
        }
        // Skill toggle
        if ("skill_toggle".equals(eventData.action) && eventData.param != null) {
            if (CompetencesTab.handleToggle(eventData.param, ref, store)) {
                refreshCompetencesTab(ref, store);
            }
            return;
        }

        // Guild accordion toggles
        if ("guild_toggle_members".equals(eventData.action)) {
            GuildeTab.handleToggleMembers(getPlayerUUID(ref, store));
            refreshGuildeTab(ref, store);
            return;
        }
        if ("guild_toggle_invite".equals(eventData.action)) {
            GuildeTab.handleToggleInvite(getPlayerUUID(ref, store));
            refreshGuildeTab(ref, store);
            return;
        }

        // Guild member pagination
        if ("guild_member_prev".equals(eventData.action)) {
            GuildeTab.handleMemberPrev(getPlayerUUID(ref, store));
            refreshGuildeTab(ref, store);
            return;
        }
        if ("guild_member_next".equals(eventData.action)) {
            GuildeTab.handleMemberNext(getPlayerUUID(ref, store));
            refreshGuildeTab(ref, store);
            return;
        }

        // Guild invite pagination
        if ("guild_invite_prev".equals(eventData.action)) {
            GuildeTab.handleInvitePrev(getPlayerUUID(ref, store));
            refreshGuildeTab(ref, store);
            return;
        }
        if ("guild_invite_next".equals(eventData.action)) {
            GuildeTab.handleInviteNext(getPlayerUUID(ref, store));
            refreshGuildeTab(ref, store);
            return;
        }

        // Guild open guilds pagination
        if ("guild_open_prev".equals(eventData.action)) {
            GuildeTab.handleOpenPrev(getPlayerUUID(ref, store));
            refreshGuildeTab(ref, store);
            return;
        }
        if ("guild_open_next".equals(eventData.action)) {
            GuildeTab.handleOpenNext(getPlayerUUID(ref, store));
            refreshGuildeTab(ref, store);
            return;
        }

        // Guild member actions
        if ("guild_promote".equals(eventData.action) && eventData.param != null) {
            if (GuildeTab.handlePromote(eventData.param, ref, store)) refreshGuildeTab(ref, store);
            return;
        }
        if ("guild_demote".equals(eventData.action) && eventData.param != null) {
            if (GuildeTab.handleDemote(eventData.param, ref, store)) refreshGuildeTab(ref, store);
            return;
        }
        if ("guild_kick".equals(eventData.action) && eventData.param != null) {
            if (GuildeTab.handleKick(eventData.param, ref, store)) refreshGuildeTab(ref, store);
            return;
        }

        // Guilde actions
        if ("guild_leave".equals(eventData.action)) {
            if (GuildeTab.handleLeave(ref, store)) refreshGuildeTab(ref, store);
            return;
        }
        if ("guild_disband".equals(eventData.action)) {
            if (GuildeTab.handleDisband(ref, store)) refreshGuildeTab(ref, store);
            return;
        }
        if ("guild_invite".equals(eventData.action) && eventData.param != null) {
            if (GuildeTab.handleInviteByIndex(eventData.param, ref, store)) refreshGuildeTab(ref, store);
            return;
        }
        if ("guild_toggle_recruitment".equals(eventData.action)) {
            if (GuildeTab.handleToggleRecruitment(ref, store)) refreshGuildeTab(ref, store);
            return;
        }
        if ("guild_join".equals(eventData.action) && eventData.param != null) {
            if (GuildeTab.handleJoinByIndex(eventData.param, ref, store)) refreshGuildeTab(ref, store);
            return;
        }

        // Groupe actions
        if ("grp_create".equals(eventData.action)) {
            if (GroupeTab.handleCreate(ref, store)) refreshGroupeTab(ref, store);
            return;
        }
        if ("grp_leave".equals(eventData.action)) {
            if (GroupeTab.handleLeave(ref, store)) refreshGroupeTab(ref, store);
            return;
        }
        if ("grp_disband".equals(eventData.action)) {
            if (GroupeTab.handleDisband(ref, store)) refreshGroupeTab(ref, store);
            return;
        }
        if ("grp_kick".equals(eventData.action) && eventData.param != null) {
            if (GroupeTab.handleKick(eventData.param, ref, store)) refreshGroupeTab(ref, store);
            return;
        }
        if ("grp_promote".equals(eventData.action) && eventData.param != null) {
            if (GroupeTab.handlePromote(eventData.param, ref, store)) refreshGroupeTab(ref, store);
            return;
        }
        if ("grp_invite".equals(eventData.action) && eventData.param != null) {
            if (GroupeTab.handleInviteByIndex(eventData.param, ref, store)) {
                refreshGroupeTab(ref, store);
            }
            return;
        }

        // Title equip
        if ("title_equip".equals(eventData.action) && eventData.param != null) {
            if (TitresTab.handleEquip(eventData.param, ref, store)) {
                refreshTitresTab(ref, store);
                // Also refresh profil tab sidebar
                refreshProfilTab(ref, store);
            }
        }
    }

    // handleAdminAction moved to AdminScreen.java

    private void refreshFamilleTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        FamilleTab.populate(update, ref, store);
        this.sendUpdate(update);
    }

    /** Rafraichit la sidebar + le profil dans un UICommandBuilder existant */
    private void appendProfilRefresh(UICommandBuilder update, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return;

        String playerName = getPlayerName(ref, store);

        // Sidebar
        update.set("#SidebarPlayerName.Text", playerName);
        update.set("#SidebarMoney.Text", money(data.getMoney()));
        update.set("#SidebarLevel.Text", "Niveau " + data.getLevel());

        // Onglets qui dependent du joueur
        if (!steward) tabVisibility(update, ref, store, data);

        // Profil tab
        ProfilTab.populate(update, ref, store, data, playerName);
    }

    private void refreshBlackMarketTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        BlackMarketTab.populate(update, ref, store);
        appendProfilRefresh(update, ref, store);
        this.sendUpdate(update);
    }

    private void refreshShopTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        ShopTab.populate(update, ref, store);
        appendProfilRefresh(update, ref, store);
        this.sendUpdate(update);
    }

    private void refreshCompetencesTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        CompetencesTab.populate(update, ref, store);
        this.sendUpdate(update);
        // Démarrer/arrêter le timer auto-refresh selon les cooldowns actifs
        if (CompetencesTab.hasCooldownActive()) {
            startCooldownRefresh(ref, store);
        } else {
            stopCooldownRefresh();
        }
    }

    private void startCooldownRefresh(Ref<EntityStore> ref, Store<EntityStore> store) {
        this.cachedRef = ref;
        this.cachedStore = store;
        if (cooldownRefreshTask != null && !cooldownRefreshTask.isCancelled()) return; // déjà actif
        cooldownRefreshTask = EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                // Update léger : uniquement les timers, sans relire le store
                UICommandBuilder update = new UICommandBuilder();
                CompetencesTab.updateCooldownTimers(update);
                try {
                    this.sendUpdate(update);
                } catch (Exception sendEx) {
                    // Page fermée, arrêter le timer
                    stopCooldownRefresh();
                    return;
                }
                // Auto-stop quand plus de cooldowns
                if (!CompetencesTab.hasCooldownActive()) {
                    stopCooldownRefresh();
                }
            } catch (Exception e) {
                EldaniorLogger.error("CooldownRefresh", e);
                stopCooldownRefresh();
            }
        }, 1, 1, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void stopCooldownRefresh() {
        if (cooldownRefreshTask != null) {
            cooldownRefreshTask.cancel(false);
            cooldownRefreshTask = null;
        }
    }

    private void refreshGuildeTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        GuildeTab.populate(update, ref, store);
        appendProfilRefresh(update, ref, store);
        this.sendUpdate(update);
    }

    private void refreshGroupeTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        GroupeTab.populate(update, ref, store);
        this.sendUpdate(update);
    }

    private void refreshTitresTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        TitresTab.populate(update, ref, store);
        appendProfilRefresh(update, ref, store);
        this.sendUpdate(update);
    }

    private void refreshEchangesTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        EchangesTab.populate(update, ref, store, false);
        this.sendUpdate(update);
    }

    /** Ouvrir le menu directement sur la poche (clic droit avec l'objet). */
    private boolean pocheOnOpen;

    public SystemScreen openOnPoche() {
        this.pocheOnOpen = true;
        return this;
    }

    /** Cour de l'Observateur (panneau #TabObservateur) : etat du panneau, un par menu ouvert. */
    private com.eldanior.system.classes.observateur.ObservateurCourTab cour;
    /** Ouvrir le menu directement sur la Cour (Sans Visage « Ma charge », /es observateur menu). */
    private boolean observateurOnOpen;

    private com.eldanior.system.classes.observateur.ObservateurCourTab cour() {
        if (cour == null) cour = new com.eldanior.system.classes.observateur.ObservateurCourTab(playerRef);
        return cour;
    }

    public SystemScreen openOnObservateur() {
        this.observateurOnOpen = true;
        return this;
    }

    /**
     * Panneau de la Cour a la place de celui de l'onglet ouvert (2026-11-19). Bouton allume s'il est affiche (souverain, admin) ;
     * l'Observateur, sans bouton, y arrive par Sans Visage ou la commande.
     */
    private boolean showObservateur(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData data) {
        if (!com.eldanior.system.classes.observateur.ObservateurCourTab.allowed(playerRef, data)) return false;
        stopCooldownRefresh();
        for (String tab : TAB_IDS) ui.set("#Tab" + tab + ".Visible", "Observateur".equals(tab));
        ui.set("#TabPoche.Visible", false);
        markTab(ui, "Observateur");
        cour().populate(ui, ref, store);
        return true;
    }

    /**
     * Panneau de la poche (2026-11-16) a la place de celui de l'onglet ouvert, sans quitter le menu : tous les onglets caches,
     * aucun bouton de categorie allume.
     */
    private void showPoche(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        stopCooldownRefresh();
        for (String tab : TAB_IDS) {
            ui.set("#Tab" + tab + ".Visible", false);
            ui.set("#TabOn" + tab + ".Visible", false);
        }
        ui.set("#TabPoche.Visible", true);
        ui.set("#ContentTitle.Text", "Poche dimensionnelle");
        com.eldanior.system.poche.PocheTab.populate(ui, ref, store);
        com.eldanior.system.poche.PocheTab.slot(ui, store.getComponent(ref, PlayerRef.getComponentType()));
    }

    private void switchTab(String tabName, Ref<EntityStore> ref, Store<EntityStore> store) {
        // Arrêter le refresh cooldown si on quitte l'onglet compétences
        if (!"competences".equals(tabName)) {
            stopCooldownRefresh();
        }

        boolean known = false;                                           // onglets retires le 2026-11-12 : ignorer
        for (String tab : TAB_IDS) known |= tab.equalsIgnoreCase(tabName);
        if (!known) return;

        if ("observateur".equals(tabName)) {                             // Cour de l'Observateur (2026-11-19)
            UICommandBuilder u = new UICommandBuilder();
            if (showObservateur(u, ref, store, store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType()))) sendUpdate(u);
            return;
        }

        UICommandBuilder update = new UICommandBuilder();

        // Hide all tabs
        for (String tab : TAB_IDS) {
            update.set("#Tab" + tab + ".Visible", false);
        }
        update.set("#TabPoche.Visible", false);                          // panneau de la poche (2026-11-16)

        // Show target tab (find matching TAB_ID case-insensitive)
        String targetId = tabName.substring(0, 1).toUpperCase() + tabName.substring(1);
        for (String tab : TAB_IDS) {
            if (tab.equalsIgnoreCase(tabName)) { targetId = tab; break; }
        }
        update.set("#Tab" + targetId + ".Visible", true);
        markTab(update, targetId);

        // Refresh tab data
        if ("blackmarket".equals(tabName)) {
            BlackMarketTab.populate(update, ref, store);
        }
        if ("shop".equals(tabName)) {
            ShopTab.populate(update, ref, store);
        }
        // admin tab removed — /es admin
        if ("competences".equals(tabName)) {
            CompetencesTab.populate(update, ref, store);
            if (CompetencesTab.hasCooldownActive()) {
                // Defer le start pour après l'envoi de l'update
                this.cachedRef = ref;
                this.cachedStore = store;
                EldaniorLogger.SCHEDULER.schedule(() -> startCooldownRefresh(ref, store), 500, java.util.concurrent.TimeUnit.MILLISECONDS);
            }
        }
        if ("quetes".equals(tabName)) {
            com.eldanior.system.gui.tabs.QuetesEnCoursTab.populate(update, ref, store);
        }
        if ("guilde".equals(tabName)) {
            GuildeTab.populate(update, ref, store);
        }
        if ("famille".equals(tabName)) {
            FamilleTab.populate(update, ref, store);
        }
        if ("groupe".equals(tabName)) {
            GroupeTab.populate(update, ref, store);
        }
        if ("titres".equals(tabName)) {
            TitresTab.populate(update, ref, store);
        }
        if ("wiki".equals(tabName)) {
            WikiTab.populate(update, ref, store);
        }
        if ("echanges".equals(tabName)) {
            com.hypixel.hytale.server.core.entity.entities.Player pc = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            boolean adm = pc != null && pc.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION);
            EchangesTab.populate(update, ref, store, adm);
        }
        if ("profil".equals(tabName)) {
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = store.getComponent(ref, type);
            if (data != null) {
                String playerName = getPlayerName(ref, store);
                ProfilTab.populate(update, ref, store, data, playerName);
            }
        }

        this.sendUpdate(update);
    }

    private void refreshProfilTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return;

        UICommandBuilder update = new UICommandBuilder();
        String playerName = getPlayerName(ref, store);
        ProfilTab.populate(update, ref, store, data, playerName);

        // Update sidebar too
        update.set("#SidebarMoney.Text", money(data.getMoney()));

        this.sendUpdate(update);
    }

    private String getPlayerName(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef info = store.getComponent(ref, PlayerRef.getComponentType());
        return info != null ? info.getUsername() : "Inconnu";
    }

    private java.util.UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        try { return UUIDExtractor.getUUID(pRef); } catch (Exception e) { return null; }
    }

    // deleteDataFile, cyclePrice, cycleRentPrice moved to AdminScreen.java

    public static class SystemEventData {
        public static final BuilderCodec<SystemEventData> CODEC = BuilderCodec.builder(SystemEventData.class, SystemEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("Param", Codec.STRING), (d, v) -> d.param = v, d -> d.param).add()
                .append(new KeyedCodec<>("InputText", Codec.STRING), (d, v) -> d.inputText = v, d -> d.inputText).add()
                .append(new KeyedCodec<>(com.eldanior.system.classes.observateur.ObservateurCourTab.ALIAS_KEY, Codec.STRING), (d, v) -> d.ocAlias = v, d -> d.ocAlias).add()
                .build();
        public String action;
        public String param;
        public String inputText;
        /** Nom d'emprunt saisi dans la Cour de l'Observateur. */
        public String ocAlias;
    }
}