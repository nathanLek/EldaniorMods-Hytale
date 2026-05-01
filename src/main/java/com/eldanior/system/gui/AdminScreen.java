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
        "Dashboard", "Players", "World", "Economy", "Data"
    };

    private static final int PLAYERS_PER_PAGE = 20;
    private static int playerPage = 0;
    private static final java.util.List<String> allPlayerNames = new java.util.ArrayList<>();
    private static String managedPlayerName = null;
    private static int worldTerrPage = 0;
    private static final java.util.List<com.eldanior.system.territory.ParcelData> worldTerrList = new java.util.ArrayList<>();

    public AdminScreen(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, AdminEventData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {

        ui.append("Admin/AdminPage.ui");

        // Double-check permission
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || !player.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) return;

        // Populate dashboard
        AdminTab.populate(ui, ref, store);
        populateDashboard(ui, ref, store);

        // === TAB NAVIGATION ===
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnDashboard", EventData.of("Action", "tab_dashboard"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnPlayers", EventData.of("Action", "tab_players"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnWorld", EventData.of("Action", "tab_world"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnEconomy", EventData.of("Action", "tab_economy"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#TabBtnData", EventData.of("Action", "tab_data"));

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

        // === WORLD — Pagination + actions territoires ===
        events.addEventBinding(CustomUIEventBindingType.Activating, "#WBtnTPrev", EventData.of("Action", "world_tprev"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#WBtnTNext", EventData.of("Action", "world_tnext"));
        for (int i = 0; i < 10; i++) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#WTPvp" + i, EventData.of("Action", "world_pvp").append("Param", String.valueOf(i)));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#WTDel" + i, EventData.of("Action", "world_del").append("Param", String.valueOf(i)));
        }

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
                    AdminTab.selectPlayer(Integer.parseInt(param));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_reset" -> {
                if (AdminTab.handleResetLevel(ref, store)) {
                    if (player != null) player.sendMessage(Message.raw("§aJoueur reset !"));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_xp" -> {
                if (param != null && AdminTab.handleAddXP(Integer.parseInt(param), ref, store)) {
                    if (player != null) player.sendMessage(Message.raw("§a+" + param + " XP !"));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_setlv" -> {
                if (param != null && AdminTab.handleSetLevel(Integer.parseInt(param), ref, store)) {
                    if (player != null) player.sendMessage(Message.raw("§aNiveau defini a " + param));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_gold" -> {
                if (param != null && AdminTab.handleGiveGold(Long.parseLong(param), ref, store)) {
                    if (player != null) player.sendMessage(Message.raw("§a+" + param + " Or !"));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_class" -> {
                if (param != null && AdminTab.handleSetClass(param, ref, store)) {
                    if (player != null) player.sendMessage(Message.raw("§aClasse: " + param));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_nob" -> {
                if (param != null && AdminTab.handleNobilityPromote(param, ref, store)) {
                    if (player != null) player.sendMessage(Message.raw("§aRang: " + param));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_ch" -> {
                if (param != null && AdminTab.handleChurchPromote(param, ref, store)) {
                    if (player != null) player.sendMessage(Message.raw("§aEglise: " + param));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_pk" -> {
                if (AdminTab.handleSetPK(ref, store)) {
                    if (player != null) player.sendMessage(Message.raw("§ePK toggle !"));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_titlerst" -> {
                if (AdminTab.handleResetTitles(ref, store)) {
                    if (player != null) player.sendMessage(Message.raw("§aTitres reset !"));
                    refreshDashboard(ref, store);
                }
            }
            case "admin_relic" -> {
                if (player != null) player.sendMessage(Message.raw("§eTapez : /es getrelic"));
            }
            case "admin_prefill" -> {
                if (param != null && player != null) {
                    String name = AdminTab.getSelectedPlayerName();
                    String cmd = param.replace("<player>", name != null ? name : "<player>");
                    player.sendMessage(Message.raw("§6Tapez dans le chat :"));
                    player.sendMessage(Message.raw("§f/es " + cmd));
                }
            }
            case "admin_reset_guilds" -> {
                if (player != null) {
                    com.eldanior.system.guild.GuildManager.init();
                    deleteDataFile("guilds.properties");
                    player.sendMessage(Message.raw("§c§lGuildes reinitialises !"));
                }
            }
            case "admin_reset_families" -> {
                if (player != null) {
                    com.eldanior.system.titles.nobility.family.FamilyManager.init();
                    deleteDataFile("families.properties");
                    player.sendMessage(Message.raw("§c§lFamilles reinitialises !"));
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
                    player.sendMessage(Message.raw("§c§lParcelles reinitialises !"));
                }
            }
            case "admin_reset_shop" -> {
                if (player != null) {
                    com.eldanior.system.shop.ShopManager.init();
                    deleteDataFile("shop.properties");
                    deleteDataFile("blackmarket.properties");
                    player.sendMessage(Message.raw("§c§lShop et Marche Noir reinitialises !"));
                }
            }
            case "admin_reset_classements" -> {
                if (player != null) {
                    com.eldanior.system.classement.ClassementManager.init();
                    deleteDataFile("classements.properties");
                    player.sendMessage(Message.raw("§c§lClassements reinitialises !"));
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
                    player.sendMessage(Message.raw("§c§l=== RESET COMPLET ==="));
                    player.sendMessage(Message.raw("§cGuildes, Familles, Parcelles, Classements, Shop reinitialises !"));
                    player.sendMessage(Message.raw("§7Redemarrez le serveur pour finaliser."));
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
            populateWorldTab(update);
            populateWorldList(update);
        }
        if ("Economy".equals(targetId)) {
            populateEconomyTab(update);
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
        switch (action) {
            case "world_tprev" -> {
                if (worldTerrPage > 0) { worldTerrPage--; refreshWorldList(ref, store); }
            }
            case "world_tnext" -> {
                int maxPage = Math.max(0, (worldTerrList.size() - 1) / 10);
                if (worldTerrPage < maxPage) { worldTerrPage++; refreshWorldList(ref, store); }
            }
            case "world_pvp" -> {
                if (param != null) {
                    int idx = Integer.parseInt(param) + (worldTerrPage * 10);
                    if (idx < worldTerrList.size()) {
                        com.eldanior.system.territory.ParcelData p = worldTerrList.get(idx);
                        p.setPvpEnabled(!p.isPvpEnabled());
                        com.eldanior.system.territory.ParcelManager.saveAll();
                        refreshWorldList(ref, store);
                    }
                }
            }
            case "world_del" -> {
                if (param != null) {
                    int idx = Integer.parseInt(param) + (worldTerrPage * 10);
                    if (idx < worldTerrList.size()) {
                        com.eldanior.system.territory.ParcelManager.deleteParcel(worldTerrList.get(idx).getId());
                        refreshWorldList(ref, store);
                    }
                }
            }
        }
    }

    private void refreshWorldList(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        populateWorldTab(update);
        populateWorldList(update);
        this.sendUpdate(update);
    }

    private void populateWorldList(UICommandBuilder update) {
        worldTerrList.clear();
        for (com.eldanior.system.territory.ParcelData p : com.eldanior.system.territory.ParcelManager.getAll()) {
            worldTerrList.add(p);
        }
        // Trier : KINGDOM d'abord, puis TERRITORY, puis CITY, puis le reste
        worldTerrList.sort((a, b) -> a.getType().ordinal() - b.getType().ordinal());

        int totalPages = Math.max(1, (worldTerrList.size() + 9) / 10);
        if (worldTerrPage >= totalPages) worldTerrPage = totalPages - 1;
        update.set("#WTPageInfo.Text", "Page " + (worldTerrPage + 1) + "/" + totalPages);

        int startIdx = worldTerrPage * 10;
        for (int i = 0; i < 10; i++) {
            int idx = startIdx + i;
            if (idx < worldTerrList.size()) {
                com.eldanior.system.territory.ParcelData p = worldTerrList.get(idx);
                update.set("#WTSlot" + i + ".Visible", true);
                update.set("#WTName" + i + ".Text", p.getName());
                update.set("#WTType" + i + ".Text", p.getType().name());

                String ownerName = p.getOwnerName();
                update.set("#WTOwner" + i + ".Text", ownerName != null && !ownerName.isEmpty() ? ownerName : "Sans proprio");

                String info = "Tresor: " + formatMoney(p.getTreasury());
                if (p.isPvpEnabled()) info += " | PVP ON";
                if (p.isRented()) info += " | Loue";
                if (p.getPrice() > 0) info += " | " + formatMoney(p.getPrice()) + " Or";
                update.set("#WTInfo" + i + ".Text", info);
            } else {
                update.set("#WTSlot" + i + ".Visible", false);
            }
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
                    int idx = Integer.parseInt(param) + (playerPage * PLAYERS_PER_PAGE);
                    if (idx < allPlayerNames.size()) {
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
        int idx = Integer.parseInt(param) + (playerPage * PLAYERS_PER_PAGE);
        if (idx < allPlayerNames.size()) {
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

        // Noblesse & Eglise — compter les joueurs par rang
        String kingName = com.eldanior.system.titles.nobility.NobilityManager.getCurrentKingName();
        update.set("#DStatKing.Text", (kingName != null && !kingName.isEmpty()) ? kingName : "Aucun");

        String popeName = com.eldanior.system.titles.church.ChurchManager.getCurrentPopeName();
        update.set("#DStatPope.Text", (popeName != null && !popeName.isEmpty()) ? popeName : "Aucun");

        // Compter les rangs via les joueurs online
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

                // Argent joueur
                totalMoney += data.getMoney();

                // Noblesse
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

                // Eglise
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

        // Argent total = joueurs + guildes + familles + tresoreries parcelles
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

        // PK & PvP
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

        // Duels actifs — approximation via DuelManager
        // Les duels sont stockes dans une map statique, on ne peut pas compter facilement sans getter

        // Parametres
        update.set("#DStatTax.Text", (int)(com.eldanior.system.territory.ParcelEconomyManager.TAX_RATE * 100) + "%");
        update.set("#DStatMaxLv.Text", String.valueOf(com.eldanior.system.config.Player.PlayerLevelData.MAX_LEVEL));

        // Groupes actifs
        int activeParties = 0;
        for (com.hypixel.hytale.server.core.universe.PlayerRef pRef3 : com.hypixel.hytale.server.core.universe.Universe.get().getPlayers()) {
            if (com.eldanior.system.party.PartyManager.hasParty(com.eldanior.system.config.UUIDExtractor.getUUID(pRef3))) {
                activeParties++;
            }
        }
        update.set("#DStatParties.Text", String.valueOf(activeParties / 2)); // /2 car chaque membre est compté

        // Duels actifs
        update.set("#DStatDuelsActive.Text", "0");

        // Coffres au tresor — lire depuis le TreasureChestTemplate du ChunkStore
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
    }

    private static String formatMoney(long amount) {
        if (amount >= 1_000_000) return String.format("%.1fM", amount / 1_000_000.0);
        if (amount >= 1_000) return String.format("%.1fK", amount / 1_000.0);
        return String.valueOf(amount);
    }

    private void populateWorldTab(UICommandBuilder update) {
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
        update.set("#WStatKingdoms.Text", String.valueOf(kingdoms));
        update.set("#WStatTerritories.Text", String.valueOf(territories));
        update.set("#WStatCities.Text", String.valueOf(cities));
        update.set("#WStatPlots.Text", String.valueOf(plots));
        update.set("#WStatHousing.Text", String.valueOf(housing));
        update.set("#WStatFarms.Text", String.valueOf(farms));
        update.set("#WStatPvp.Text", pvpCities + " ville(s) PvP actif sur " + cities);
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
