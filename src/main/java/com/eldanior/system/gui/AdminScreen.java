package com.eldanior.system.gui;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.gui.tabs.AdminTab;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Interface d'administration dediee — accessible via /es admin (OP uniquement).
 * Separe de SystemScreen pour une meilleure securite et maintenabilite.
 */
public class AdminScreen extends InteractiveCustomUIPage<AdminScreen.AdminEventData> {

    private static final String[] ADMIN_TAB_IDS = {
        "Dashboard", "Players", "World", "Economy", "Holograms", "Data"
    };
    private static final int HOLO_SLOTS = 10;

    private static final int PLAYERS_PER_PAGE = 20;
    private static int playerPage = 0;
    private static final java.util.List<String> allPlayerNames = new java.util.ArrayList<>();
    private static String managedPlayerName = null;
    private static final int WORLD_SLOTS = 5;
    private static int worldTerrPage = 0;
    private static int worldParcelPage = 0;
    private static final java.util.List<com.eldanior.system.territory.ParcelData> worldTerrList = new java.util.ArrayList<>();
    private static final java.util.List<com.eldanior.system.territory.ParcelData> worldParcelList = new java.util.ArrayList<>();
    private static String worldSelectedId = null;
    private static boolean worldSelectedIsTerr = false;

    public AdminScreen(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, AdminEventData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {

        ui.append("Admin/AdminPage.ui");

        // Double-check permission
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || !player.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION)) return;

        // Populate dashboard
        AdminTab.populate(ui, ref, store);
        populateDashboard(ui, ref, store);

        // === TAB NAVIGATION ===
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnDashboard", EventData.of("Action", "tab_dashboard"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnPlayers", EventData.of("Action", "tab_players"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnWorld", EventData.of("Action", "tab_world"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnEconomy", EventData.of("Action", "tab_economy"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnData", EventData.of("Action", "tab_data"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnHolograms", EventData.of("Action", "tab_holograms"));

        // === HOLOGRAMMES — Delete buttons ===
        for (int i = 0; i < HOLO_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#HoloDel" + i, EventData.of("Action", "holo_delete").append("Param", String.valueOf(i)));
        }

        // === DASHBOARD — Player selector ===
        for (int i = 0; i < AdminTab.MAX_PLAYER_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#ASelPlayer" + i, EventData.of("Action", "admin_sel").append("Param", String.valueOf(i)));
        }

        // === DASHBOARD — Action buttons ===
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AResetLv", EventData.of("Action", "admin_reset"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AXP1k", EventData.of("Action", "admin_xp").append("Param", "1000"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AXP10k", EventData.of("Action", "admin_xp").append("Param", "10000"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AXP100k", EventData.of("Action", "admin_xp").append("Param", "100000"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ALv50", EventData.of("Action", "admin_setlv").append("Param", "50"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ALv100", EventData.of("Action", "admin_setlv").append("Param", "100"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ALv200", EventData.of("Action", "admin_setlv").append("Param", "200"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AGold10k", EventData.of("Action", "admin_gold").append("Param", "10000"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AGold100k", EventData.of("Action", "admin_gold").append("Param", "100000"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AGold1m", EventData.of("Action", "admin_gold").append("Param", "1000000"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ACWar", EventData.of("Action", "admin_class").append("Param", "warrior"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ACMag", EventData.of("Action", "admin_class").append("Param", "mage"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ACArc", EventData.of("Action", "admin_class").append("Param", "archer"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ACAss", EventData.of("Action", "admin_class").append("Param", "assassin"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ACDra", EventData.of("Action", "admin_class").append("Param", "dragon"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ANChev", EventData.of("Action", "admin_nob").append("Param", "CHEVALIER"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ANBaron", EventData.of("Action", "admin_nob").append("Param", "BARON"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ANComte", EventData.of("Action", "admin_nob").append("Param", "COMTE"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ANDuc", EventData.of("Action", "admin_nob").append("Param", "DUC"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ANMarq", EventData.of("Action", "admin_nob").append("Param", "MARQUIS"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ANRoi", EventData.of("Action", "admin_nob").append("Param", "ROI"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ACPret", EventData.of("Action", "admin_ch").append("Param", "PRETRE"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ACArch", EventData.of("Action", "admin_ch").append("Param", "ARCHEVEQUE"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ACCard", EventData.of("Action", "admin_ch").append("Param", "CARDINAL"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ACPope", EventData.of("Action", "admin_ch").append("Param", "PAPE"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APK", EventData.of("Action", "admin_pk"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ATitleRst", EventData.of("Action", "admin_titlerst"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ARelic", EventData.of("Action", "admin_relic"));

        // Prefill buttons
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APrefillTitleGrant", EventData.of("Action", "admin_prefill").append("Param", "titleadmin grant <player> <titleId>"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APrefillTitleRemove", EventData.of("Action", "admin_prefill").append("Param", "titleadmin remove <player> <titleId>"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APrefillFamilySet", EventData.of("Action", "admin_prefill").append("Param", "familyset <player> <familyId>"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APrefillGuildCreate", EventData.of("Action", "admin_prefill").append("Param", "guildcreate <nom> <tag>"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APrefillSetLevel", EventData.of("Action", "admin_prefill").append("Param", "setlevel <player> <level>"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APrefillAddXP", EventData.of("Action", "admin_prefill").append("Param", "addxp <player> <amount>"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APrefillGuildDisband", EventData.of("Action", "admin_prefill").append("Param", "guilddisband"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APrefillKingdom", EventData.of("Action", "admin_prefill").append("Param", "kingdom _"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APrefillChurchStatus", EventData.of("Action", "admin_prefill").append("Param", "church status _"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APrefillBankGive", EventData.of("Action", "admin_prefill").append("Param", "bankGive <amount>"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APrefillSetClass", EventData.of("Action", "admin_prefill").append("Param", "setclass <player> <warrior|mage|archer|assassin|dragon>"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APrefillRankDemote", EventData.of("Action", "admin_prefill").append("Param", "rank demote <player>"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APrefillChurchDemote", EventData.of("Action", "admin_prefill").append("Param", "church demote <player>"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APrefillSetVice", EventData.of("Action", "admin_prefill").append("Param", "nstatus setvice <player>"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#APrefillClassInfo", EventData.of("Action", "admin_prefill").append("Param", "classinfo <player>"));

        // === PLAYERS — Pagination ===
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PBtnPrev", EventData.of("Action", "player_prev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PBtnNext", EventData.of("Action", "player_next"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PDetailClose", EventData.of("Action", "player_close"));

        // === PLAYERS — 20 slots (quick buttons + manage) ===
        for (int i = 0; i < 20; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#PManage" + i, EventData.of("Action", "player_manage").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#PPK" + i, EventData.of("Action", "player_qpk").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#PRst" + i, EventData.of("Action", "player_qrst").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#PXP" + i, EventData.of("Action", "player_qxp").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#PGold" + i, EventData.of("Action", "player_qgold").append("Param", String.valueOf(i)));
        }

        // === WORLD — Territories section ===
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWTBtnPrev", EventData.of("Action", "world_tprev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWTBtnNext", EventData.of("Action", "world_tnext"));
        for (int i = 0; i < WORLD_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#AWTSlot" + i, EventData.of("Action", "world_tselect").append("Param", String.valueOf(i)));
        }
        // === WORLD — Parcels section ===
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWPBtnPrev", EventData.of("Action", "world_pprev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWPBtnNext", EventData.of("Action", "world_pnext"));
        for (int i = 0; i < WORLD_SLOTS; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#AWPSlot" + i, EventData.of("Action", "world_pselect").append("Param", String.valueOf(i)));
        }
        // === WORLD DETAIL PANEL ===
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWDetClose", EventData.of("Action", "world_dclose"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWDetBtnPvp", EventData.of("Action", "world_dpvp"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWDetBtnProtect", EventData.of("Action", "world_dprotect"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWDetBtnSale", EventData.of("Action", "world_dsale"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWDetBtnClearOwner", EventData.of("Action", "world_dclearowner"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWDetBtnGuild", EventData.of("Action", "world_dguild"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWDetBtnFamily", EventData.of("Action", "world_dfamily"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWDetBtnPrice", EventData.of("Action", "world_dprice"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWDetBtnRentPrice", EventData.of("Action", "world_drentprice"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWDetBtnTreasury", EventData.of("Action", "world_dtreasury"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWDetBtnDelete", EventData.of("Action", "world_ddelete"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWDetBtnTax", EventData.of("Action", "world_dtax"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AWDetBtnTransfer", EventData.of("Action", "world_dtransfer"));

        // === RESETS ===
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AResetAll", EventData.of("Action", "admin_resetall"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AResetGuilds", EventData.of("Action", "admin_reset_guilds"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AResetFamilies", EventData.of("Action", "admin_reset_families"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AResetParcels", EventData.of("Action", "admin_reset_parcels"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AResetShop", EventData.of("Action", "admin_reset_shop"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#AResetClassements", EventData.of("Action", "admin_reset_classements"));
    }

    // ==================== EVENT HANDLING ====================

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull AdminEventData eventData) {
        if (eventData.action == null) return;
        String action = eventData.action;
        String param = eventData.param;

        // Tab switching
        if (action.startsWith("tab_")) {
            switchTab(action.substring(4), ref, store);
            return;
        }

        // World management actions
        if (action.startsWith("world_")) {
            handleWorldAction(action, param, ref, store);
            return;
        }

        // Player management actions
        if (action.startsWith("player_")) {
            handlePlayerAction(action, param, ref, store);
            return;
        }

        // Hologram actions
        if (action.startsWith("holo_")) {
            handleHologramAction(action, param, ref, store);
            return;
        }

        // All admin actions
        if (action.startsWith("admin_")) {
            handleAdminAction(action, param, eventData, ref, store);
        }
    }

    // ==================== ADMIN ACTIONS ====================

    private void handleAdminAction(String action, String param, AdminEventData eventData,
                                   Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());

        switch (action) {
            case "admin_sel" -> {
                if (param != null) {
                    AdminTab.selectPlayer(EldaniorLogger.parseSafeInt(param, -1));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_reset" -> {
                if (AdminTab.handleResetLevel(ref, store)) {
                    if (player != null) player.getPlayerRef().sendMessage(Message.raw("§aJoueur reset !"));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_xp" -> {
                if (param != null && AdminTab.handleAddXP(EldaniorLogger.parseSafeInt(param, 0), ref, store)) {
                    if (player != null) player.getPlayerRef().sendMessage(Message.raw("§a+" + param + " XP !"));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_setlv" -> {
                if (param != null && AdminTab.handleSetLevel(EldaniorLogger.parseSafeInt(param, 1), ref, store)) {
                    if (player != null) player.getPlayerRef().sendMessage(Message.raw("§aNiveau defini a " + param));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_gold" -> {
                if (param != null && AdminTab.handleGiveGold(EldaniorLogger.parseSafeLong(param, 0L), ref, store)) {
                    if (player != null) player.getPlayerRef().sendMessage(Message.raw("§a+" + param + " Or !"));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_class" -> {
                if (param != null && AdminTab.handleSetClass(param, ref, store)) {
                    if (player != null) player.getPlayerRef().sendMessage(Message.raw("§aClasse: " + param));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_nob" -> {
                if (param != null && AdminTab.handleNobilityPromote(param, ref, store)) {
                    if (player != null) player.getPlayerRef().sendMessage(Message.raw("§aRang: " + param));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_ch" -> {
                if (param != null && AdminTab.handleChurchPromote(param, ref, store)) {
                    if (player != null) player.getPlayerRef().sendMessage(Message.raw("§aEglise: " + param));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_pk" -> {
                if (AdminTab.handleSetPK(ref, store)) {
                    if (player != null) player.getPlayerRef().sendMessage(Message.raw("§ePK toggle !"));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_titlerst" -> {
                if (AdminTab.handleResetTitles(ref, store)) {
                    if (player != null) player.getPlayerRef().sendMessage(Message.raw("§aTitres reset !"));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_relic" -> {
                if (player != null) player.getPlayerRef().sendMessage(Message.raw("§eTapez : /es getrelic"));
            }
            case "admin_prefill" -> {
                if (param != null && player != null) {
                    String name = AdminTab.getSelectedPlayerName();
                    String cmd = param.replace("<player>", name != null ? name : "<player>");
                    player.getPlayerRef().sendMessage(Message.raw("§6Tapez dans le chat :"));
                    player.getPlayerRef().sendMessage(Message.raw("§f/es " + cmd));
                }
            }
            case "admin_reset_guilds" -> {
                if (player != null) {
                    com.eldanior.system.guild.GuildManager.init();
                    deleteDataFile("guilds.properties");
                    player.getPlayerRef().sendMessage(Message.raw("§c§lGuildes reinitialises !"));
                }
            }
            case "admin_reset_families" -> {
                if (player != null) {
                    com.eldanior.system.titles.nobility.family.FamilyManager.init();
                    com.eldanior.system.titles.nobility.NobilityManager.init();
                    deleteDataFile("families.properties");
                    deleteDataFile("hierarchies.properties");
                    player.getPlayerRef().sendMessage(Message.raw("§c§lFamilles et Noblesse reinitialises !"));
                }
            }
            case "admin_reset_parcels" -> {
                if (player != null) {
                    try {
                        java.io.File f = com.eldanior.system.EldaniorSystem.get().getDataDirectory()
                                .resolve("parcels.properties").toFile();
                        if (f.exists()) f.delete();
                    } catch (Exception e) { EldaniorLogger.error("AdminScreen", e); }
                    com.eldanior.system.territory.ParcelManager.init(com.eldanior.system.EldaniorSystem.get().getDataDirectory());
                    com.eldanior.system.territory.systems.ParcelMapSystem.refreshMap(player.getWorld());
                    player.getPlayerRef().sendMessage(Message.raw("§c§lParcelles reinitialises !"));
                }
            }
            case "admin_reset_shop" -> {
                if (player != null) {
                    com.eldanior.system.shop.ShopManager.init();
                    deleteDataFile("shop.properties");
                    deleteDataFile("blackmarket.properties");
                    player.getPlayerRef().sendMessage(Message.raw("§c§lShop et Marche Noir reinitialises !"));
                }
            }
            case "admin_reset_classements" -> {
                if (player != null) {
                    com.eldanior.system.classement.ClassementManager.init();
                    deleteDataFile("classements.properties");
                    player.getPlayerRef().sendMessage(Message.raw("§c§lClassements reinitialises !"));
                }
            }
            case "admin_resetall" -> {
                if (player != null) {
                    com.eldanior.system.guild.GuildManager.init();
                    com.eldanior.system.titles.nobility.family.FamilyManager.init();
                    try {
                        java.io.File parcelsFile = com.eldanior.system.EldaniorSystem.get().getDataDirectory()
                                .resolve("parcels.properties").toFile();
                        if (parcelsFile.exists()) parcelsFile.delete();
                    } catch (Exception e) { EldaniorLogger.error("AdminScreen", e); }
                    com.eldanior.system.territory.ParcelManager.init(com.eldanior.system.EldaniorSystem.get().getDataDirectory());
                    try {
                        java.nio.file.Path dataDir = com.eldanior.system.EldaniorSystem.get().getDataDirectory().resolve("eldanior_data");
                        if (java.nio.file.Files.exists(dataDir)) {
                            for (java.io.File f : dataDir.toFile().listFiles()) {
                                f.delete();
                            }
                        }
                    } catch (Exception e) { EldaniorLogger.error("AdminScreen", e); }
                    player.getPlayerRef().sendMessage(Message.raw("§c§l=== RESET COMPLET ==="));
                    player.getPlayerRef().sendMessage(Message.raw("§cGuildes, Familles, Parcelles, Classements, Shop reinitialises !"));
                    player.getPlayerRef().sendMessage(Message.raw("§7Redemarrez le serveur pour finaliser."));
                }
            }
        }
    }

    // ==================== TAB SWITCHING ====================

    private void switchTab(String tabName, Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();

        // Hide all tabs
        for (String tabId : ADMIN_TAB_IDS) {
            update.set("#Tab" + tabId + ".Visible", false);
        }

        // Show target tab
        String targetId = switch (tabName) {
            case "dashboard" -> "Dashboard";
            case "players" -> "Players";
            case "world" -> "World";
            case "economy" -> "Economy";
            case "holograms" -> "Holograms";
            case "data" -> "Data";
            default -> "Dashboard";
        };
        update.set("#Tab" + targetId + ".Visible", true);

        // Refresh data for target tab
        if ("Dashboard".equals(targetId)) {
            AdminTab.populate(update, ref, store);
            populateDashboard(update, ref, store);
        }
        if ("Players".equals(targetId)) {
            populatePlayerList(update, ref, store);
        }
        if ("World".equals(targetId)) {
            worldSelectedId = null;
            update.set("#AWDetail.Visible", false);
            buildWorldLists();
            populateWorldSlots(update);
        }
        if ("Economy".equals(targetId)) {
            populateEconomyTab(update);
        }
        if ("Holograms".equals(targetId)) {
            populateHologramsTab(update);
        }

        this.sendUpdate(update);
    }

    // ==================== REFRESH ====================

    private void refreshDashboard(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        AdminTab.populate(update, ref, store);
        this.sendUpdate(update);
    }

    // ==================== WORLD MANAGEMENT ====================

    private void handleWorldAction(String action, String param, Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        switch (action) {
            case "world_tprev" -> { if (worldTerrPage > 0) { worldTerrPage--; worldSelectedId = null; refreshWorldAll(); } }
            case "world_tnext" -> { int mp = Math.max(0, (worldTerrList.size() - 1) / WORLD_SLOTS); if (worldTerrPage < mp) { worldTerrPage++; worldSelectedId = null; refreshWorldAll(); } }
            case "world_pprev" -> { if (worldParcelPage > 0) { worldParcelPage--; worldSelectedId = null; refreshWorldAll(); } }
            case "world_pnext" -> { int mp = Math.max(0, (worldParcelList.size() - 1) / WORLD_SLOTS); if (worldParcelPage < mp) { worldParcelPage++; worldSelectedId = null; refreshWorldAll(); } }
            case "world_tselect" -> {
                if (param != null) {
                    int idx = EldaniorLogger.parseSafeInt(param, -1) + (worldTerrPage * WORLD_SLOTS);
                    if (idx < worldTerrList.size()) {
                        String id = worldTerrList.get(idx).getId();
                        if (id.equals(worldSelectedId)) { worldSelectedId = null; } else { worldSelectedId = id; worldSelectedIsTerr = true; }
                        refreshWorldAll();
                    }
                }
            }
            case "world_pselect" -> {
                if (param != null) {
                    int idx = EldaniorLogger.parseSafeInt(param, -1) + (worldParcelPage * WORLD_SLOTS);
                    if (idx < worldParcelList.size()) {
                        String id = worldParcelList.get(idx).getId();
                        if (id.equals(worldSelectedId)) { worldSelectedId = null; } else { worldSelectedId = id; worldSelectedIsTerr = false; }
                        refreshWorldAll();
                    }
                }
            }
            case "world_dclose" -> { worldSelectedId = null; refreshWorldAll(); }
            case "world_dpvp" -> {
                com.eldanior.system.territory.ParcelData p = getSelectedParcel();
                if (p != null) { p.setPvpEnabled(!p.isPvpEnabled()); com.eldanior.system.territory.ParcelManager.saveAll();
                    if (player != null) player.getPlayerRef().sendMessage(Message.raw("§ePVP " + (p.isPvpEnabled() ? "active" : "desactive") + " pour " + p.getName()));
                    refreshWorldAll(); }
            }
            case "world_dprotect" -> {
                com.eldanior.system.territory.ParcelData p = getSelectedParcel();
                if (p != null) { p.setProtectedByDefault(!p.isProtectedByDefault()); com.eldanior.system.territory.ParcelManager.saveAll();
                    if (player != null) player.getPlayerRef().sendMessage(Message.raw("§eProtection " + (p.isProtectedByDefault() ? "activee" : "desactivee") + " pour " + p.getName()));
                    refreshWorldAll(); }
            }
            case "world_dsale" -> {
                com.eldanior.system.territory.ParcelData p = getSelectedParcel();
                if (p != null) { p.setForSale(!p.isForSale()); com.eldanior.system.territory.ParcelManager.saveAll();
                    if (player != null) player.getPlayerRef().sendMessage(Message.raw("§eVente " + (p.isForSale() ? "activee" : "desactivee") + " pour " + p.getName()));
                    refreshWorldAll(); }
            }
            case "world_dclearowner" -> {
                com.eldanior.system.territory.ParcelData p = getSelectedParcel();
                if (p != null) { p.clearOwnership(); com.eldanior.system.territory.ParcelManager.saveAll();
                    if (player != null) player.getPlayerRef().sendMessage(Message.raw("§eProprietaire retire pour " + p.getName()));
                    refreshWorldAll(); }
            }
            case "world_dguild" -> {
                com.eldanior.system.territory.ParcelData p = getSelectedParcel();
                if (p != null && player != null) { player.getPlayerRef().sendMessage(Message.raw("§6Tapez : §f/es parcel assignguild " + p.getId() + " <guildId>")); }
            }
            case "world_dfamily" -> {
                com.eldanior.system.territory.ParcelData p = getSelectedParcel();
                if (p != null && player != null) {
                    player.getPlayerRef().sendMessage(Message.raw("§6Familles : §f" + com.eldanior.system.titles.nobility.family.FamilyManager.getAvailableIds()));
                    player.getPlayerRef().sendMessage(Message.raw("§6Tapez : §f/es parcel assignfamily " + p.getId() + " <familyId>"));
                }
            }
            case "world_dprice" -> {
                com.eldanior.system.territory.ParcelData p = getSelectedParcel();
                if (p != null && player != null) { player.getPlayerRef().sendMessage(Message.raw("§6Tapez : §f/es parcel setprice " + p.getId() + " <montant>")); }
            }
            case "world_drentprice" -> {
                com.eldanior.system.territory.ParcelData p = getSelectedParcel();
                if (p != null && player != null) { player.getPlayerRef().sendMessage(Message.raw("§6Tapez : §f/es parcel setrent " + p.getId() + " <montant>")); }
            }
            case "world_dtreasury" -> {
                com.eldanior.system.territory.ParcelData p = getSelectedParcel();
                if (p != null) { p.addTreasury(10000); com.eldanior.system.territory.ParcelManager.saveAll();
                    if (player != null) player.getPlayerRef().sendMessage(Message.raw("§a+10,000 Or au tresor de " + p.getName()));
                    refreshWorldAll(); }
            }
            case "world_ddelete" -> {
                com.eldanior.system.territory.ParcelData p = getSelectedParcel();
                if (p != null) { String name = p.getName(); com.eldanior.system.territory.ParcelManager.deleteParcel(p.getId());
                    worldSelectedId = null; if (player != null) player.getPlayerRef().sendMessage(Message.raw("§c" + name + " supprime !"));
                    buildWorldLists(); refreshWorldAll(); }
            }
            case "world_dtax" -> {
                com.eldanior.system.territory.ParcelData p = getSelectedParcel();
                if (p != null && p.canCollectTax() && !p.getChildIds().isEmpty()) {
                    double taxRate = switch (p.getType()) {
                        case KINGDOM -> 0.57;
                        case TERRITORY -> {
                            com.eldanior.system.territory.ParcelData parent = p.getParentId() != null ? com.eldanior.system.territory.ParcelManager.get(p.getParentId()) : null;
                            yield (parent != null && parent.getType() == com.eldanior.system.territory.ParcelType.KINGDOM) ? 0.80 : 0.875;
                        }
                        default -> 0.0;
                    };
                    if (taxRate > 0) {
                        long totalCollected = 0;
                        for (String childId : p.getChildIds()) {
                            com.eldanior.system.territory.ParcelData child = com.eldanior.system.territory.ParcelManager.get(childId);
                            if (child == null || child.getTreasury() <= 0) continue;
                            long amount = (long)(child.getTreasury() * taxRate);
                            child.addTreasury(-amount);
                            totalCollected += amount;
                        }
                        p.addTreasury(totalCollected);
                        p.setLastTaxCollection(System.currentTimeMillis());
                        p.setLastTaxAmount(totalCollected);
                        com.eldanior.system.territory.ParcelManager.saveAll();
                        if (player != null) player.getPlayerRef().sendMessage(Message.raw("§a" + formatMoney(totalCollected) + " Or d'impots collectes pour " + p.getName()));
                        refreshWorldAll();
                    }
                }
            }
            case "world_dtransfer" -> {
                com.eldanior.system.territory.ParcelData p = getSelectedParcel();
                if (p != null && p.canTransferTreasury() && p.getTreasury() > 0) {
                    long transferAmount = p.getTreasury() / 2;
                    boolean transferred = false;
                    String gId = p.getGuildId(); String fId = p.getFamilyId();
                    if (gId != null && !gId.isEmpty()) {
                        com.eldanior.system.guild.Guild guild = com.eldanior.system.guild.GuildManager.get(gId);
                        if (guild != null) { guild.addTreasury(transferAmount); p.addTreasury(-transferAmount); transferred = true; }
                    }
                    if (!transferred && fId != null && !fId.isEmpty()) {
                        var rd = com.eldanior.system.titles.nobility.family.FamilyManager.getRuntimeData(fId);
                        if (rd != null) { rd.addTreasury(transferAmount); p.addTreasury(-transferAmount); transferred = true; }
                    }
                    if (transferred) {
                        p.setLastTreasuryTransfer(System.currentTimeMillis());
                        com.eldanior.system.territory.ParcelManager.saveAll();
                        if (player != null) player.getPlayerRef().sendMessage(Message.raw("§a" + formatMoney(transferAmount) + " Or transferes depuis " + p.getName()));
                        refreshWorldAll();
                    }
                }
            }
        }
    }

    private com.eldanior.system.territory.ParcelData getSelectedParcel() {
        if (worldSelectedId == null) return null;
        return com.eldanior.system.territory.ParcelManager.get(worldSelectedId);
    }

    private void buildWorldLists() {
        worldTerrList.clear();
        worldParcelList.clear();
        for (com.eldanior.system.territory.ParcelData p : com.eldanior.system.territory.ParcelManager.getAll()) {
            switch (p.getType()) {
                case KINGDOM, GRAND_TERRITORY, TERRITORY, CITY -> worldTerrList.add(p);
                default -> worldParcelList.add(p);
            }
        }
        worldTerrList.sort(java.util.Comparator.comparingInt(a -> a.getType().ordinal()));
        worldParcelList.sort(java.util.Comparator.comparingInt(a -> a.getType().ordinal()));
    }

    private void refreshWorldAll() {
        UICommandBuilder update = new UICommandBuilder();
        buildWorldLists();
        populateWorldSlots(update);
        populateWorldDetail(update);
        this.sendUpdate(update);
    }

    private void populateWorldSlots(UICommandBuilder update) {
        // Counters
        int k = 0, t = 0, c = 0, pl = 0, h = 0, f = 0, pvpCount = 0;
        for (com.eldanior.system.territory.ParcelData p : com.eldanior.system.territory.ParcelManager.getAll()) {
            switch (p.getType()) { case KINGDOM -> k++; case GRAND_TERRITORY, TERRITORY -> t++; case CITY -> { c++; if (p.isPvpEnabled()) pvpCount++; } case PLOT -> pl++; case HOUSING -> h++; case FARM -> f++; default -> {} }
        }
        update.set("#WStatKingdoms.Text", String.valueOf(k));
        update.set("#WStatTerritories.Text", String.valueOf(t));
        update.set("#WStatCities.Text", String.valueOf(c));
        update.set("#WStatPlots.Text", String.valueOf(pl));
        update.set("#WStatHousing.Text", String.valueOf(h));
        update.set("#WStatFarms.Text", String.valueOf(f));
        update.set("#WStatPvp.Text", String.valueOf(pvpCount));

        // Territories section
        int ttPages = Math.max(1, (worldTerrList.size() + WORLD_SLOTS - 1) / WORLD_SLOTS);
        if (worldTerrPage >= ttPages) worldTerrPage = ttPages - 1;
        update.set("#AWTPageInfo.Text", (worldTerrPage + 1) + "/" + ttPages);
        fillSlots(update, "AWT", worldTerrList, worldTerrPage);

        // Parcels section
        int tpPages = Math.max(1, (worldParcelList.size() + WORLD_SLOTS - 1) / WORLD_SLOTS);
        if (worldParcelPage >= tpPages) worldParcelPage = tpPages - 1;
        update.set("#AWPPageInfo.Text", (worldParcelPage + 1) + "/" + tpPages);
        fillSlots(update, "AWP", worldParcelList, worldParcelPage);
    }

    private void fillSlots(UICommandBuilder update, String prefix, java.util.List<com.eldanior.system.territory.ParcelData> list, int page) {
        int start = page * WORLD_SLOTS;
        for (int i = 0; i < WORLD_SLOTS; i++) {
            int idx = start + i;
            if (idx < list.size()) {
                com.eldanior.system.territory.ParcelData p = list.get(idx);
                update.set("#" + prefix + "Slot" + i + ".Visible", true);
                update.set("#" + prefix + "Type" + i + ".Text", "[" + p.getType().getLabel() + "]");
                String typeColor = switch (p.getType()) {
                    case KINGDOM -> "#FFD700"; case GRAND_TERRITORY -> "#1E90FF"; case TERRITORY -> "#3498DB"; case CITY -> "#2ECC71";
                    case PLOT -> "#aabbcc"; case HOUSING -> "#9B59B6"; case FARM -> "#8B4513";
                    default -> "#8899aa";
                };
                update.set("#" + prefix + "Type" + i + ".Style.TextColor", typeColor);
                update.set("#" + prefix + "Name" + i + ".Text", p.getName());
                String owner = p.getOwnerName() != null && !p.getOwnerName().isEmpty() ? p.getOwnerName() : "---";
                update.set("#" + prefix + "Owner" + i + ".Text", owner);

                // Status with color
                String status;
                String statusColor;
                boolean hasOwner = p.getOwnerName() != null && !p.getOwnerName().isEmpty();
                boolean hasFamily = p.getFamilyId() != null && !p.getFamilyId().isEmpty();
                boolean hasGuild = p.getGuildId() != null && !p.getGuildId().isEmpty();
                boolean isTerritoryType = (p.getType() == com.eldanior.system.territory.ParcelType.KINGDOM
                    || p.getType() == com.eldanior.system.territory.ParcelType.TERRITORY
                    || p.getType() == com.eldanior.system.territory.ParcelType.CITY);

                if (isTerritoryType) {
                    boolean needsGuild = p.getType() == com.eldanior.system.territory.ParcelType.CITY;
                    boolean isConfigured = needsGuild ? hasGuild : hasFamily;
                    if (isConfigured) {
                        status = "Configure";
                        statusColor = "#4CAF50";
                    } else {
                        status = needsGuild ? "A configurer (Guilde)" : "A configurer (Famille)";
                        statusColor = "#cc4444";
                    }
                } else {
                    if (p.isRented()) {
                        status = p.isRentExpired() ? "Location EXPIREE" : "En location";
                        statusColor = p.isRentExpired() ? "#cc4444" : "#ff9800";
                    } else if (p.isBought()) {
                        status = "Achete";
                        statusColor = "#4CAF50";
                    } else if (p.isForSale() && p.isForRent()) {
                        status = "Vente + Location";
                        statusColor = "#2196F3";
                    } else if (p.isForSale()) {
                        status = "En vente";
                        statusColor = "#2196F3";
                    } else if (p.isForRent()) {
                        status = "A louer";
                        statusColor = "#ff9800";
                    } else if (p.getPrice() <= 0 && p.getRentPrice() <= 0 && !hasOwner) {
                        status = "A configurer";
                        statusColor = "#cc4444";
                    } else {
                        status = "Libre";
                        statusColor = "#8BC34A";
                    }
                }
                if (p.isPvpEnabled()) status += " | PVP";

                if (prefix.equals("AWT")) {
                    String locText = formatMoney(p.getTreasury()) + " Or";
                    if (p.getType() == com.eldanior.system.territory.ParcelType.CITY) {
                        com.eldanior.system.territory.ParcelData pt = com.eldanior.system.territory.ParcelEconomyManager.findParentOfType(p, com.eldanior.system.territory.ParcelType.TERRITORY);
                        if (pt != null) locText = "Dans: " + pt.getName() + " | " + locText;
                    } else if (p.getType() == com.eldanior.system.territory.ParcelType.TERRITORY) {
                        com.eldanior.system.territory.ParcelData pk = com.eldanior.system.territory.ParcelEconomyManager.findParentOfType(p, com.eldanior.system.territory.ParcelType.KINGDOM);
                        if (pk != null) locText = "Dans: " + pk.getName() + " | " + locText;
                    }
                    if (p.isPvpEnabled()) locText += " | PVP";
                    update.set("#" + prefix + "Info" + i + ".Text", locText);
                    update.set("#" + prefix + "Info" + i + ".Style.TextColor", "#8899aa");
                } else {
                    update.set("#" + prefix + "Info" + i + ".Text", status);
                    update.set("#" + prefix + "Info" + i + ".Style.TextColor", statusColor);
                }

                // Highlight selected
                boolean sel = p.getId().equals(worldSelectedId);
                update.set("#" + prefix + "Slot" + i + ".Style.Default.Background", sel ? "#1a2a3a" : (i % 2 == 0 ? (prefix.equals("AWT") ? "#1a1a0d" : "#0d1a0d") : (prefix.equals("AWT") ? "#15150a" : "#0a150a")));
            } else {
                update.set("#" + prefix + "Slot" + i + ".Visible", false);
            }
        }
    }

    private void populateWorldDetail(UICommandBuilder update) {
        com.eldanior.system.territory.ParcelData p = getSelectedParcel();
        if (p == null) { update.set("#AWDetail.Visible", false); return; }
        update.set("#AWDetail.Visible", true);
        boolean isTerr = (p.getType() == com.eldanior.system.territory.ParcelType.KINGDOM
            || p.getType() == com.eldanior.system.territory.ParcelType.TERRITORY
            || p.getType() == com.eldanior.system.territory.ParcelType.CITY);

        // Header
        update.set("#AWDetName.Text", p.getName());
        update.set("#AWDetType.Text", p.getType().name());
        String typeColor = switch (p.getType()) {
            case KINGDOM -> "#FFD700"; case GRAND_TERRITORY -> "#1E90FF"; case TERRITORY -> "#3498DB"; case CITY -> "#2ECC71";
            case PLOT -> "#aabbcc"; case HOUSING -> "#9B59B6"; default -> "#8899aa";
        };
        update.set("#AWDetName.Style.TextColor", typeColor);

        // === COLONNE GAUCHE ===
        String owner = p.getOwnerName() != null && !p.getOwnerName().isEmpty() ? p.getOwnerName() : "Aucun";
        update.set("#AWDetOwner.Text", "Proprietaire : " + owner + "   —   ID : " + p.getId());

        int sX = Math.abs(p.getMaxX() - p.getMinX()) + 1;
        int sY = Math.abs(p.getMaxY() - p.getMinY()) + 1;
        int sZ = Math.abs(p.getMaxZ() - p.getMinZ()) + 1;
        update.set("#AWDetCoords.Text", "Taille : " + sX + " x " + sZ + " (" + (sX * sZ) + " blocs)  |  Hauteur : " + sY);

        String parentText = "Parent : ";
        if (p.getParentId() != null && !p.getParentId().isEmpty()) {
            com.eldanior.system.territory.ParcelData parentParcel = com.eldanior.system.territory.ParcelManager.get(p.getParentId());
            parentText += parentParcel != null ? parentParcel.getName() + " (" + parentParcel.getType().name() + ")" : p.getParentId();
        } else {
            parentText += "aucun";
        }
        java.util.List<String> children = com.eldanior.system.territory.ParcelManager.getChildrenOf(p.getId());
        parentText += "   |   " + children.size() + " enfant(s)";
        update.set("#AWDetParent.Text", parentText);

        String gId = p.getGuildId(); String fId = p.getFamilyId();
        String assocText = "";
        if (fId != null && !fId.isEmpty()) {
            var family = com.eldanior.system.titles.nobility.family.FamilyManager.get(fId);
            assocText += "Famille : " + (family != null ? family.getDisplayName() : fId);
        } else {
            assocText += "Famille : ---";
        }
        assocText += "   |   Guilde : " + (gId != null && !gId.isEmpty() ? gId : "---");
        assocText += "   |   " + p.getMembers().size() + " membre(s)";
        update.set("#AWDetMembers.Text", assocText);

        // === COLONNE DROITE ===
        String statusText;
        if (isTerr) {
            boolean hasFamily = fId != null && !fId.isEmpty();
            boolean hasGuild = gId != null && !gId.isEmpty();
            if (p.getType() == com.eldanior.system.territory.ParcelType.CITY) {
                statusText = hasGuild ? "Configure (Guilde)" : "A CONFIGURER (Guilde)";
            } else {
                statusText = hasFamily ? "Configure (Famille)" : "A CONFIGURER (Famille)";
            }
        } else {
            if (p.isRented()) statusText = "EN LOCATION";
            else if (p.isBought()) statusText = "ACHETE";
            else if (p.isForSale() || p.isForRent()) statusText = "DISPONIBLE";
            else if (p.isFree()) statusText = "LIBRE";
            else statusText = "---";
        }
        String pvp = p.isPvpEnabled() ? "PVP ON" : "PVP OFF";
        String prot = p.isProtectedByDefault() ? "Protege" : "Ouvert";
        update.set("#AWDetStatus.Text", statusText + "   |   " + pvp + "   |   " + prot);

        if (isTerr) {
            String taxInfo = "Taxe : " + (int)(p.getTaxRate() * 100) + "%";
            if (p.canCollectTax()) taxInfo += "  (collecte dispo)";
            update.set("#AWDetTreasury.Text", "Tresorerie : " + formatMoney(p.getTreasury()) + " Or   |   " + taxInfo);
        } else {
            update.set("#AWDetTreasury.Text", "Tresorerie : " + formatMoney(p.getTreasury()) + " Or");
        }

        if (isTerr) {
            String taxText = "";
            if (p.getLastTaxCollection() > 0) {
                long nextTax = (p.getLastTaxCollection() + 7L * 24 * 60 * 60 * 1000) - System.currentTimeMillis();
                if (nextTax > 0) {
                    long days = nextTax / (24 * 60 * 60 * 1000);
                    long hours = (nextTax % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
                    taxText = "Prochain impot : " + days + "j " + hours + "h";
                } else {
                    taxText = "Impots reclamables !";
                }
            } else {
                taxText = "Aucun impot collecte";
            }
            if (p.getLastTaxAmount() > 0) taxText += "   |   Dernier : " + formatMoney(p.getLastTaxAmount()) + " Or";
            update.set("#AWDetTaxInfo.Text", taxText);
            update.set("#AWDetTaxInfo.Visible", true);
        } else {
            update.set("#AWDetTaxInfo.Visible", false);
        }

        String priceText = "Prix vente : " + (p.getPrice() > 0 ? formatMoney(p.getPrice()) + " Or" : "Non defini");
        priceText += "   |   Loyer : " + (p.getRentPrice() > 0 ? formatMoney(p.getRentPrice()) + " Or/7j" : "Non defini");
        update.set("#AWDetPrices.Text", priceText);

        String flags = p.isForSale() ? "En vente" : "Pas en vente";
        flags += "   |   " + (p.isForRent() ? "A louer" : "Pas a louer");
        if (p.isRented()) flags += "   |   LOUEE";
        if (p.isRentExpired()) flags += " (EXPIREE)";
        else if (p.isInGracePeriod()) flags += " (GRACE 24h)";
        update.set("#AWDetFlags.Text", flags);

        if (p.getRenterUUID() != null) {
            update.set("#AWDetRenter.Visible", true);
            String renterName = "Inconnu";
            try {
                com.hypixel.hytale.server.core.universe.PlayerRef rRef = com.hypixel.hytale.server.core.universe.Universe.get()
                    .getPlayer(p.getRenterUUID());
                if (rRef != null) renterName = rRef.getUsername();
            } catch (Exception e) { /* skip */ }
            String renterText = "Locataire : " + renterName;
            if (p.getRentEndTime() > 0) {
                long remaining = p.getRentEndTime() - System.currentTimeMillis();
                if (remaining > 0) {
                    long days = remaining / (24 * 60 * 60 * 1000);
                    long hours = (remaining % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
                    renterText += "   —   Expire dans " + days + "j " + hours + "h";
                } else {
                    renterText += "   —   EXPIRE";
                }
            }
            update.set("#AWDetRenter.Text", renterText);
        } else {
            update.set("#AWDetRenter.Visible", false);
        }

        if (isTerr) {
            java.util.Set<java.util.UUID> inhabitants = new java.util.HashSet<>();
            countInhabitants(p.getId(), inhabitants, 0);
            update.set("#AWDetPopulation.Text", "Population : " + inhabitants.size() + " habitant(s)");
            update.set("#AWDetPopulation.Visible", true);
        } else {
            update.set("#AWDetPopulation.Visible", false);
        }

        java.util.List<String> childIds = p.getChildIds();
        for (int i = 0; i < 6; i++) {
            if (i < childIds.size()) {
                com.eldanior.system.territory.ParcelData child = com.eldanior.system.territory.ParcelManager.get(childIds.get(i));
                if (child != null) {
                    update.set("#AWDetChild" + i + ".Visible", true);
                    String childOwner = child.getOwnerName() != null && !child.getOwnerName().isEmpty() ? child.getOwnerName() : "-";
                    String childText = "[" + child.getType().getLabel() + "] " + child.getName() + "   —   " + childOwner;
                    if (child.getTreasury() > 0) childText += "   —   " + formatMoney(child.getTreasury()) + " Or";
                    update.set("#AWDetChild" + i + ".Text", childText);
                } else {
                    update.set("#AWDetChild" + i + ".Visible", false);
                }
            } else {
                update.set("#AWDetChild" + i + ".Visible", false);
            }
        }
    }

    private static void countInhabitants(String parentId, java.util.Set<java.util.UUID> inhabitants, int depth) {
        if (depth > 5) return;
        for (String childId : com.eldanior.system.territory.ParcelManager.getChildrenOf(parentId)) {
            com.eldanior.system.territory.ParcelData child = com.eldanior.system.territory.ParcelManager.get(childId);
            if (child == null) continue;
            if (child.getOwnerUUID() != null) inhabitants.add(child.getOwnerUUID());
            if (child.getRenterUUID() != null) inhabitants.add(child.getRenterUUID());
            countInhabitants(childId, inhabitants, depth + 1);
        }
    }

    // ==================== PLAYER MANAGEMENT ====================

    private void handlePlayerAction(String action, String param, Ref<EntityStore> ref, Store<EntityStore> store) {
        switch (action) {
            case "player_prev" -> {
                if (playerPage > 0) { playerPage--; refreshPlayerList(ref, store); }
            }
            case "player_next" -> {
                int maxPage = Math.max(0, (allPlayerNames.size() - 1) / PLAYERS_PER_PAGE);
                if (playerPage < maxPage) { playerPage++; refreshPlayerList(ref, store); }
            }
            case "player_close" -> {
                managedPlayerName = null;
                UICommandBuilder update = new UICommandBuilder();
                update.set("#PDetailPanel.Visible", false);
                this.sendUpdate(update);
            }
            case "player_manage" -> {
                if (param != null) {
                    int idx = EldaniorLogger.parseSafeInt(param, -1) + (playerPage * PLAYERS_PER_PAGE);
                    if (idx >= 0 && idx < allPlayerNames.size()) {
                        managedPlayerName = allPlayerNames.get(idx);
                        AdminTab.selectPlayer(idx < 8 ? idx : 0);
                        refreshPlayerDetail(ref, store);
                    }
                }
            }
            case "player_qpk" -> { selectAndDoQuick(param, ref, store); AdminTab.handleSetPK(ref, store); refreshPlayerList(ref, store); }
            case "player_qrst" -> { selectAndDoQuick(param, ref, store); AdminTab.handleResetLevel(ref, store); refreshPlayerList(ref, store); }
            case "player_qxp" -> { selectAndDoQuick(param, ref, store); AdminTab.handleAddXP(10000, ref, store); refreshPlayerList(ref, store); }
            case "player_qgold" -> { selectAndDoQuick(param, ref, store); AdminTab.handleGiveGold(10000, ref, store); refreshPlayerList(ref, store); }
        }
    }

    private void selectAndDoQuick(String param, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (param == null) return;
        int idx = EldaniorLogger.parseSafeInt(param, -1) + (playerPage * PLAYERS_PER_PAGE);
        if (idx >= 0 && idx < allPlayerNames.size()) {
            AdminTab.selectPlayerByName(allPlayerNames.get(idx));
        }
    }

    private void refreshPlayerList(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        populatePlayerList(update, ref, store);
        this.sendUpdate(update);
    }

    private void refreshPlayerDetail(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        if (managedPlayerName == null) {
            update.set("#PDetailPanel.Visible", false);
        } else {
            update.set("#PDetailPanel.Visible", true);
            update.set("#PDetailName.Text", "Gestion : " + managedPlayerName);

            com.hypixel.hytale.server.core.universe.PlayerRef targetRef = com.hypixel.hytale.server.core.universe.Universe.get()
                .getPlayerByUsername(managedPlayerName, com.hypixel.hytale.server.core.NameMatching.EXACT_IGNORE_CASE);
            if (targetRef != null) {
                try {
                    var tRef = targetRef.getReference();
                    if (tRef != null) {
                        var tStore = tRef.getStore();
                        com.eldanior.system.config.Player.PlayerLevelData data = tStore.getComponent(tRef, com.eldanior.system.EldaniorSystem.get().getPlayerLevelDataType());
                        if (data != null) {
                            update.set("#PDetailLv.Text", "Lv." + data.getLevel());
                            update.set("#PDetailClass.Text", data.getPlayerClass());
                            update.set("#PDetailMoney.Text", formatMoney(data.getMoney()) + " Or");
                            update.set("#PDetailRank.Text", data.getNobilityRank());
                            update.set("#PDetailChurch.Text", data.getChurchRank());
                            update.set("#PDetailPK.Text", data.isPK() ? "PK" : "");
                            String familyId = data.getNobleFamilyId();
                            update.set("#PDetailFamily.Text", "Famille: " + (familyId != null && !familyId.isEmpty() ? familyId : "---"));
                            String guildId = data.getGuildId();
                            update.set("#PDetailGuild.Text", "Guilde: " + (guildId != null && !guildId.isEmpty() ? guildId : "---"));
                            update.set("#PDetailKD.Text", "K/D: " + data.getPlayerKills() + "/" + data.getPlayerDeaths());
                            update.set("#PDetailDuel.Text", "Duels: " + data.getDuelWins() + "/" + data.getDuelLosses());
                        }
                    }
                } catch (Exception e) { EldaniorLogger.error("AdminScreen.detail", e); }
            }
        }
        this.sendUpdate(update);
    }

    private void populatePlayerList(UICommandBuilder update, Ref<EntityStore> ref, Store<EntityStore> store) {
        // Refresh player list
        allPlayerNames.clear();
        for (com.hypixel.hytale.server.core.universe.PlayerRef pRef : com.hypixel.hytale.server.core.universe.Universe.get().getPlayers()) {
            allPlayerNames.add(pRef.getUsername());
        }
        java.util.Collections.sort(allPlayerNames, String.CASE_INSENSITIVE_ORDER);

        int totalPages = Math.max(1, (allPlayerNames.size() + PLAYERS_PER_PAGE - 1) / PLAYERS_PER_PAGE);
        if (playerPage >= totalPages) playerPage = totalPages - 1;
        update.set("#PPageInfo.Text", "Page " + (playerPage + 1) + "/" + totalPages);

        int startIdx = playerPage * PLAYERS_PER_PAGE;
        for (int i = 0; i < PLAYERS_PER_PAGE; i++) {
            int playerIdx = startIdx + i;
            if (playerIdx < allPlayerNames.size()) {
                String name = allPlayerNames.get(playerIdx);
                update.set("#PSlot" + i + ".Visible", true);
                update.set("#PName" + i + ".Text", name);

                // Get quick info
                try {
                    com.hypixel.hytale.server.core.universe.PlayerRef pRef = com.hypixel.hytale.server.core.universe.Universe.get()
                        .getPlayerByUsername(name, com.hypixel.hytale.server.core.NameMatching.EXACT_IGNORE_CASE);
                    if (pRef != null) {
                        var tRef = pRef.getReference();
                        if (tRef != null) {
                            com.eldanior.system.config.Player.PlayerLevelData data = tRef.getStore().getComponent(tRef, com.eldanior.system.EldaniorSystem.get().getPlayerLevelDataType());
                            if (data != null) {
                                update.set("#PInfo" + i + ".Text", "Lv." + data.getLevel() + " | " + data.getPlayerClass() + (data.isPK() ? " | PK" : ""));
                                String statLine = data.getNobilityRank() + " | " + data.getChurchRank()
                                    + " | " + formatMoney(data.getMoney()) + " Or"
                                    + " | K/D: " + data.getPlayerKills() + "/" + data.getPlayerDeaths();
                                String guildId = data.getGuildId();
                                if (guildId != null && !guildId.isEmpty()) statLine += " | Guilde: " + guildId;
                                update.set("#PStat" + i + ".Text", statLine);
                            }
                        }
                    }
                } catch (Exception e) { /* skip */ }
            } else {
                update.set("#PSlot" + i + ".Visible", false);
            }
        }
    }

    // ==================== TAB POPULATORS ====================

    private void populateDashboard(UICommandBuilder update, Ref<EntityStore> ref, Store<EntityStore> store) {
        // Territoire stats
        var parcels = com.eldanior.system.territory.ParcelManager.getAll();
        int kingdoms = 0, territories = 0, cities = 0, plots = 0, housing = 0, farms = 0, pvpCities = 0;
        for (com.eldanior.system.territory.ParcelData p : parcels) {
            switch (p.getType()) {
                case KINGDOM -> kingdoms++;
                case TERRITORY -> territories++;
                case CITY -> { cities++; if (p.isPvpEnabled()) pvpCities++; }
                case PLOT -> plots++;
                case HOUSING -> housing++;
                case FARM -> farms++;
                default -> {}
            }
        }
        update.set("#DStatKingdoms.Text", String.valueOf(kingdoms));
        update.set("#DStatTerritories.Text", String.valueOf(territories));
        update.set("#DStatCities.Text", String.valueOf(cities));
        update.set("#DStatPlots.Text", String.valueOf(plots));
        update.set("#DStatHousing.Text", String.valueOf(housing));
        update.set("#DStatFarms.Text", String.valueOf(farms));
        update.set("#DStatTotalParcels.Text", String.valueOf(parcels.size()));

        // Economie & Social
        update.set("#DStatGuilds.Text", String.valueOf(com.eldanior.system.guild.GuildManager.getAll().size()));
        update.set("#DStatShop.Text", String.valueOf(com.eldanior.system.shop.ShopManager.getListings().size()));
        update.set("#DStatBlackMarket.Text", String.valueOf(com.eldanior.system.shop.ShopManager.getBlackMarketListings().size()));
        update.set("#DStatPvp.Text", String.valueOf(pvpCities));

        int familyCount = 0;
        for (var f : com.eldanior.system.titles.nobility.family.FamilyManager.getAll()) {
            if (com.eldanior.system.titles.nobility.family.FamilyManager.isFamilyTaken(f.getId())) familyCount++;
        }
        update.set("#DStatFamilies.Text", familyCount + "/9");

        // Noblesse & Eglise
        String kingName = com.eldanior.system.titles.nobility.NobilityManager.getCurrentKingName();
        update.set("#DStatKing.Text", (kingName != null && !kingName.isEmpty()) ? kingName : "Aucun");

        String popeName = com.eldanior.system.titles.church.ChurchManager.getCurrentPopeName();
        update.set("#DStatPope.Text", (popeName != null && !popeName.isEmpty()) ? popeName : "Aucun");

        int marquis = 0, ducs = 0, comtes = 0, barons = 0, chevaliers = 0;
        int pretres = 0, archeveques = 0, cardinaux = 0;
        long totalMoney = 0;
        for (com.hypixel.hytale.server.core.universe.PlayerRef pRef : com.hypixel.hytale.server.core.universe.Universe.get().getPlayers()) {
            try {
                var tRef = pRef.getReference();
                if (tRef == null) continue;
                var tStore = tRef.getStore();
                com.eldanior.system.config.Player.PlayerLevelData data = tStore.getComponent(tRef, com.eldanior.system.EldaniorSystem.get().getPlayerLevelDataType());
                if (data == null) continue;

                totalMoney += data.getMoney();

                String rank = data.getNobilityRank();
                if (rank != null) {
                    switch (rank) {
                        case "MARQUIS" -> marquis++;
                        case "DUC" -> ducs++;
                        case "COMTE" -> comtes++;
                        case "BARON" -> barons++;
                        case "CHEVALIER" -> chevaliers++;
                    }
                }

                String church = data.getChurchRank();
                if (church != null) {
                    switch (church) {
                        case "PRETRE" -> pretres++;
                        case "ARCHEVEQUE" -> archeveques++;
                        case "CARDINAL" -> cardinaux++;
                    }
                }
            } catch (Exception e) { /* skip */ }
        }

        update.set("#DStatMarquis.Text", String.valueOf(marquis));
        update.set("#DStatDucs.Text", String.valueOf(ducs));
        update.set("#DStatComtes.Text", String.valueOf(comtes));
        update.set("#DStatBarons.Text", String.valueOf(barons));
        update.set("#DStatChevaliers.Text", String.valueOf(chevaliers));
        update.set("#DStatCardinals.Text", String.valueOf(cardinaux));
        update.set("#DStatArcheveques.Text", String.valueOf(archeveques));
        update.set("#DStatPretres.Text", String.valueOf(pretres));

        long guildMoney = 0;
        for (var g : com.eldanior.system.guild.GuildManager.getAll()) {
            guildMoney += g.getTreasury();
        }
        long familyMoney = 0;
        for (var f : com.eldanior.system.titles.nobility.family.FamilyManager.getAll()) {
            if (com.eldanior.system.titles.nobility.family.FamilyManager.isFamilyTaken(f.getId())) {
                familyMoney += com.eldanior.system.titles.nobility.family.FamilyManager.getRuntimeData(f.getId()).getTreasury();
            }
        }
        long parcelMoney = 0;
        for (com.eldanior.system.territory.ParcelData p : parcels) {
            parcelMoney += p.getTreasury();
        }
        long grandTotal = totalMoney + guildMoney + familyMoney + parcelMoney;
        update.set("#DStatTotalMoney.Text", formatMoney(grandTotal));
        update.set("#DStatPlayerMoney.Text", formatMoney(totalMoney));
        update.set("#DStatGuildMoney.Text", formatMoney(guildMoney));
        update.set("#DStatParcelMoney.Text", formatMoney(parcelMoney));

        int pkCount = 0;
        for (com.hypixel.hytale.server.core.universe.PlayerRef pRef2 : com.hypixel.hytale.server.core.universe.Universe.get().getPlayers()) {
            try {
                var tRef2 = pRef2.getReference();
                if (tRef2 == null) continue;
                var tStore2 = tRef2.getStore();
                com.eldanior.system.config.Player.PlayerLevelData d2 = tStore2.getComponent(tRef2, com.eldanior.system.EldaniorSystem.get().getPlayerLevelDataType());
                if (d2 != null && d2.isPK()) pkCount++;
            } catch (Exception e) { /* skip */ }
        }
        update.set("#DStatPKCount.Text", String.valueOf(pkCount));
        update.set("#DStatPvpCities.Text", String.valueOf(pvpCities));

        update.set("#DStatTax.Text", (int)(com.eldanior.system.territory.ParcelEconomyManager.TAX_RATE * 100) + "%");
        update.set("#DStatMaxLv.Text", String.valueOf(com.eldanior.system.config.Player.PlayerLevelData.MAX_LEVEL));

        int activeParties = 0;
        for (com.hypixel.hytale.server.core.universe.PlayerRef pRef3 : com.hypixel.hytale.server.core.universe.Universe.get().getPlayers()) {
            if (com.eldanior.system.party.PartyManager.hasParty(com.eldanior.system.config.UUIDExtractor.getUUID(pRef3))) {
                activeParties++;
            }
        }
        update.set("#DStatParties.Text", String.valueOf(activeParties / 2));

        update.set("#DStatDuelsActive.Text", "0");

        int chestsTotal = 0, chestsDonjon = 0, chestsDefault = 0, chestsLegend = 0, chestsOr = 0;
        try {
            Player playerForWorld = store.getComponent(ref, Player.getComponentType());
            if (playerForWorld != null && playerForWorld.getWorld() != null) {
                com.eldanior.system.TreasureChest.resources.TreasureChestTemplate template =
                    playerForWorld.getWorld().getChunkStore().getStore().getResource(com.eldanior.system.EldaniorSystem.CHEST_TEMPLATE_TYPE);
                if (template != null) {
                    for (String key : template.getTemplateKeys()) {
                        chestsTotal++;
                        String[] coords = key.split(",");
                        if (coords.length == 3) {
                            int cx = Integer.parseInt(coords[0]);
                            int cy = Integer.parseInt(coords[1]);
                            int cz = Integer.parseInt(coords[2]);
                            String dropList = template.getDropList(cx, cy, cz);
                            if (dropList != null) {
                                switch (dropList) {
                                    case "donjon", "donjon_common" -> chestsDonjon++;
                                    case "default" -> chestsDefault++;
                                    case "legendary" -> chestsLegend++;
                                    case "gold" -> chestsOr++;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) { EldaniorLogger.error("AdminScreen.chests", e); }

        update.set("#DStatChestsTotal.Text", String.valueOf(chestsTotal));
        update.set("#DStatChestsDonjon.Text", String.valueOf(chestsDonjon));
        update.set("#DStatChestsDefault.Text", String.valueOf(chestsDefault));
        update.set("#DStatChestsLegend.Text", String.valueOf(chestsLegend));
        update.set("#DStatChestsOr.Text", String.valueOf(chestsOr));

        // === EFFETS VISUELS — Rapport de couverture (ELD-11) ===
        if (com.eldanior.system.config.Effects.SkillEffectConfig.isValidated()) {
            int total = com.eldanior.system.config.Effects.SkillEffectConfig.getTotalSkills();
            int mapped = com.eldanior.system.config.Effects.SkillEffectConfig.getSkillsWithEffect();
            int unmapped = com.eldanior.system.config.Effects.SkillEffectConfig.getSkillsWithoutEffect();
            int mappings = com.eldanior.system.config.Effects.SkillEffectConfig.getMappingCount();
            int pct = total > 0 ? (mapped * 100 / total) : 0;
            var missing = com.eldanior.system.config.Effects.SkillEffectConfig.getMissingAssets();

            update.set("#DStatEffectsCoverage.Text", mapped + "/" + total + " (" + pct + "%)");
            update.set("#DStatEffectsMappings.Text", String.valueOf(mappings));
            update.set("#DStatEffectsUnmapped.Text", String.valueOf(unmapped));
            update.set("#DStatEffectsMissing.Text", missing.isEmpty() ? "0" : missing.size() + " — " + String.join(", ", missing));
            update.set("#DStatEffectsMissing.Style.TextColor", missing.isEmpty() ? "#4CAF50" : "#cc4444");
        }

    }

    private static String formatMoney(long amount) {
        if (amount >= 1_000_000) return String.format("%.1fM", amount / 1_000_000.0);
        if (amount >= 1_000) return String.format("%.1fK", amount / 1_000.0);
        return String.valueOf(amount);
    }

    // ==================== HOLOGRAMMES ====================

    private static final java.util.List<String> cachedHoloIds = new java.util.ArrayList<>();

    private void handleHologramAction(String action, String param, Ref<EntityStore> ref, Store<EntityStore> store) {
        if ("holo_delete".equals(action) && param != null) {
            int idx = EldaniorLogger.parseSafeInt(param, -1);
            if (idx >= 0 && idx < cachedHoloIds.size()) {
                String holoId = cachedHoloIds.get(idx);
                com.eldanior.system.hologram.HologramManager.delete(holoId);
                Player player = store.getComponent(ref, Player.getComponentType());
                if (player != null) player.getPlayerRef().sendMessage(Message.raw("§aHologramme " + holoId + " supprime !"));
                // Refresh
                UICommandBuilder update = new UICommandBuilder();
                populateHologramsTab(update);
                this.sendUpdate(update);
            }
        }
    }

    private void populateHologramsTab(UICommandBuilder update) {
        var all = new java.util.ArrayList<>(com.eldanior.system.hologram.HologramManager.getAll());
        cachedHoloIds.clear();
        for (var h : all) cachedHoloIds.add(h.getId());

        update.set("#HoloCount.Text", all.size() + " hologramme(s)");

        for (int i = 0; i < HOLO_SLOTS; i++) {
            if (i < all.size()) {
                var h = all.get(i);
                update.set("#HoloSlot" + i + ".Visible", true);
                update.set("#HoloId" + i + ".Text", h.getId());
                update.set("#HoloText" + i + ".Text", h.getText());
                update.set("#HoloPos" + i + ".Text", h.getLocationString());
            } else {
                update.set("#HoloSlot" + i + ".Visible", false);
            }
        }
    }

    private void populateEconomyTab(UICommandBuilder update) {
        update.set("#EStatShop.Text", String.valueOf(com.eldanior.system.shop.ShopManager.getListings().size()));
        update.set("#EStatBlackMarket.Text", String.valueOf(com.eldanior.system.shop.ShopManager.getBlackMarketListings().size()));
        update.set("#EStatGuilds.Text", String.valueOf(com.eldanior.system.guild.GuildManager.getAll().size()));
        int familyCount = 0;
        for (var f : com.eldanior.system.titles.nobility.family.FamilyManager.getAll()) {
            if (com.eldanior.system.titles.nobility.family.FamilyManager.isFamilyTaken(f.getId())) familyCount++;
        }
        update.set("#EStatFamilies.Text", familyCount + "/" + com.eldanior.system.titles.nobility.family.FamilyManager.getAll().size());
        update.set("#EStatTaxRate.Text", "Taux de taxe : " + (int)(com.eldanior.system.territory.ParcelEconomyManager.TAX_RATE * 100) + "%");

        // === ARGENT EN CIRCULATION ===
        long playerMoney = 0;
        for (com.hypixel.hytale.server.core.universe.PlayerRef pRef : com.hypixel.hytale.server.core.universe.Universe.get().getPlayers()) {
            try {
                var tRef = pRef.getReference();
                if (tRef == null) continue;
                com.eldanior.system.config.Player.PlayerLevelData data = tRef.getStore().getComponent(tRef, com.eldanior.system.EldaniorSystem.get().getPlayerLevelDataType());
                if (data != null) playerMoney += data.getMoney();
            } catch (Exception e) { /* skip */ }
        }
        long guildMoney = 0;
        for (var g : com.eldanior.system.guild.GuildManager.getAll()) {
            guildMoney += g.getTreasury();
        }
        long familyMoney = 0;
        for (var f : com.eldanior.system.titles.nobility.family.FamilyManager.getAll()) {
            if (com.eldanior.system.titles.nobility.family.FamilyManager.isFamilyTaken(f.getId())) {
                familyMoney += com.eldanior.system.titles.nobility.family.FamilyManager.getRuntimeData(f.getId()).getTreasury();
            }
        }
        long parcelMoney = 0;
        for (com.eldanior.system.territory.ParcelData p : com.eldanior.system.territory.ParcelManager.getAll()) {
            parcelMoney += p.getTreasury();
        }
        update.set("#EMoneyTotal.Text", "Total: " + formatMoney(playerMoney + guildMoney + familyMoney + parcelMoney) + " Or");
        update.set("#EMoneyBreakdown.Text", "Joueurs: " + formatMoney(playerMoney) + " | Guildes: " + formatMoney(guildMoney)
            + " | Familles: " + formatMoney(familyMoney) + " | Parcelles: " + formatMoney(parcelMoney));

        // === TOP 5 GUILDES PAR TRESORERIE (compact) ===
        java.util.List<com.eldanior.system.guild.Guild> guilds = new java.util.ArrayList<>(com.eldanior.system.guild.GuildManager.getAll());
        guilds.sort((a, b) -> Long.compare(b.getTreasury(), a.getTreasury()));
        for (int i = 0; i < 5; i++) {
            if (i < guilds.size()) {
                var g = guilds.get(i);
                update.set("#EGuildLine" + i + ".Visible", true);
                update.set("#EGuildLine" + i + ".Text", (i + 1) + ". " + g.getName() + " [" + g.getTag() + "] — " + g.getMemberCount() + " mbr — " + formatMoney(g.getTreasury()) + " Or");
            } else {
                update.set("#EGuildLine" + i + ".Visible", false);
            }
        }

        // === FAMILLES ACTIVES (compact) ===
        int fi = 0;
        for (var f : com.eldanior.system.titles.nobility.family.FamilyManager.getAll()) {
            if (fi >= 5) break;
            if (com.eldanior.system.titles.nobility.family.FamilyManager.isFamilyTaken(f.getId())) {
                var rd = com.eldanior.system.titles.nobility.family.FamilyManager.getRuntimeData(f.getId());
                update.set("#EFamilyLine" + fi + ".Visible", true);
                update.set("#EFamilyLine" + fi + ".Text", f.getDisplayName() + " — Tresor: " + formatMoney(rd.getTreasury()) + " Or | Contrib: " + formatMoney(rd.getContribution()));
                fi++;
            }
        }
        for (int i = fi; i < 5; i++) {
            update.set("#EFamilyLine" + i + ".Visible", false);
        }
    }

    // ==================== UTILITIES ====================

    private void deleteDataFile(String filename) {
        try {
            java.nio.file.Path dataDir = com.eldanior.system.EldaniorSystem.get().getDataDirectory().resolve("eldanior_data");
            java.io.File f = dataDir.resolve(filename).toFile();
            if (f.exists()) f.delete();
        } catch (Exception e) { EldaniorLogger.error("AdminScreen", e); }
    }

    // ==================== EVENT DATA ====================

    public static class AdminEventData {
        public static final BuilderCodec<AdminEventData> CODEC = BuilderCodec.builder(AdminEventData.class, AdminEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("Param", Codec.STRING), (d, v) -> d.param = v, d -> d.param).add()
                .append(new KeyedCodec<>("InputText", Codec.STRING), (d, v) -> d.inputText = v, d -> d.inputText).add()
                .build();

        public String action;
        public String param;
        public String inputText;
    }
}
