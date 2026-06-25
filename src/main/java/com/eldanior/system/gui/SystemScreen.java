package com.eldanior.system.gui;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
// AdminTab moved to AdminScreen
import com.eldanior.system.gui.tabs.BlackMarketTab;
import com.eldanior.system.gui.tabs.QuestTab;
import com.eldanior.system.gui.tabs.DuelTab;
import com.eldanior.system.gui.tabs.ShopTab;
import com.eldanior.system.gui.tabs.ClassementsTab;
import com.eldanior.system.gui.tabs.FamilleTab;
import com.eldanior.system.gui.tabs.WikiTab;
import com.eldanior.system.gui.tabs.CompetencesTab;
import com.eldanior.system.gui.tabs.GuildeTab;
import com.eldanior.system.gui.tabs.GroupeTab;
import com.eldanior.system.gui.tabs.InventaireTab;
import com.eldanior.system.gui.tabs.TerritoiresTab;
import com.eldanior.system.gui.tabs.ProprietesTab;
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
            "Profil", "Inventaire", "Competences", "Guilde", "Groupe", "Famille", "Titres", "Quetes", "Classements", "Duel", "Shop", "BlackMarket", "Territoires", "Proprietes", "Echanges", "Wiki"
    };

    // Timer pour refresh automatique des cooldowns dans l'onglet compétences
    private java.util.concurrent.ScheduledFuture<?> cooldownRefreshTask = null;
    private Ref<EntityStore> cachedRef = null;
    private Store<EntityStore> cachedStore = null;

    public SystemScreen(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, SystemEventData.CODEC);
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

        // Sidebar info
        ui.set("#SidebarPlayerName.Text", playerName);
        ui.set("#SidebarPlayerInfo.Text", data.getPlayerClass() + " - Niv." + data.getLevel());
        ui.set("#SidebarMoney.Text", data.getMoney() + " Or");
        ui.set("#SidebarLevel.Text", "Niveau " + data.getLevel());

        // Admin separe — via /es admin
        PlayerRef pRefCheck = store.getComponent(ref, PlayerRef.getComponentType());
        com.hypixel.hytale.server.core.entity.entities.Player playerCheck = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());

        // Famille visible si Duc, Marquis ou Roi (ou si a deja une famille)
        String rankStr = data.getNobilityRank();
        String familyId = data.getNobleFamilyId();
        boolean showFamille = (familyId != null && !familyId.isEmpty());
        if (!showFamille && rankStr != null) {
            showFamille = "DUC".equals(rankStr) || "MARQUIS".equals(rankStr) || "ROI".equals(rankStr);
        }
        if (showFamille) {
            ui.set("#TabBtnFamille.Visible", true);
            FamilleTab.populate(ui, ref, store);
        }

        // Territoires visible si Comte+ (Comte, Duc, Marquis, Roi)
        boolean showTerritoires = rankStr != null && ("COMTE".equals(rankStr) || "DUC".equals(rankStr) || "MARQUIS".equals(rankStr) || "ROI".equals(rankStr));
        if (showTerritoires) {
            ui.set("#TabBtnTerritoires.Visible", true);
            TerritoiresTab.populate(ui, ref, store);
        }

        // Proprietes visible pour tous
        ProprietesTab.populate(ui, ref, store);

        // Echanges visible pour Marchands, Dragons et Admins
        String classType = data.getPlayerClassId();
        com.eldanior.system.classes.models.ClassModel classModel = com.eldanior.system.classes.ClassManager.get(classType);
        boolean showEchanges = classModel != null && (classModel.getType() == com.eldanior.system.config.configs.ClassType.MERCHANT
            || classModel.getType() == com.eldanior.system.config.configs.ClassType.DRAGON);
        if (!showEchanges && playerCheck != null && playerCheck.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION)) showEchanges = true;
        if (showEchanges) {
            ui.set("#TabBtnEchanges.Visible", true);
            EchangesTab.populate(ui, ref, store, false);
        }

        // Populate profil tab (default)
        ProfilTab.populate(ui, ref, store, data, playerName);

        // Populate inventaire tab (pre-load slots)
        InventaireTab.populate(ui, events, ref, store);

        // Populate titres tab
        TitresTab.populate(ui, ref, store);

        // Populate competences tab
        CompetencesTab.populate(ui, ref, store);
        for (int i = 0; i < CompetencesTab.MAX_SKILL_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#SkillToggle" + i,
                    EventData.of("Action", "skill_toggle").append("Param", String.valueOf(i)));
        }
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SkillBtnPrev", EventData.of("Action", "skill_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SkillBtnNext", EventData.of("Action", "skill_next"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SkillTabPassive", EventData.of("Action", "skill_tab_passive"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SkillTabActive", EventData.of("Action", "skill_tab_active"));

        // Populate guilde tab
        GuildeTab.populate(ui, ref, store);

        // Guilde buttons
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GuildeBtnCreate", EventData.of("Action", "guild_create"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GuildeBtnLeave", EventData.of("Action", "guild_leave"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GuildeBtnDisband", EventData.of("Action", "guild_disband"));
        for (int i = 0; i < GuildeTab.MAX_INVITE_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#GInviteBtn" + i, EventData.of("Action", "guild_invite").append("Param", String.valueOf(i)));
        }
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GuildeBtnToggleRecruitment", EventData.of("Action", "guild_toggle_recruitment"));
        for (int i = 0; i < GuildeTab.MAX_OPEN_GUILD_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#GOpenGuildBtn" + i, EventData.of("Action", "guild_join").append("Param", String.valueOf(i)));
        }

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
            events.addEventBinding(CustomUIEventBindingType.Activating, "#FamInvBtn" + i,
                    EventData.of("Action", "fam_invite").append("Param", String.valueOf(i)));
        }

        // Populate quest tab
        QuestTab.populate(ui, ref, store);
        for (int i = 0; i < QuestTab.MAX_QUEST_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#QSlotBtnAccept" + i, EventData.of("Action", "quest_accept").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#QSlotBtnActivate" + i, EventData.of("Action", "quest_activate").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#QSlotBtnClaim" + i, EventData.of("Action", "quest_claim").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#QSlotBtnAbandon" + i, EventData.of("Action", "quest_abandon").append("Param", String.valueOf(i)));
        }
        events.addEventBinding(CustomUIEventBindingType.Activating, "#QActiveBtnAbandon", EventData.of("Action", "quest_abandon_active"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnQuetes", EventData.of("Action", "tab_quetes"));

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
        boolean showBM = data.isPK() || (playerCheck != null && playerCheck.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION));
        ui.set("#TabBtnBlackMarket.Visible", showBM);
        if (showBM) {
            BlackMarketTab.populate(ui, ref, store);
        }
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnBlackMarket", EventData.of("Action", "tab_blackmarket"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BMBtnPrev", EventData.of("Action", "bm_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BMBtnNext", EventData.of("Action", "bm_next"));
        for (int i = 0; i < BlackMarketTab.MAX_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BMBtnBuy" + i, EventData.of("Action", "bm_buy").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BMBtnCancel" + i, EventData.of("Action", "bm_cancel").append("Param", String.valueOf(i)));
        }

        // Populate duel tab
        DuelTab.populate(ui, ref, store);
        for (int i = 0; i < DuelTab.MAX_INVITE_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#DuelChallenge" + i,
                    EventData.of("Action", "duel_challenge").append("Param", String.valueOf(i)));
        }

        // Echanges invite buttons
        for (int i = 0; i < EchangesTab.MAX_TRADE_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#EchBtn" + i,
                    EventData.of("Action", "ech_invite").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#EchForceBtn" + i,
                    EventData.of("Action", "ech_force").append("Param", String.valueOf(i)));
        }

        // Territoires - pagination
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TerrBtnPrev", EventData.of("Action", "terr_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TerrBtnNext", EventData.of("Action", "terr_next"));
        // Territoires - clic sur un territoire (selection)
        for (int i = 0; i < TerritoiresTab.MAX_TERR_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#TerrSlot" + i,
                    EventData.of("Action", "terr_select").append("Param", String.valueOf(i)));
        }
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TDetBtnProt", EventData.of("Action", "terr_prot"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TDetBtnFamily", EventData.of("Action", "terr_family"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TDetBtnGuild", EventData.of("Action", "terr_guild"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TDetBtnDel", EventData.of("Action", "terr_del"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TDetBtnPvp", EventData.of("Action", "terr_pvp"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TDetBtnTax", EventData.of("Action", "terr_tax"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TDetBtnSellCity", EventData.of("Action", "terr_sell_city"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TDetBtnTransfer", EventData.of("Action", "terr_transfer"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TDetBtnInvasion", EventData.of("Action", "terr_invasion"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TDetBtnDecretMarquis", EventData.of("Action", "terr_decret").append("Param", "MARQUIS"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TDetBtnDecretDuc", EventData.of("Action", "terr_decret").append("Param", "DUC"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TDetBtnDecretComte", EventData.of("Action", "terr_decret").append("Param", "COMTE"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TDetBtnDecretBaron", EventData.of("Action", "terr_decret").append("Param", "BARON"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TDetBtnDecretChevalier", EventData.of("Action", "terr_decret").append("Param", "CHEVALIER"));

        // Proprietes - pagination
        events.addEventBinding(CustomUIEventBindingType.Activating, "#POwnPrev", EventData.of("Action", "prop_owned_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#POwnNext", EventData.of("Action", "prop_owned_next"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PAvPrev", EventData.of("Action", "prop_avail_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PAvNext", EventData.of("Action", "prop_avail_next"));
        // Proprietes - clic sur un bien (selection)
        for (int i = 0; i < ProprietesTab.MAX_OWNED_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#POwnSlot" + i,
                    EventData.of("Action", "prop_select").append("Param", String.valueOf(i)));
        }
        // Proprietes - disponibles
        for (int i = 0; i < ProprietesTab.MAX_AVAILABLE_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#PAvBtnBuy" + i,
                    EventData.of("Action", "prop_buy").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#PAvBtnRent" + i,
                    EventData.of("Action", "prop_rent").append("Param", String.valueOf(i)));
        }
        // Proprietes - villes (Comte + Guilde)
        for (int i = 0; i < ProprietesTab.MAX_CITY_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#CityBtnBuy" + i,
                    EventData.of("Action", "prop_buy_city").append("Param", String.valueOf(i)));
        }
        // Proprietes - detail actions
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PDetBtnProt", EventData.of("Action", "prop_prot"));
        // prop_setprice and prop_setrent removed — admin-only, handled in AdminScreen via /es admin
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PDetBtnFamily", EventData.of("Action", "prop_family"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PDetBtnSell", EventData.of("Action", "prop_sellback"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PDetBtnQuit", EventData.of("Action", "prop_quit"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PDetBtnRenew", EventData.of("Action", "prop_renew"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PDetBtnRentOut", EventData.of("Action", "prop_rentout"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PDetBtnCancelRent", EventData.of("Action", "prop_cancelrent"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PDetBtnDel", EventData.of("Action", "prop_del"));
        // Invite buttons dans le detail
        for (int i = 0; i < ProprietesTab.MAX_INVITE_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#PDetInvBtn" + i,
                    EventData.of("Action", "prop_invite").append("Param", String.valueOf(i)));
        }

        // Populate classements
        ClassementsTab.populate(ui, ref, store);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#RkBtnMobs", EventData.of("Action", "rk_mobs"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#RkBtnPvP", EventData.of("Action", "rk_pvp"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#RkBtnGuildFam", EventData.of("Action", "rk_guildfam"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#RkBtnDuel", EventData.of("Action", "rk_duel"));

        // Admin separe — via /es admin

        // === EVENT BINDINGS ===

        // Tab navigation
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnProfil",      EventData.of("Action", "tab_profil"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnInventaire",  EventData.of("Action", "tab_inventaire"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnCompetences", EventData.of("Action", "tab_competences"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnGuilde",      EventData.of("Action", "tab_guilde"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnGroupe",      EventData.of("Action", "tab_groupe"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnFamille",     EventData.of("Action", "tab_famille"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnTitres",      EventData.of("Action", "tab_titres"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnClassements", EventData.of("Action", "tab_classements"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnDuel",        EventData.of("Action", "tab_duel"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnShop",        EventData.of("Action", "tab_shop"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnTerritoires", EventData.of("Action", "tab_territoires"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnProprietes", EventData.of("Action", "tab_proprietes"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnEchanges",   EventData.of("Action", "tab_echanges"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnWiki",       EventData.of("Action", "tab_wiki"));
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
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull SystemEventData eventData) {
        if (eventData.action == null) return;

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

        // Inventory actions
        if ("inv_take".equals(eventData.action) && eventData.param != null) {
            if (InventaireTab.handleTake(ref, store, EldaniorLogger.parseSafeInt(eventData.param, -1))) {
                refreshInventaireTab(ref, store);
            }
            return;
        }
        if ("inv_deposit".equals(eventData.action) && eventData.param != null) {
            if (InventaireTab.handleDeposit(ref, store, EldaniorLogger.parseSafeInt(eventData.param, -1))) {
                refreshInventaireTab(ref, store);
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

        // Quest actions
        if ("quest_accept".equals(eventData.action) && eventData.param != null) {
            if (QuestTab.handleAccept(eventData.param, ref, store)) refreshQuestTab(ref, store);
            return;
        }
        if ("quest_activate".equals(eventData.action) && eventData.param != null) {
            if (QuestTab.handleActivate(eventData.param, ref, store)) {
                refreshQuestTab(ref, store);
                // Set le QuestHud
                setQuestHud(ref, store);
            }
            return;
        }
        if ("quest_claim".equals(eventData.action) && eventData.param != null) {
            if (QuestTab.handleClaim(eventData.param, ref, store)) refreshQuestTab(ref, store);
            return;
        }
        if ("quest_abandon".equals(eventData.action) && eventData.param != null) {
            if (QuestTab.handleAbandon(eventData.param, ref, store)) refreshQuestTab(ref, store);
            return;
        }
        if ("quest_abandon_active".equals(eventData.action)) {
            if (QuestTab.handleAbandonActive(ref, store)) refreshQuestTab(ref, store);
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

        // Duel challenge
        if ("duel_challenge".equals(eventData.action) && eventData.param != null) {
            if (DuelTab.handleChallenge(eventData.param, ref, store)) {
                refreshDuelTab(ref, store);
            }
            return;
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

        // Territoires - pagination
        if ("terr_prev".equals(eventData.action)) {
            if (TerritoiresTab.handlePrev(getPlayerUUID(ref, store))) refreshTerritoiresTab(ref, store);
            return;
        }
        if ("terr_next".equals(eventData.action)) {
            if (TerritoiresTab.handleNext(getPlayerUUID(ref, store))) refreshTerritoiresTab(ref, store);
            return;
        }
        // Territoires - selection
        if ("terr_select".equals(eventData.action) && eventData.param != null) {
            if (TerritoiresTab.handleSelect(EldaniorLogger.parseSafeInt(eventData.param, -1), getPlayerUUID(ref, store))) {
                refreshTerritoiresTab(ref, store);
            }
            return;
        }
        if ("terr_prot".equals(eventData.action)) {
            if (TerritoiresTab.handleToggleProtection(getPlayerUUID(ref, store))) refreshTerritoiresTab(ref, store);
            return;
        }
        if ("terr_family".equals(eventData.action)) {
            com.hypixel.hytale.server.core.entity.entities.Player p = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            if (p != null) {
                p.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("§6Tapez dans le chat :"));
                p.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("§f/es parcel assign <familyId> _"));
            }
            return;
        }
        if ("terr_guild".equals(eventData.action)) {
            com.hypixel.hytale.server.core.entity.entities.Player p = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            if (p != null) {
                p.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("§6Tapez dans le chat :"));
                p.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("§f/es parcel assignguild <guildId> _"));
            }
            return;
        }
        if ("terr_del".equals(eventData.action)) {
            if (TerritoiresTab.handleDelete(getPlayerUUID(ref, store))) refreshTerritoiresTab(ref, store);
            return;
        }
        if ("terr_sell_city".equals(eventData.action)) {
            if (TerritoiresTab.handleSellCity(ref, store)) {
                refreshTerritoiresTab(ref, store);
                refreshProfilTab(ref, store);
            }
            return;
        }
        if ("terr_pvp".equals(eventData.action)) {
            if (TerritoiresTab.handleTogglePvp(getPlayerUUID(ref, store))) refreshTerritoiresTab(ref, store);
            return;
        }
        if ("terr_tax".equals(eventData.action)) {
            if (TerritoiresTab.handleCollectTax(getPlayerUUID(ref, store))) {
                refreshTerritoiresTab(ref, store);
                com.hypixel.hytale.server.core.entity.entities.Player p = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                if (p != null) p.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("§a§lImpots collectes !"));
            }
            return;
        }
        if ("terr_transfer".equals(eventData.action)) {
            if (TerritoiresTab.handleTransferTreasury(getPlayerUUID(ref, store))) {
                refreshTerritoiresTab(ref, store);
                com.hypixel.hytale.server.core.entity.entities.Player p = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                if (p != null) p.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("§a§lTresorerie transferee !"));
            }
            return;
        }
        if ("terr_decret".equals(eventData.action) && eventData.param != null) {
            if (TerritoiresTab.handleGiveDecret(eventData.param, ref, store)) {
                refreshTerritoiresTab(ref, store);
            }
            return;
        }
        if ("terr_invasion".equals(eventData.action)) {
            com.hypixel.hytale.server.core.entity.entities.Player p = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            if (p != null) p.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("§c§lINVASION LANCEE ! §7(fonctionnalite en cours de developpement)"));
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

        // Proprietes - pagination
        if ("prop_owned_prev".equals(eventData.action)) {
            if (ProprietesTab.handleOwnedPrev(getPlayerUUID(ref, store))) refreshProprietesTab(ref, store);
            return;
        }
        if ("prop_owned_next".equals(eventData.action)) {
            if (ProprietesTab.handleOwnedNext(getPlayerUUID(ref, store))) refreshProprietesTab(ref, store);
            return;
        }
        if ("prop_avail_prev".equals(eventData.action)) {
            if (ProprietesTab.handleAvailablePrev(getPlayerUUID(ref, store))) refreshProprietesTab(ref, store);
            return;
        }
        if ("prop_avail_next".equals(eventData.action)) {
            if (ProprietesTab.handleAvailableNext(getPlayerUUID(ref, store))) refreshProprietesTab(ref, store);
            return;
        }

        // Proprietes - selection d'un bien
        if ("prop_select".equals(eventData.action) && eventData.param != null) {
            if (ProprietesTab.handleSelect(EldaniorLogger.parseSafeInt(eventData.param, -1), getPlayerUUID(ref, store))) {
                refreshProprietesTab(ref, store);
            }
            return;
        }
        // Proprietes - acheter ville
        if ("prop_buy_city".equals(eventData.action) && eventData.param != null) {
            if (ProprietesTab.handleBuyCity(EldaniorLogger.parseSafeInt(eventData.param, -1), ref, store)) {
                refreshProprietesTab(ref, store);
                refreshProfilTab(ref, store);
            }
            return;
        }
        // Proprietes - acheter/louer
        if ("prop_buy".equals(eventData.action) && eventData.param != null) {
            if (ProprietesTab.handleBuy(EldaniorLogger.parseSafeInt(eventData.param, -1), ref, store)) {
                refreshProprietesTab(ref, store);
                refreshProfilTab(ref, store);
            }
            return;
        }
        if ("prop_rent".equals(eventData.action) && eventData.param != null) {
            if (ProprietesTab.handleRent(EldaniorLogger.parseSafeInt(eventData.param, -1), ref, store)) {
                refreshProprietesTab(ref, store);
                refreshProfilTab(ref, store);
            }
            return;
        }
        // Proprietes - detail actions
        if ("prop_prot".equals(eventData.action)) {
            if (ProprietesTab.handleToggleProtection(getPlayerUUID(ref, store))) refreshProprietesTab(ref, store);
            return;
        }
        if ("prop_family".equals(eventData.action)) {
            // Prefill la commande dans le chat
            com.hypixel.hytale.server.core.entity.entities.Player p = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            if (p != null) {
                p.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("§6Tapez dans le chat :"));
                p.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("§f/es parcel assign <familyId> _"));
            }
            return;
        }
        if ("prop_quit".equals(eventData.action)) {
            if (ProprietesTab.handleQuitRental(getPlayerUUID(ref, store))) {
                refreshProprietesTab(ref, store);
            }
            return;
        }
        if ("prop_renew".equals(eventData.action)) {
            if (ProprietesTab.handleRenew(ref, store)) {
                refreshProprietesTab(ref, store);
                refreshProfilTab(ref, store);
            }
            return;
        }
        if ("prop_rentout".equals(eventData.action)) {
            if (ProprietesTab.handleRentOut(getPlayerUUID(ref, store))) {
                refreshProprietesTab(ref, store);
            }
            return;
        }
        if ("prop_cancelrent".equals(eventData.action)) {
            if (ProprietesTab.handleCancelRent(getPlayerUUID(ref, store))) {
                refreshProprietesTab(ref, store);
            }
            return;
        }
        if ("prop_sellback".equals(eventData.action)) {
            if (ProprietesTab.handleSellSelected(getPlayerUUID(ref, store))) {
                refreshProprietesTab(ref, store);
            }
            return;
        }
        if ("prop_invite".equals(eventData.action) && eventData.param != null) {
            if (ProprietesTab.handleInvite(EldaniorLogger.parseSafeInt(eventData.param, -1), getPlayerUUID(ref, store))) {
                refreshProprietesTab(ref, store);
            }
            return;
        }
        if ("prop_del".equals(eventData.action)) {
            if (ProprietesTab.handleDeleteSelected(getPlayerUUID(ref, store))) {
                refreshProprietesTab(ref, store);
            }
            return;
        }

        // Ranking category switch
        if (eventData.action.startsWith("rk_")) {
            String cat = eventData.action.substring(3);
            UICommandBuilder update = new UICommandBuilder();
            ClassementsTab.switchCategory(cat, update, getPlayerUUID(ref, store));
            this.sendUpdate(update);
            return;
        }

        // Admin actions
        if (eventData.action.startsWith("admin_")) {
            // Admin actions moved to AdminScreen (/es admin)
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
        // Skill toggle
        if ("skill_toggle".equals(eventData.action) && eventData.param != null) {
            if (CompetencesTab.handleToggle(eventData.param, ref, store)) {
                refreshCompetencesTab(ref, store);
            }
            return;
        }

        // Guilde actions
        if ("guild_create".equals(eventData.action)) {
            com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            if (player != null) {
                player.getPlayerRef().sendMessage(Message.raw("§eTapez dans le chat : §f/es guildcreate <nom> <tag>"));
            }
            return;
        }
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

    private void refreshQuestTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        QuestTab.populate(update, ref, store);
        appendProfilRefresh(update, ref, store);
        this.sendUpdate(update);
    }

    private void setQuestHud(Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (player == null || pRef == null) return;
                        java.util.UUID uuid = UUIDExtractor.getUUID(pRef);

            var currentHud = player.getHudManager().getCustomHud("combined_hud");
            if (currentHud instanceof com.eldanior.system.hud.CombinedHud) {
                currentHud.show(); // Refresh
            } else {
                var data = store.getComponent(ref, com.eldanior.system.EldaniorSystem.get().getPlayerLevelDataType());
                com.eldanior.system.hud.CombinedHud hud = new com.eldanior.system.hud.CombinedHud(pRef, uuid);
                hud.setCachedData(data, player);
                player.getHudManager().addCustomHud(pRef, hud);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /** Rafraichit la sidebar + le profil dans un UICommandBuilder existant */
    private void appendProfilRefresh(UICommandBuilder update, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return;

        String playerName = getPlayerName(ref, store);

        // Sidebar
        update.set("#SidebarPlayerName.Text", playerName);
        update.set("#SidebarPlayerInfo.Text", data.getPlayerClass() + " - Niv." + data.getLevel());
        update.set("#SidebarMoney.Text", data.getMoney() + " Or");
        update.set("#SidebarLevel.Text", "Niveau " + data.getLevel());

        // Famille visible si Duc, Marquis ou Roi
        String rankStr = data.getNobilityRank();
        String familyId = data.getNobleFamilyId();
        boolean showFamille = (familyId != null && !familyId.isEmpty());
        if (!showFamille && rankStr != null) {
            showFamille = "DUC".equals(rankStr) || "MARQUIS".equals(rankStr) || "ROI".equals(rankStr);
        }
        update.set("#TabBtnFamille.Visible", showFamille);

        // Territoires visible si Comte+ ou si possede une ville/territoire via guilde
        boolean showTerritoires = rankStr != null && ("COMTE".equals(rankStr) || "DUC".equals(rankStr) || "MARQUIS".equals(rankStr) || "ROI".equals(rankStr));
        update.set("#TabBtnTerritoires.Visible", showTerritoires);

        // Echanges visible pour Marchands, Dragons et Admins
        com.eldanior.system.classes.models.ClassModel classModel = com.eldanior.system.classes.ClassManager.get(data.getPlayerClassId());
        boolean showEchanges = classModel != null && (classModel.getType() == com.eldanior.system.config.configs.ClassType.MERCHANT
            || classModel.getType() == com.eldanior.system.config.configs.ClassType.DRAGON);
        if (!showEchanges) {
            com.hypixel.hytale.server.core.entity.entities.Player pCheck = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            if (pCheck != null && pCheck.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION)) showEchanges = true;
        }
        update.set("#TabBtnEchanges.Visible", showEchanges);

        // Profil tab
        ProfilTab.populate(update, ref, store, data, playerName);
    }

    private void refreshDuelTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        DuelTab.populate(update, ref, store);
        appendProfilRefresh(update, ref, store);
        this.sendUpdate(update);
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

    private void refreshInventaireTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        InventaireTab.refreshOnly(update, ref, store);
        this.sendUpdate(update);
    }

    private void refreshEchangesTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        EchangesTab.populate(update, ref, store, false);
        this.sendUpdate(update);
    }

    private void refreshTerritoiresTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        TerritoiresTab.populate(update, ref, store);
        this.sendUpdate(update);
    }

    private void refreshProprietesTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        ProprietesTab.populate(update, ref, store);
        this.sendUpdate(update);
    }

    private void switchTab(String tabName, Ref<EntityStore> ref, Store<EntityStore> store) {
        // Arrêter le refresh cooldown si on quitte l'onglet compétences
        if (!"competences".equals(tabName)) {
            stopCooldownRefresh();
        }

        UICommandBuilder update = new UICommandBuilder();

        // Hide all tabs
        for (String tab : TAB_IDS) {
            update.set("#Tab" + tab + ".Visible", false);
        }

        // Show target tab (find matching TAB_ID case-insensitive)
        String targetId = tabName.substring(0, 1).toUpperCase() + tabName.substring(1);
        for (String tab : TAB_IDS) {
            if (tab.equalsIgnoreCase(tabName)) { targetId = tab; break; }
        }
        update.set("#Tab" + targetId + ".Visible", true);

        // Refresh tab data
        if ("inventaire".equals(tabName)) {
            InventaireTab.refreshOnly(update, ref, store);
        }
        if ("blackmarket".equals(tabName)) {
            BlackMarketTab.populate(update, ref, store);
        }
        if ("quetes".equals(tabName)) {
            QuestTab.populate(update, ref, store);
        }
        if ("shop".equals(tabName)) {
            ShopTab.populate(update, ref, store);
        }
        if ("duel".equals(tabName)) {
            DuelTab.populate(update, ref, store);
        }
        if ("classements".equals(tabName)) {
            ClassementsTab.populate(update, ref, store);
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
        if ("territoires".equals(tabName)) {
            TerritoiresTab.populate(update, ref, store);
        }
        if ("proprietes".equals(tabName)) {
            ProprietesTab.populate(update, ref, store);
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
        update.set("#SidebarPlayerInfo.Text", data.getPlayerClass() + " - Niv." + data.getLevel());
        update.set("#SidebarMoney.Text", data.getMoney() + " Or");

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
                .build();
        public String action;
        public String param;
        public String inputText;
    }
}