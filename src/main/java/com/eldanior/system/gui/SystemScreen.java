package com.eldanior.system.gui;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.gui.tabs.AdminTab;
import com.eldanior.system.gui.tabs.FamilleTab;
import com.eldanior.system.gui.tabs.CompetencesTab;
import com.eldanior.system.gui.tabs.GuildeTab;
import com.eldanior.system.gui.tabs.GroupeTab;
import com.eldanior.system.gui.tabs.InventaireTab;
import com.eldanior.system.gui.tabs.ProfilTab;
import com.eldanior.system.gui.tabs.TitresTab;
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
            "Profil", "Inventaire", "Competences", "Guilde", "Groupe", "Famille", "Titres", "Admin"
    };

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

        // Admin visible si permission
        PlayerRef pRefCheck = store.getComponent(ref, PlayerRef.getComponentType());
        com.hypixel.hytale.server.core.entity.entities.Player playerCheck = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (playerCheck != null && playerCheck.hasPermission("eldanior.command.setlevel")) {
            ui.set("#TabBtnAdmin.Visible", true);
            AdminTab.populate(ui, ref, store);
        }

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

        // Populate guilde tab
        GuildeTab.populate(ui, ref, store);

        // Guilde buttons
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GuildeBtnCreate", EventData.of("Action", "guild_create"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GuildeBtnLeave", EventData.of("Action", "guild_leave"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#GuildeBtnDisband", EventData.of("Action", "guild_disband"));
        for (int i = 0; i < GuildeTab.MAX_INVITE_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#GInviteBtn" + i, EventData.of("Action", "guild_invite").append("Param", String.valueOf(i)));
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

        // Famille choose buttons
        for (int i = 0; i < FamilleTab.MAX_FAMILY_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#FamBtn" + i,
                    EventData.of("Action", "fam_choose").append("Param", String.valueOf(i)));
        }

        // Admin events
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AdminBtnKingdom", EventData.of("Action", "admin_kingdom"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AdminBtnChurchStatus", EventData.of("Action", "admin_church"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AdminBtnTreasure", EventData.of("Action", "admin_treasure"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AdminBtnRelic", EventData.of("Action", "admin_relic"));
        for (int i = 0; i < AdminTab.MAX_PLAYER_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#AdminReset" + i, EventData.of("Action", "admin_reset").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#AdminXP" + i, EventData.of("Action", "admin_xp").append("Param", String.valueOf(i)));
            // Nobility
            events.addEventBinding(CustomUIEventBindingType.Activating, "#NobChev" + i, EventData.of("Action", "admin_nob").append("Param", i + ":CHEVALIER"));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#NobBaron" + i, EventData.of("Action", "admin_nob").append("Param", i + ":BARON"));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#NobComte" + i, EventData.of("Action", "admin_nob").append("Param", i + ":COMTE"));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#NobDuc" + i, EventData.of("Action", "admin_nob").append("Param", i + ":DUC"));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#NobMarq" + i, EventData.of("Action", "admin_nob").append("Param", i + ":MARQUIS"));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#NobKing" + i, EventData.of("Action", "admin_nob").append("Param", i + ":ROI"));
            // Church
            events.addEventBinding(CustomUIEventBindingType.Activating, "#ChPret" + i, EventData.of("Action", "admin_ch").append("Param", i + ":PRETRE"));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#ChArch" + i, EventData.of("Action", "admin_ch").append("Param", i + ":ARCHEVEQUE"));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#ChCard" + i, EventData.of("Action", "admin_ch").append("Param", i + ":CARDINAL"));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#ChPope" + i, EventData.of("Action", "admin_ch").append("Param", i + ":PAPE"));
        }

        // === EVENT BINDINGS ===

        // Tab navigation
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnProfil",      EventData.of("Action", "tab_profil"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnInventaire",  EventData.of("Action", "tab_inventaire"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnCompetences", EventData.of("Action", "tab_competences"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnGuilde",      EventData.of("Action", "tab_guilde"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnGroupe",      EventData.of("Action", "tab_groupe"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnFamille",     EventData.of("Action", "tab_famille"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnTitres",      EventData.of("Action", "tab_titres"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnAdmin",      EventData.of("Action", "tab_admin"));

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
            if (InventaireTab.handleTake(ref, store, Integer.parseInt(eventData.param))) {
                refreshInventaireTab(ref, store);
            }
            return;
        }
        if ("inv_deposit".equals(eventData.action) && eventData.param != null) {
            if (InventaireTab.handleDeposit(ref, store, Integer.parseInt(eventData.param))) {
                refreshInventaireTab(ref, store);
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

        // Admin actions
        if (eventData.action.startsWith("admin_")) {
            handleAdminAction(eventData.action, eventData.param, ref, store);
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
                player.sendMessage(Message.raw("§eTapez dans le chat : §f/es guildcreate <nom> <tag>"));
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

    private void handleAdminAction(String action, String param, Ref<EntityStore> ref, Store<EntityStore> store) {
        com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());

        switch (action) {
            case "admin_kingdom" -> {
                if (player != null) player.sendMessage(Message.raw("§eTapez : /es kingdom _"));
            }
            case "admin_church" -> {
                if (player != null) player.sendMessage(Message.raw("§eTapez : /es church status _"));
            }
            case "admin_treasure" -> {
                if (player != null) player.sendMessage(Message.raw("§eTapez : /es treasureconfig"));
            }
            case "admin_relic" -> {
                if (player != null) player.sendMessage(Message.raw("§eTapez : /es getrelic"));
            }
            case "admin_reset" -> {
                if (param != null && AdminTab.handleResetLevel(param, ref, store)) {
                    if (player != null) player.sendMessage(Message.raw("§aJoueur reset !"));
                    refreshAdminTab(ref, store);
                }
            }
            case "admin_xp" -> {
                if (param != null && AdminTab.handleAddXP(param, ref, store)) {
                    if (player != null) player.sendMessage(Message.raw("§a+10,000 XP ajoute !"));
                }
            }
            case "admin_nob" -> {
                if (param != null && param.contains(":")) {
                    String[] parts = param.split(":");
                    if (AdminTab.handleNobilityPromote(parts[0], parts[1], ref, store)) {
                        if (player != null) player.sendMessage(Message.raw("§aPromu en " + parts[1]));
                    }
                }
            }
            case "admin_ch" -> {
                if (param != null && param.contains(":")) {
                    String[] parts = param.split(":");
                    if (AdminTab.handleChurchPromote(parts[0], parts[1], ref, store)) {
                        if (player != null) player.sendMessage(Message.raw("§aPromu en " + parts[1]));
                    }
                }
            }
        }
    }

    private void refreshFamilleTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        FamilleTab.populate(update, ref, store);
        this.sendUpdate(update);
    }

    private void refreshAdminTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        AdminTab.populate(update, ref, store);
        this.sendUpdate(update);
    }

    private void refreshCompetencesTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        CompetencesTab.populate(update, ref, store);
        this.sendUpdate(update);
    }

    private void refreshGuildeTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        GuildeTab.populate(update, ref, store);
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
        this.sendUpdate(update);
    }

    private void refreshInventaireTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        InventaireTab.refreshOnly(update, ref, store);
        this.sendUpdate(update);
    }

    private void switchTab(String tabName, Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();

        // Hide all tabs
        for (String tab : TAB_IDS) {
            update.set("#Tab" + tab + ".Visible", false);
        }

        // Show target tab
        String targetId = tabName.substring(0, 1).toUpperCase() + tabName.substring(1);
        update.set("#Tab" + targetId + ".Visible", true);

        // Refresh tab data
        if ("inventaire".equals(tabName)) {
            InventaireTab.refreshOnly(update, ref, store);
        }
        if ("admin".equals(tabName)) {
            AdminTab.populate(update, ref, store);
        }
        if ("competences".equals(tabName)) {
            CompetencesTab.populate(update, ref, store);
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

    public static class SystemEventData {
        public static final BuilderCodec<SystemEventData> CODEC = BuilderCodec.builder(SystemEventData.class, SystemEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("Param", Codec.STRING), (d, v) -> d.param = v, d -> d.param).add()
                .build();
        public String action;
        public String param;
    }
}