package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * /es garde poser <ville|royal|chevalier|patrouilleur|mage|errant|villageois|aventurier> : PNJ de poste a votre position.
 * /es garde retirer : retire le poste le plus proche (6 blocs) et son garde.
 * /es garde liste : postes et etat des gardes.
 * (Sous-commandes : un argument optionnel de commande Hytale s'ecrit --nom valeur, pas en position.)
 */
public class GuardCommand extends AbstractCommandCollection {

    public GuardCommand() {
        super("garde", "Gardes de zone (OP) : poser|retirer|liste");
        this.addSubCommand(new Place());
        this.addSubCommand(new Remove());
        this.addSubCommand(new ListPosts());
    }

    protected boolean canGeneratePermission() { return false; }

    /** Contexte d'une sous-commande, execute dans le thread du monde du joueur (admin seulement). */
    private interface Action {
        void run(PlayerRef sender, World world, Vector3d pos, float yaw);
    }

    private static CompletableFuture<Void> onWorld(CommandContext ctx, Action action) {
        Ref<EntityStore> ref = ctx.senderAsPlayerRef();
        if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);
        world.execute(() -> {
            try {
                PlayerRef sender = store.getComponent(ref, PlayerRef.getComponentType());
                if (sender == null) return;
                if (!sender.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) { sender.sendMessage(Message.raw("Permission refusee.")); return; }
                TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
                if (t == null) return;
                action.run(sender, world, new Vector3d(t.getPosition()), t.getRotation().yaw());
            } catch (Exception e) {
                EldaniorLogger.error("GuardCommand", e);
            }
        });
        return CompletableFuture.completedFuture(null);
    }

    private static final class Place extends AbstractAsyncCommand {
        private final RequiredArg<String> rankArg;

        Place() {
            super("poser", "Poste un PNJ ici : " + GuardType.args());
            this.rankArg = this.withRequiredArg("rang", GuardType.args(), ArgTypes.STRING);
        }

        protected boolean canGeneratePermission() { return false; }

        @Nonnull
        @Override
        public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
            String rank = this.rankArg.get(ctx);
            return onWorld(ctx, (sender, world, pos, yaw) -> {
                GuardType type = GuardType.fromArg(rank);
                if (type == null) { sender.sendMessage(Message.raw("Rang inconnu : " + rank + " (" + GuardType.args().replace("|", ", ") + ")")); return; }
                GuardManager.Post p = GuardManager.place(type, world, pos, yaw);
                sender.sendMessage(Message.raw(type.getLabel() + " poste ici (" + p.getId() + ")."
                        + switch (type) {
                            case PATROUILLEUR -> " Il fait sa ronde autour de ce point.";
                            case CHEVALIER_ERRANT -> " Il erre dans 200 blocs autour de ce point.";
                            case VILLAGEOIS -> " Il se promene dans 30 blocs autour de ce point.";
                            case AVENTURIER -> " Niveau " + p.level + ", recrutable pour " + GuardManager.priceFor(p.level) + " or.";
                            default -> " Il revient toujours a ce poste.";
                        }));
            });
        }
    }

    private static final class Remove extends AbstractAsyncCommand {
        Remove() { super("retirer", "Retire le poste de garde le plus proche (6 blocs)"); }

        protected boolean canGeneratePermission() { return false; }

        @Nonnull
        @Override
        public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
            return onWorld(ctx, (sender, world, pos, yaw) -> {
                GuardManager.Post p = GuardManager.removeNearest(world, pos);
                sender.sendMessage(Message.raw(p != null ? p.getType().getLabel() + " retire (" + p.getId() + ")."
                        : "Aucun poste de garde a moins de 6 blocs."));
            });
        }
    }

    private static final class ListPosts extends AbstractAsyncCommand {
        ListPosts() { super("liste", "Postes de garde et etat des gardes"); }

        protected boolean canGeneratePermission() { return false; }

        @Nonnull
        @Override
        public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
            return onWorld(ctx, (sender, world, pos, yaw) -> {
                if (GuardManager.getPosts().isEmpty()) { sender.sendMessage(Message.raw("Aucun poste de garde.")); return; }
                long now = System.currentTimeMillis();
                for (GuardManager.Post p : GuardManager.getPosts()) {
                    Vector3d c = p.position();
                    String state = p.isAlive() ? "en poste" : p.getRespawnAt() > 0
                            ? "tombe, retour dans " + Math.max(0, (p.getRespawnAt() - now) / 1000) + " s" : "a faire apparaitre";
                    sender.sendMessage(Message.raw(p.getId() + " - " + p.getType().getLabel() + " - " + p.getWorld() + " "
                            + (int) c.x + " " + (int) c.y + " " + (int) c.z + " - " + state));
                }
            });
        }
    }
}
