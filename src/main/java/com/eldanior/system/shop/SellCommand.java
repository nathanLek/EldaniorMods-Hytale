package com.eldanior.system.shop;

import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SellCommand extends AbstractAsyncCommand {

    private final RequiredArg<Integer> priceArg;

    public SellCommand() {
        super("sell", "Mettre en vente l'objet en main (prix en or)");
        this.priceArg = this.withRequiredArg("prix", "Prix en or", ArgTypes.INTEGER);
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        int price = this.priceArg.get(ctx);
        if (price <= 0) {
            sender.sendMessage(Message.raw("§cLe prix doit etre superieur a 0."));
            return CompletableFuture.completedFuture(null);
        }

        assert sender.getWorld() != null;
        return CompletableFuture.runAsync(() -> {
            try {
                // Recuperer l'item en main
                var inventory = sender.getInventory();
                ItemStack item = inventory.getItemInHand();

                if (item == null || item.isEmpty()) {
                    sender.sendMessage(Message.raw("§cVous n'avez rien en main !"));
                    return;
                }

                UUID sellerUUID = getSenderUUID(sender);
                if (sellerUUID == null) return;

                // Determiner si PK
                var sRef = sender.getReference();
                if (sRef == null) return;
                var sStore = sRef.getStore();
                com.eldanior.system.config.Player.PlayerLevelData data = sStore.getComponent(sRef,
                        com.eldanior.system.EldaniorSystem.get().getPlayerLevelDataType());
                boolean isPK = data != null && data.isPK();
                boolean isAdmin = sender.hasPermission(EldaniorLogger.ADMIN_PERMISSION);

                // Check limite (3 max pour non-admin)
                if (!isAdmin) {
                    int currentListings = isPK
                            ? ShopManager.getBlackMarketPlayerListingCount(sellerUUID)
                            : ShopManager.getPlayerListingCount(sellerUUID);
                    if (currentListings >= ShopManager.MAX_LISTINGS_PER_PLAYER) {
                        sender.sendMessage(Message.raw("§cLimite atteinte ! Vous avez deja " + ShopManager.MAX_LISTINGS_PER_PLAYER + " objets en vente."));
                        return;
                    }
                }

                // Retirer l'item de la hotbar
                byte activeSlot = inventory.getActiveHotbarSlot();
                inventory.getHotbar().removeItemStackFromSlot(activeSlot);

                // Ajouter au bon marche
                if (isPK) {
                    ShopManager.addBlackMarketListing(sellerUUID, sender.getDisplayName(), item, price);
                } else {
                    ShopManager.addListing(sellerUUID, sender.getDisplayName(), item, price);
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

                sender.sendMessage(Message.raw("§a" + itemName + " §amis en vente pour §e" + price + " Or §a!"));
                sender.sendMessage(Message.raw("§7Les joueurs peuvent l'acheter via §f/es system §7> Shop"));

            } catch (Exception e) {
                sender.sendMessage(Message.raw("§cErreur: " + e.getMessage()));
                e.printStackTrace();
            }
        }, sender.getWorld());
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
