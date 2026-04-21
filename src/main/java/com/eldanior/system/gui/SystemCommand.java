package com.eldanior.system.gui;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class SystemCommand extends AbstractAsyncCommand {

    public SystemCommand() {
        super("system", "Ouvre l'interface principale Eldanior");
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        assert sender.getWorld() != null;
        return CompletableFuture.runAsync(() -> {
            try {
                var ref = sender.getReference();
                assert ref != null;
                Store<EntityStore> store = ref.getStore();
                PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                assert playerRef != null;

                sender.getPageManager().openCustomPage(ref, store, new SystemScreen(playerRef));
            } catch (Exception e) {
                System.err.println("[System] Erreur GUI : " + e.getMessage());
                e.printStackTrace();
            }
        }, sender.getWorld());
    }
}
