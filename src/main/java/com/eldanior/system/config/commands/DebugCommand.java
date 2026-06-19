package com.eldanior.system.config.commands;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * Commande admin : /es debug
 * Active ou desactive le mode debug (toggle).
 * Quand actif, EldaniorLogger.debug() affiche les messages dans la console.
 */
public class DebugCommand extends AbstractAsyncCommand {

    public DebugCommand() {
        super("debug", "Active/desactive le mode debug (admin)");
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
                if (playerRef == null) return;

                if (!playerRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
                    playerRef.sendMessage(Message.raw("§cVous n'avez pas la permission."));
                    return;
                }

                boolean newState = !EldaniorLogger.isDebugMode();
                EldaniorLogger.setDebugMode(newState);

                if (newState) {
                    playerRef.sendMessage(Message.raw("§aMode debug ACTIVE. Les messages debug sont affiches dans la console."));
                } else {
                    playerRef.sendMessage(Message.raw("§7Mode debug DESACTIVE."));
                }
            } catch (Exception e) {
                EldaniorLogger.error("DebugCommand", e);
            }
        }, world);
    }
}
