package com.eldanior.system.skills.commands;

import com.hypixel.hytale.builtin.abilities.interaction.OpenAbilityBenchInteraction;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * Ouvre directement la fenetre des runes (celle du Bench_Abilities) sans avoir a poser/utiliser le bloc.
 */
public class RunesCommand extends AbstractAsyncCommand {

    public RunesCommand() {
        super("runes", "Ouvre la fenetre des competences (runes)");
    }

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
                if (ref.isValid()) OpenAbilityBenchInteraction.openBench(ref, store);
            } catch (Exception e) {
                System.err.println("[Runes] Erreur ouverture fenetre : " + e.getMessage());
                e.printStackTrace();
            }
        }, world);
    }
}
