package com.eldanior.system.shop;

import com.hypixel.hytale.server.core.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ShopManager {

    private static final List<ShopListing> listings = new CopyOnWriteArrayList<>();
    private static final List<ShopListing> blackMarketListings = new CopyOnWriteArrayList<>();

    // Gains en attente pour les vendeurs deconnectes (UUID -> total or a recevoir)
    private static final Map<UUID, Long> pendingEarnings = new ConcurrentHashMap<>();

    public static void init() {
        listings.clear();
        blackMarketListings.clear();
        pendingEarnings.clear();
        System.out.println("[Eldanior] Systeme de Shop initialise.");
    }

    public static void addListing(UUID sellerUUID, String sellerName, ItemStack item, long price) {
        listings.add(new ShopListing(sellerUUID, sellerName, item, price));
    }

    public static ShopListing getListing(int index) {
        if (index < 0 || index >= listings.size()) return null;
        return listings.get(index);
    }

    /** Achat atomique : retire et retourne le listing, ou null si deja achete. */
    public static synchronized ShopListing buyListing(int index) {
        if (index < 0 || index >= listings.size()) return null;
        return listings.remove(index);
    }

    public static void removeListing(int index) {
        if (index >= 0 && index < listings.size()) {
            listings.remove(index);
        }
    }

    public static List<ShopListing> getListings() {
        return Collections.unmodifiableList(listings);
    }

    public static final int MAX_LISTINGS_PER_PLAYER = 3;

    // ==================== BLACK MARKET ====================

    public static void addBlackMarketListing(UUID sellerUUID, String sellerName, ItemStack item, long price) {
        blackMarketListings.add(new ShopListing(sellerUUID, sellerName, item, price));
    }

    public static ShopListing getBlackMarketListing(int index) {
        if (index < 0 || index >= blackMarketListings.size()) return null;
        return blackMarketListings.get(index);
    }

    public static void removeBlackMarketListing(int index) {
        if (index >= 0 && index < blackMarketListings.size()) blackMarketListings.remove(index);
    }

    /** Achat atomique Black Market. */
    public static synchronized ShopListing buyBlackMarketListing(int index) {
        if (index < 0 || index >= blackMarketListings.size()) return null;
        return blackMarketListings.remove(index);
    }

    public static List<ShopListing> getBlackMarketListings() { return Collections.unmodifiableList(blackMarketListings); }

    public static int getBlackMarketPlayerListingCount(UUID playerUUID) {
        int count = 0;
        for (ShopListing listing : blackMarketListings) {
            if (listing.getSellerUUID().equals(playerUUID)) count++;
        }
        return count;
    }

    // ==================== COMMON ====================

    public static int getListingCount() {
        return listings.size();
    }

    public static int getPlayerListingCount(UUID playerUUID) {
        int count = 0;
        for (ShopListing listing : listings) {
            if (listing.getSellerUUID().equals(playerUUID)) count++;
        }
        return count;
    }

    // Gains en attente
    public static void addPendingEarnings(UUID sellerUUID, long amount) {
        pendingEarnings.merge(sellerUUID, amount, (a, b) -> { long s = a + b; return (s < a) ? Long.MAX_VALUE : s; });
    }

    public static Map<UUID, Long> getPendingEarningsMap() {
        return pendingEarnings;
    }

    public static long collectPendingEarnings(UUID playerUUID) {
        Long amount = pendingEarnings.remove(playerUUID);
        return amount != null ? amount : 0;
    }

    public static class ShopListing {
        private final UUID sellerUUID;
        private final String sellerName;
        private final ItemStack item;
        private final long price;
        private final long timestamp;

        public ShopListing(UUID sellerUUID, String sellerName, ItemStack item, long price) {
            this.sellerUUID = sellerUUID;
            this.sellerName = sellerName;
            this.item = item;
            this.price = price;
            this.timestamp = System.currentTimeMillis();
        }

        public UUID getSellerUUID() { return sellerUUID; }
        public String getSellerName() { return sellerName; }
        public ItemStack getItem() { return item; }
        public long getPrice() { return price; }
        public long getTimestamp() { return timestamp; }
    }
}
