package com.eldanior.system.accueil;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
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
 * /es accueil (2026-11-25), reserve aux admins : apercu de la fenetre d'accueil (refermable, rien n'est applique). Si l'admin
 * lui-meme n'a pas termine son accueil (remis en premiere connexion), c'est le vrai accueil qui se rouvre.
 */
public class WelcomeCommand extends AbstractAsyncCommand {

    public WelcomeCommand() {
        super("accueil", "Aperçu de la fenêtre d'accueil des nouveaux joueurs (admin)");
    }

    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        Ref<EntityStore> ref = ctx.senderAsPlayerRef();
        if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);
        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
                PlayerLevelData d = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
                if (pr == null || d == null) return;
                if (!pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {             // verification OP, comme /es admin
                    pr.sendMessage(Message.raw("Vous n'avez pas la permission d'utiliser cette commande."));
                    return;
                }
                if (d.isOnboardingDone()) Welcome.openPreview(ref, store);
                else Welcome.open(ref, store);
            } catch (Exception e) {
                EldaniorLogger.error("[Accueil] /es accueil", e);
            }
        }, world);
    }
}
