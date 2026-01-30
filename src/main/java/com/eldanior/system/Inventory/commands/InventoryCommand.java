package com.eldanior.system.Inventory.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Inventory.components.PlayerPersonalChestData;
import com.eldanior.system.Inventory.gui.PersonalChestPage;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class InventoryCommand extends AbstractAsyncCommand {

    public InventoryCommand() {
        super("inventory", "Ouvrir votre coffre personnel");
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) {
            return CompletableFuture.completedFuture(null);
        }

        World world = sender.getWorld();
        if (world == null) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                System.out.println("=== [INVENTORY] CustomUIPage ===");

                Ref<EntityStore> ref = sender.getReference();
                assert ref != null;
                Store<EntityStore> store = ref.getStore();

                ComponentType<EntityStore, PlayerPersonalChestData> type =
                        EldaniorSystem.get().getPlayerPersonalChestDataType();

                PlayerPersonalChestData chestData = store.getComponent(ref, type);
                if (chestData == null) {
                    chestData = new PlayerPersonalChestData();
                    store.putComponent(ref, type, chestData);
                }

                PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

                // Ouvrir la page custom
                assert playerRef != null;
                PersonalChestPage page = new PersonalChestPage(playerRef, chestData);
                sender.getPageManager().openCustomPage(ref, store, page);

                System.out.println("[INVENTORY] Page ouverte!");
                sender.sendMessage(Message.raw("§aCoffre personnel ouvert!"));

            } catch (Exception e) {
                System.err.println("[INVENTORY] EXCEPTION:");
                e.printStackTrace();
                sender.sendMessage(Message.raw("§cErreur: " + e.getMessage()));
            }
        }, world);
    }
}