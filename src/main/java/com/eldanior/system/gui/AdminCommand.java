package com.eldanior.system.gui;

import com.eldanior.system.config.EldaniorLogger;
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

public class AdminCommand extends AbstractAsyncCommand {

    public AdminCommand() {
        super("admin", "Ouvre l'interface d'administration Eldanior (OP uniquement)");
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        Ref<EntityStore> ref = ctx.senderAsPlayerRef();
        if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);

        Store<EntityStore> store = ref.getStore();
        World world = ((EntityStore) store.getExternalData()).getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                Player player = store.getComponent(ref, Player.getComponentType());
                if (playerRef == null || player == null) return;

                // Verification OP
                if (!playerRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
                    playerRef.sendMessage(Message.raw("§cVous n'avez pas la permission d'utiliser cette commande."));
                    return;
                }

                player.getPageManager().openCustomPage(ref, store, new AdminScreen(playerRef));
            } catch (Exception e) {
                EldaniorLogger.error("AdminCommand", e);
            }
        }, world);
    }
}