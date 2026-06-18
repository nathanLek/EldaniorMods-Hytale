package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.shop.ShopManager;
import com.eldanior.system.shop.ShopManager.ShopListing;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;

public class ShopTab {

    public static final int MAX_SHOP_SLOTS = 20;
    private static int currentPage = 0;

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID myUUID = getPlayerUUID(ref, store);
        List<ShopListing> listings = ShopManager.getListings();

        int totalPages = Math.max(1, (int) Math.ceil((double) listings.size() / MAX_SHOP_SLOTS));
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;

        int startIdx = currentPage * MAX_SHOP_SLOTS;

        ui.set("#ShopCount.Text", "MARCHE (" + listings.size() + " annonces)");
        ui.set("#ShopPage.Text", "Page " + (currentPage + 1) + " / " + totalPages);

        for (int i = 0; i < MAX_SHOP_SLOTS; i++) {
            int listIdx = startIdx + i;
            if (listIdx < listings.size()) {
                ShopListing listing = listings.get(listIdx);
                ItemStack item = listing.getItem();
                boolean isOwner = myUUID != null && myUUID.equals(listing.getSellerUUID());

                ui.set("#ShopRow" + i + ".Visible", true);

                // Item icon
                ui.set("#ShopIcon" + i + ".Visible", true);
                ui.set("#ShopIcon" + i + ".ItemId", item.getItemId());

                // Item name + rarity
                String itemName = getItemName(item);
                ui.set("#ShopName" + i + ".Text", itemName);
                ui.set("#ShopRarity" + i + ".Text", getItemRarity(item));

                // Quantity + Seller + price
                ui.set("#ShopQty" + i + ".Text", item.getQuantity() > 1 ? "x" + item.getQuantity() : "");
                ui.set("#ShopSeller" + i + ".Text", "Vendeur : " + listing.getSellerName());
                ui.set("#ShopPrice" + i + ".Text", listing.getPrice() + " Or");

                // Rarity color on label + border
                String rarityColor = getRarityColor(item);
                ui.set("#ShopRarity" + i + ".Style.TextColor", rarityColor);
                ui.set("#ShopBorder" + i + ".Background", rarityColor);

                // Buttons: owner/admin voit retirer, non-owner voit acheter
                ui.set("#ShopBtnBuy" + i + ".Visible", !isOwner);
                ui.set("#ShopBtnCancel" + i + ".Visible", isOwner);
            } else {
                ui.set("#ShopRow" + i + ".Visible", false);
            }
        }
    }

    public static boolean handleBuy(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int slotIdx;
        try { slotIdx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        int idx = currentPage * MAX_SHOP_SLOTS + slotIdx;

        // Pre-checks before atomic buy (read-only, no race condition risk)
        ShopListing peekListing = ShopManager.getListing(idx);
        if (peekListing == null) return false;

        UUID myUUID = getPlayerUUID(ref, store);
        if (myUUID == null || myUUID.equals(peekListing.getSellerUUID())) return false;

        // PK ne peuvent pas acheter au shop normal
        ComponentType<EntityStore, PlayerLevelData> typeCheck = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData dataCheck = store.getComponent(ref, typeCheck);
        if (dataCheck != null && dataCheck.isPK()) {
            Player p = store.getComponent(ref, Player.getComponentType());
            if (p != null) p.getPlayerRef().sendMessage(Message.raw("§cLes criminels n'ont pas acces au Marche ! Utilisez le Marche Noir."));
            return false;
        }

        // Check money
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || data.getMoney() < peekListing.getPrice()) {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) player.getPlayerRef().sendMessage(Message.raw("§cPas assez d'or ! (besoin de " + peekListing.getPrice() + ")"));
            return false;
        }

        Player buyer = store.getComponent(ref, Player.getComponentType());
        if (buyer == null) return false;

        // Atomic buy: remove listing in one synchronized operation to prevent duplication
        ShopListing listing = ShopManager.buyListing(idx);
        if (listing == null) {
            buyer.getPlayerRef().sendMessage(Message.raw("§cCet objet a deja ete achete !"));
            return false;
        }

        // Give item to buyer
        var result = buyer.getInventory().getHotbar().addItemStack(listing.getItem());
        if (!result.succeeded()) {
            // Re-add listing since we already removed it but can't give the item
            ShopManager.addListing(listing.getSellerUUID(), listing.getSellerName(), listing.getItem(), listing.getPrice());
            buyer.getPlayerRef().sendMessage(Message.raw("§cInventaire plein !"));
            return false;
        }

        // Deduct money from buyer
        data.removeMoney(listing.getPrice());
        store.putComponent(ref, type, data);

        // Add money to seller (online or pending)
        PlayerRef sellerRef = com.hypixel.hytale.server.core.universe.Universe.get().getPlayer(listing.getSellerUUID());
        if (sellerRef != null) {
            try {
                var sRef = sellerRef.getReference();
                if (sRef != null) {
                    var sStore = sRef.getStore();
                    PlayerLevelData sData = sStore.getComponent(sRef, type);
                    if (sData != null) {
                        sData.addMoney(listing.getPrice());
                        sStore.putComponent(sRef, type, sData);
                        sellerRef.sendMessage(Message.raw("§a" + buyer.getPlayerRef().getUsername() + " a achete votre objet pour " + listing.getPrice() + " Or !"));
                    }
                }
            } catch (Exception e) { EldaniorLogger.error("ShopTab", e); }
        } else {
            // Vendeur deconnecte -> gains en attente
            ShopManager.addPendingEarnings(listing.getSellerUUID(), listing.getPrice());
        }

        buyer.getPlayerRef().sendMessage(Message.raw("§aObjet achete pour " + listing.getPrice() + " Or !"));
        return true;
    }

    public static boolean handleCancel(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int slotIdx;
        try { slotIdx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        int idx = currentPage * MAX_SHOP_SLOTS + slotIdx;

        ShopListing listing = ShopManager.getListing(idx);
        if (listing == null) return false;

        UUID myUUID = getPlayerUUID(ref, store);
        Player me = store.getComponent(ref, Player.getComponentType());
        if (myUUID == null || me == null) return false;

        boolean isOwner = myUUID.equals(listing.getSellerUUID());
        boolean isAdmin = me.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION);
        if (!isOwner && !isAdmin) return false;

        if (isOwner) {
            // Owner: retourne l'item directement
            var result = me.getInventory().getHotbar().addItemStack(listing.getItem());
            if (!result.succeeded()) {
                me.getPlayerRef().sendMessage(Message.raw("§cInventaire plein !"));
                return false;
            }
            ShopManager.removeListing(idx);
            me.getPlayerRef().sendMessage(Message.raw("§7Annonce retiree, objet recupere."));
        } else {
            // Admin: retourne l'item au vendeur (en ligne ou pending)
            PlayerRef sellerRef = com.hypixel.hytale.server.core.universe.Universe.get().getPlayer(listing.getSellerUUID());
            if (sellerRef != null) {
                // Vendeur en ligne -> donner l'item
                try {
                    var sRef = sellerRef.getReference();
                    if (sRef != null) {
                        var sStore = sRef.getStore();
                        var sPlayer = sStore.getComponent(sRef, Player.getComponentType());
                        if (sPlayer != null) {
                            sPlayer.getInventory().getHotbar().addItemStack(listing.getItem());
                            sellerRef.sendMessage(Message.raw("§eUn admin a retire votre annonce. Objet restitue."));
                        }
                    }
                } catch (Exception e) { EldaniorLogger.error("ShopTab", e); }
            }
            // Note: si vendeur deconnecte, l'item est perdu (pas de pending items system)
            // On pourrait ajouter un systeme de pending items plus tard
            ShopManager.removeListing(idx);
            me.getPlayerRef().sendMessage(Message.raw("§eAnnonce de " + listing.getSellerName() + " retiree (admin)."));
        }
        return true;
    }

    public static void nextPage() { currentPage++; }
    public static void prevPage() { if (currentPage > 0) currentPage--; }

    private static String getRarityColor(ItemStack item) {
        try {
            var itemConfig = item.getItem();
            if (itemConfig != null) {
                int qualityIdx = itemConfig.getQualityIndex();
                var qualityMap = ItemQuality.getAssetMap();
                if (qualityMap != null) {
                    ItemQuality quality = qualityMap.getAsset(qualityIdx);
                    if (quality != null) {
                        String id = quality.getId().toLowerCase();
                        if (id.contains("unique")) return "#cc4444";
                        if (id.contains("divine")) return "#7EB8DA";
                        if (id.contains("legendary")) return "#FFD700";
                        if (id.contains("epic")) return "#9C27B0";
                        if (id.contains("rare")) return "#2196F3";
                        if (id.contains("uncommon")) return "#4CAF50";
                    }
                }
            }
        } catch (Exception e) { EldaniorLogger.error("ShopTab", e); }
        return "#8899aa"; // Common/default
    }

    public static String getItemRarity(ItemStack item) {
        try {
            var itemConfig = item.getItem();
            if (itemConfig != null) {
                int qualityIdx = itemConfig.getQualityIndex();
                var qualityMap = ItemQuality.getAssetMap();
                if (qualityMap != null) {
                    ItemQuality quality = qualityMap.getAsset(qualityIdx);
                    if (quality != null) {
                        String translated = InventaireTab.translate(quality.getLocalizationKey());
                        if (translated != null) return translated;
                        return InventaireTab.formatItemName(quality.getId());
                    }
                }
            }
        } catch (Exception e) { EldaniorLogger.error("ShopTab", e); }
        return "";
    }

    public static String getItemName(ItemStack item) {
        try {
            var itemConfig = item.getItem();
            if (itemConfig != null) {
                String key = itemConfig.getTranslationKey();
                String translated = InventaireTab.translate(key);
                if (translated != null) return translated;
            }
        } catch (Exception e) { EldaniorLogger.error("ShopTab", e); }
        return InventaireTab.formatItemName(item.getItemId());
    }

    private static UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        return extractUUID(pRef);
    }

    private static UUID extractUUID(PlayerRef playerRef) {
        if (playerRef == null) return null;
        try { return UUIDExtractor.getUUID(playerRef); } catch (Exception e) { return null; }
    }
}
