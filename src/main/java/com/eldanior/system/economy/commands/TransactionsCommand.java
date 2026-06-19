package com.eldanior.system.economy.commands;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.economy.TransactionLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Commande admin : /es transactions [N]
 * Affiche les N dernieres lignes du log de transactions (defaut: 20).
 */
public class TransactionsCommand extends AbstractAsyncCommand {

    private final OptionalArg<Integer> countArg;

    public TransactionsCommand() {
        super("transactions", "Affiche les dernieres transactions economiques (admin)");
        this.countArg = this.withOptionalArg("count", "Nombre de lignes (defaut 20)", ArgTypes.INTEGER);
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

        Integer countVal = this.countArg.get(ctx);
        int count = (countVal != null) ? Math.max(1, Math.min(countVal, 100)) : 20;

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef == null) return;

                // Admin only
                if (!playerRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
                    playerRef.sendMessage(Message.raw("§cVous n'avez pas la permission."));
                    return;
                }

                List<String> lines = TransactionLogger.getRecentLines(count);
                if (lines.isEmpty()) {
                    playerRef.sendMessage(Message.raw("§7Aucune transaction enregistree."));
                    return;
                }

                playerRef.sendMessage(Message.raw("§6=== Transactions recentes (" + lines.size() + ") ==="));
                for (String line : lines) {
                    playerRef.sendMessage(Message.raw("§7" + line));
                }
                playerRef.sendMessage(Message.raw("§6=== Fin ==="));
            } catch (Exception e) {
                EldaniorLogger.error("TransactionsCommand", e);
            }
        }, world);
    }
}
