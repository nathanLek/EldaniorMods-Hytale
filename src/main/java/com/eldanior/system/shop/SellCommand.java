package com.eldanior.system.shop;

import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.eldanior.system.persistence.PersistenceManager;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SellCommand extends AbstractAsyncCommand {

    private static final int MAX_SELL_PRICE = 10_000_000;
    private final RequiredArg<Integer> priceArg;

    public SellCommand() {
        super("sell", "Mettre en vente l'objet en main (prix en or)");
        this.priceArg = this.withRequiredArg("prix", "Prix en or", ArgTypes.INTEGER);
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    @SuppressWarnings("removal")
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        Ref<EntityStore> ref = ctx.senderAsPlayerRef();
        if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);

        Store<EntityStore> store = ref.getStore();
        World world = ((EntityStore) store.getExternalData()).getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);

        int price = this.priceArg.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = store.getComponent(ref, PlayerRef.getComponentType());
                Player sender = store.getComponent(ref, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                if (price <= 0) {
                    senderRef.sendMessage(Message.raw("§cLe prix doit etre superieur a 0."));
                    return;
                }

                if (price > MAX_SELL_PRICE) {
                    senderRef.sendMessage(Message.raw("§cPrix maximum autorise : 10 000 000 Or."));
                    return;
                }

                // Recuperer l'item en main
                var inventory = sender.getInventory();
                ItemStack item = inventory.getActiveHotbarItem();

                if (item == null || item.isEmpty()) {
                    senderRef.sendMessage(Message.raw("§cVous n'avez rien en main !"));
                    return;
                }

                UUID sellerUUID = UUIDExtractor.getUUID(senderRef);
                if (sellerUUID == null) return;

                // Determiner si PK
                com.eldanior.system.config.Player.PlayerLevelData data = store.getComponent(ref,
                        com.eldanior.system.EldaniorSystem.get().getPlayerLevelDataType());
                boolean isPK = data != null && data.isPK();
                boolean isAdmin = senderRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION);

                // Check limite (3 max pour non-admin)
                if (!isAdmin) {
                    int currentListings = isPK
                            ? ShopManager.getBlackMarketPlayerListingCount(sellerUUID)
                            : ShopManager.getPlayerListingCount(sellerUUID);
                    if (currentListings >= ShopManager.MAX_LISTINGS_PER_PLAYER) {
                        senderRef.sendMessage(Message.raw("§cLimite atteinte ! Vous avez deja " + ShopManager.MAX_LISTINGS_PER_PLAYER + " objets en vente."));
                        return;
                    }
                }

                // Retirer l'item de la hotbar
                byte activeSlot = inventory.getActiveHotbarSlot();
                inventory.getHotbar().removeItemStackFromSlot(activeSlot);

                // Ajouter au bon marche
                if (isPK) {
                    ShopManager.addBlackMarketListing(sellerUUID, senderRef.getUsername(), item, price);
                } else {
                    ShopManager.addListing(sellerUUID, senderRef.getUsername(), item, price);
                }

                String itemName = item.getItemId();
                try {
                    var itemConfig = item.getItem();
                    if (itemConfig != null) {
                        String key = itemConfig.getTranslationKey();
                        String translated = com.eldanior.system.gui.tabs.InventaireTab.translate(key);
                        if (translated != null) itemName = translated;
                    }
                } catch (Exception e) { EldaniorLogger.error("SellCommand", e); }

                // Persist shop state immediately to prevent data loss on crash (BUGS-12)
                try { PersistenceManager.saveShop(); } catch (Exception e2) { EldaniorLogger.error("SellCommand", e2); }

                senderRef.sendMessage(Message.raw("§a" + itemName + " §amis en vente pour §e" + price + " Or §a!"));
                senderRef.sendMessage(Message.raw("§7Les joueurs peuvent l'acheter via §f/es system §7> Shop"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }

    private UUID getSenderUUID(Player sender) throws Exception {
        var ref = sender.getReference();
        if (ref == null) return null;
        Store<EntityStore> store = ref.getStore();
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        return UUIDExtractor.getUUID(pRef);
    }
}