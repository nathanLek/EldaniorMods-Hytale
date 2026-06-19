package com.eldanior.system.persistence.commands;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.persistence.PersistenceManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * Commande admin : /es backup (save|restore)
 * - save    : force une sauvegarde immediate de toutes les donnees
 * - restore : recharge les donnees depuis les fichiers (sans redemarrer)
 */
public class BackupCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;

    public BackupCommand() {
        super("backup", "Sauvegarde ou restauration manuelle des donnees (admin)");
        this.actionArg = this.withRequiredArg("action", "save ou restore", ArgTypes.STRING);
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

        String action = this.actionArg.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef == null) return;

                if (!playerRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
                    playerRef.sendMessage(Message.raw("§cVous n'avez pas la permission."));
                    return;
                }

                if ("save".equalsIgnoreCase(action)) {
                    long start = System.currentTimeMillis();
                    PersistenceManager.saveAll();
                    long elapsed = System.currentTimeMillis() - start;
                    playerRef.sendMessage(Message.raw("§aSauvegarde manuelle terminee en " + elapsed + "ms."));
                    EldaniorLogger.info("Sauvegarde manuelle declenchee par " + playerRef.getUsername());
                } else if ("restore".equalsIgnoreCase(action)) {
                    long start = System.currentTimeMillis();
                    PersistenceManager.load();
                    long elapsed = System.currentTimeMillis() - start;
                    playerRef.sendMessage(Message.raw("§aRestauration terminee en " + elapsed + "ms."));
                    playerRef.sendMessage(Message.raw("§7Note: les donnees joueurs en memoire restent inchangees jusqu'a reconnexion."));
                    EldaniorLogger.info("Restauration manuelle declenchee par " + playerRef.getUsername());
                } else {
                    playerRef.sendMessage(Message.raw("§cUsage: /es backup <save|restore>"));
                }
            } catch (Exception e) {
                EldaniorLogger.error("BackupCommand", e);
            }
        }, world);
    }
}
