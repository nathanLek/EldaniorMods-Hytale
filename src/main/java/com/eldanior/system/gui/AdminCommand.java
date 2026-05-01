package com.eldanior.system.gui;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class AdminCommand extends AbstractAsyncCommand {

    public AdminCommand() {
        super("admin", "Ouvre l'interface d'administration Eldanior (OP uniquement)");
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        // Verification OP
        if (!sender.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
            sender.sendMessage(Message.raw("§cVous n'avez pas la permission d'utiliser cette commande."));
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                var ref = sender.getReference();
                if (ref == null) return;
                Store<EntityStore> store = ref.getStore();
                PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef == null) return;

                sender.getPageManager().openCustomPage(ref, store, new AdminScreen(playerRef));
            } catch (Exception e) {
                EldaniorLogger.error("AdminCommand", e);
            }
        }, sender.getWorld());
    }
}
