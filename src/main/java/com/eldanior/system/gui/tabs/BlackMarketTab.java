package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.shop.ShopManager;
import com.eldanior.system.shop.ShopManager.ShopListing;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.persistence.PersistenceManager;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;

public class BlackMarketTab {

    public static final int MAX_SLOTS = 20;
    private static final Map<UUID, Integer> playerPages = new java.util.concurrent.ConcurrentHashMap<>();

    private static int getPage(UUID uuid) { return playerPages.getOrDefault(uuid, 0); }
    private static void setPage(UUID uuid, int page) { playerPages.put(uuid, page); }

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID myUUID = getPlayerUUID(ref, store);
        Player playerCheck = store.getComponent(ref, Player.getComponentType());
        boolean isAdmin = playerCheck != null && playerCheck.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION);
        List<ShopListing> listings = ShopManager.getBlackMarketListings();

        int currentPage = getPage(myUUID);
        int totalPages = Math.max(1, (int) Math.ceil((double) listings.size() / MAX_SLOTS));
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;
        setPage(myUUID, currentPage);
        int startIdx = currentPage * MAX_SLOTS;

        ui.set("#BMCount.Text", "MARCHE NOIR (" + listings.size() + " annonces)");
        ui.set("#BMPage.Text", "Page " + (currentPage + 1) + " / " + totalPages);

        for (int i = 0; i < MAX_SLOTS; i++) {
            int listIdx = startIdx + i;
            if (listIdx < listings.size()) {
                ShopListing listing = listings.get(listIdx);
                ItemStack item = listing.getItem();
                boolean isOwner = myUUID != null && myUUID.equals(listing.getSellerUUID());

                ui.set("#BMRow" + i + ".Visible", true);
                ui.set("#BMIcon" + i + ".Visible", true);
                ui.set("#BMIcon" + i + ".ItemId", item.getItemId());
                ui.set("#BMName" + i + ".Text", ShopTab.getItemName(item));
                ui.set("#BMRarity" + i + ".Text", ShopTab.getItemRarity(item));
                ui.set("#BMQty" + i + ".Text", item.getQuantity() > 1 ? "x" + item.getQuantity() : "");
                ui.set("#BMSeller" + i + ".Text", "Vendeur : " + listing.getSellerName());
                ui.set("#BMPrice" + i + ".Text", listing.getPrice() + " Or");
                ui.set("#BMBtnBuy" + i + ".Visible", !isOwner);
                ui.set("#BMBtnCancel" + i + ".Visible", isOwner || isAdmin);
            } else {
                ui.set("#BMRow" + i + ".Visible", false);
            }
        }
    }

    public static boolean handleBuy(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int slotIdx;
        try { slotIdx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        UUID myUUID = getPlayerUUID(ref, store);
        int idx = getPage(myUUID) * MAX_SLOTS + slotIdx;

        // Pre-checks before atomic buy (read-only, no race condition risk)
        ShopListing peekListing = ShopManager.getBlackMarketListing(idx);
        if (peekListing == null) return false;

        if (myUUID == null || myUUID.equals(peekListing.getSellerUUID())) return false;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || data.getMoney() < peekListing.getPrice()) {
            Player p = store.getComponent(ref, Player.getComponentType());
            if (p != null) p.getPlayerRef().sendMessage(Message.raw("§cPas assez d'or !"));
            return false;
        }

        Player buyer = store.getComponent(ref, Player.getComponentType());
        if (buyer == null) return false;

        // Atomic buy: remove listing in one synchronized operation to prevent duplication
        ShopListing listing = ShopManager.buyBlackMarketListing(idx);
        if (listing == null) {
            buyer.getPlayerRef().sendMessage(Message.raw("§cCet objet a deja ete achete !"));
            return false;
        }

        var result = buyer.getInventory().getHotbar().addItemStack(listing.getItem());
        if (!result.succeeded()) {
            // Re-add listing since we already removed it but can't give the item
            ShopManager.addBlackMarketListing(listing.getSellerUUID(), listing.getSellerName(), listing.getItem(), listing.getPrice());
            buyer.getPlayerRef().sendMessage(Message.raw("§cInventaire plein !"));
            return false;
        }

        data.removeMoney(listing.getPrice());
        store.putComponent(ref, type, data);

        PlayerRef sellerRef = com.hypixel.hytale.server.core.universe.Universe.get().getPlayer(listing.getSellerUUID());
        if (sellerRef != null) {
            try {
                var sRef = sellerRef.getReference(); if (sRef == null) throw new Exception();
                var sStore = sRef.getStore();
                PlayerLevelData sData = sStore.getComponent(sRef, type);
                if (sData != null) { sData.addMoney(listing.getPrice()); sStore.putComponent(sRef, type, sData);
                    sellerRef.sendMessage(Message.raw("§a" + buyer.getPlayerRef().getUsername() + " a achete votre objet (Marche Noir) pour " + listing.getPrice() + " Or !"));
                }
            } catch (Exception e) { EldaniorLogger.error("BlackMarketTab", e); }
        } else {
            ShopManager.addPendingEarnings(listing.getSellerUUID(), listing.getPrice());
        }

        buyer.getPlayerRef().sendMessage(Message.raw("§aObjet achete au Marche Noir pour " + listing.getPrice() + " Or !"));

        // Persist shop state immediately to prevent data loss on crash (BUGS-12)
        try { PersistenceManager.saveShop(); } catch (Exception e) { EldaniorLogger.error("BlackMarketTab", e); }

        return true;
    }

    public static boolean handleCancel(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int slotIdx;
        try { slotIdx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        UUID myUUID = getPlayerUUID(ref, store);
        int idx = getPage(myUUID) * MAX_SLOTS + slotIdx;

        ShopListing listing = ShopManager.getBlackMarketListing(idx);
        if (listing == null) return false;

        Player me = store.getComponent(ref, Player.getComponentType());
        if (myUUID == null || me == null) return false;

        boolean isOwner = myUUID.equals(listing.getSellerUUID());
        boolean isAdmin = me.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION);
        if (!isOwner && !isAdmin) return false;

        if (isOwner) {
            var result = me.getInventory().getHotbar().addItemStack(listing.getItem());
            if (!result.succeeded()) { me.getPlayerRef().sendMessage(Message.raw("§cInventaire plein !")); return false; }
            ShopManager.removeBlackMarketListing(idx);
            me.getPlayerRef().sendMessage(Message.raw("§7Annonce retiree du Marche Noir."));
        } else {
            PlayerRef sellerRef = com.hypixel.hytale.server.core.universe.Universe.get().getPlayer(listing.getSellerUUID());
            if (sellerRef != null) {
                try {
                    var sRef = sellerRef.getReference(); if (sRef != null) {
                        var sStore = sRef.getStore();
                        var sPlayer = sStore.getComponent(sRef, Player.getComponentType());
                        if (sPlayer != null) { sPlayer.getInventory().getHotbar().addItemStack(listing.getItem());
                            sellerRef.sendMessage(Message.raw("§eAdmin a retire votre annonce du Marche Noir."));
                        }
                    }
                } catch (Exception e) { EldaniorLogger.error("BlackMarketTab", e); }
            }
            ShopManager.removeBlackMarketListing(idx);
            me.getPlayerRef().sendMessage(Message.raw("§eAnnonce retiree du Marche Noir (admin)."));
        }

        // Persist shop state immediately to prevent data loss on crash (BUGS-12)
        try { PersistenceManager.saveShop(); } catch (Exception e) { EldaniorLogger.error("BlackMarketTab", e); }

        return true;
    }

    public static void nextPage(UUID uuid) { playerPages.merge(uuid, 1, Integer::sum); }
    public static void prevPage(UUID uuid) { playerPages.compute(uuid, (k, v) -> v != null && v > 0 ? v - 1 : 0); }

    // Make getItemName and getItemRarity accessible - delegate to ShopTab
    static String getItemName(ItemStack item) { return ShopTab.getItemName(item); }
    static String getItemRarity(ItemStack item) { return ShopTab.getItemRarity(item); }

    private static UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        try { return UUIDExtractor.getUUID(pRef); }
        catch (Exception e) { return null; }
    }
}
