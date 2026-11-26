package com.eldanior.system.familiers;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * /es familier info : etat de son familier et du malus de deuil, ou qu'on soit (son oeuf peut etre loin).
 * Tout le reste se gere depuis l'oeuf (FamilierPage, 2026-11-13 : demande de l'utilisateur, pas de commandes).
 */
public class FamilierCommand extends AbstractCommandCollection {

    public FamilierCommand() {
        super("familier", "Votre familier : info");
        requireNoPermission();
        this.addSubCommand(new Info());
    }

    private static final class Info extends AbstractAsyncCommand {
        Info() { super("info", "Etat de votre familier"); requireNoPermission(); }

        @Nonnull
        @Override
        public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
            Ref<EntityStore> ref = ctx.senderAsPlayerRef();
            if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            if (world == null) return CompletableFuture.completedFuture(null);
            world.execute(() -> {
                try {
                    PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
                    if (pr == null) return;
                    long now = System.currentTimeMillis();
                    Familier f = FamilierManager.of(pr.getUuid());
                    if (f == null) FamilierManager.say(pr, "Vous n'avez pas de familier. Le Dresseur de bêtes vend des œufs.", "#a9b8c8");
                    else {
                        FamilierManager.say(pr, FamilierManager.status(f, now), "#bfe8ff");
                        FamilierManager.say(pr, FamilierManager.whereabouts(f, now)
                                + (f.hasEgg() ? " · œuf en " + f.eggCoords() + " (" + f.eggWorld + ")" : "") + ". Gérez-le depuis son œuf.", "#a9b8c8");
                    }
                    long malus = FamilierManager.malusUntil(pr.getUuid());
                    if (malus > now && !FamilierManager.isAdmin(pr.getUuid()))
                        FamilierManager.say(pr, "Deuil : -20 % d'expérience encore " + FamilierManager.duration(malus - now) + ".", "#e8a060");
                } catch (Exception e) {
                    EldaniorLogger.error("FamilierCommand", e);
                }
            });
            return CompletableFuture.completedFuture(null);
        }
    }
}
