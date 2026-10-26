package com.eldanior.system.boards;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
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
 * Contrats de monstres (OP) :
 * /es contrat zone <nom> [--niveau N] : zone de contrat a votre position (nom : les _ deviennent des espaces ; niveau 50 par defaut).
 * /es contrat retirer : retire la zone la plus proche (10 blocs) et son contrat du jour.
 * /es contrat liste : zones et contrats du jour.
 * /es contrat relancer : tire de nouveaux contrats pour aujourd'hui.
 */
public class ContractCommand extends AbstractCommandCollection {

    public ContractCommand() {
        super("contrat", "Contrats de monstres (OP) : zone|retirer|liste|relancer");
        this.addSubCommand(new AddZone());
        this.addSubCommand(new Remove());
        this.addSubCommand(new ListAll());
        this.addSubCommand(new Reroll());
    }

    protected boolean canGeneratePermission() { return false; }

    private interface Action {
        void run(PlayerRef sender, World world, Vector3d pos);
    }

    /** Dans le thread du monde du joueur, admin seulement. */
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
                if (t != null) action.run(sender, world, new Vector3d(t.getPosition()));
            } catch (Exception e) {
                EldaniorLogger.error("ContractCommand", e);
            }
        });
        return CompletableFuture.completedFuture(null);
    }

    private static final class AddZone extends AbstractAsyncCommand {
        private final RequiredArg<String> nameArg;
        private final OptionalArg<Integer> levelArg;

        AddZone() {
            super("zone", "Zone de contrat a votre position");
            this.nameArg = this.withRequiredArg("nom", "Nom de la zone (Foret_Noire -> Foret Noire)", ArgTypes.STRING);
            this.levelArg = this.withOptionalArg("niveau", "Niveau de base des monstres (x1,2 pour l'elite)", ArgTypes.INTEGER);
        }

        protected boolean canGeneratePermission() { return false; }

        @Nonnull
        @Override
        public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
            String name = this.nameArg.get(ctx).replace('_', ' ');
            Integer level = this.levelArg.get(ctx);
            return onWorld(ctx, (sender, world, pos) -> {
                MonsterContracts.Zone z = MonsterContracts.addZone(name, world.getName(), pos, level != null ? level : 50);
                sender.sendMessage(Message.raw("Zone de contrat « " + z.name() + " » (niveau " + z.level() + ", élite niveau "
                        + Math.round(z.level() * MonsterContracts.ELITE) + ") posée ici. /es contrat relancer pour tirer les contrats du jour."));
            });
        }
    }

    private static final class Remove extends AbstractAsyncCommand {
        Remove() { super("retirer", "Retire la zone de contrat la plus proche (10 blocs)"); }

        protected boolean canGeneratePermission() { return false; }

        @Nonnull
        @Override
        public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
            return onWorld(ctx, (sender, world, pos) -> {
                MonsterContracts.Zone z = MonsterContracts.removeNearest(world.getName(), pos);
                sender.sendMessage(Message.raw(z != null ? "Zone « " + z.name() + " » retirée." : "Aucune zone de contrat à moins de 10 blocs."));
            });
        }
    }

    private static final class ListAll extends AbstractAsyncCommand {
        ListAll() { super("liste", "Zones et contrats du jour"); }

        protected boolean canGeneratePermission() { return false; }

        @Nonnull
        @Override
        public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
            return onWorld(ctx, (sender, world, pos) -> {
                if (MonsterContracts.getZones().isEmpty()) { sender.sendMessage(Message.raw("Aucune zone de contrat (/es contrat zone <nom>).")); return; }
                for (MonsterContracts.Zone z : MonsterContracts.getZones())
                    sender.sendMessage(Message.raw(z.id() + " - " + z.name() + " (niv. " + z.level() + ") - " + z.world() + " "
                            + (int) z.pos().x + " " + (int) z.pos().y + " " + (int) z.pos().z));
                for (MonsterContracts.Contract c : MonsterContracts.getContracts())
                    sender.sendMessage(Message.raw("Contrat : " + c.getName() + " (" + c.getSpecies() + " niv. " + c.getLevel() + ", "
                            + c.getReward() + " or) - " + (c.getClaimedBy() != null ? "réclamé par " + c.getClaimedBy() : "en cours")));
            });
        }
    }

    private static final class Reroll extends AbstractAsyncCommand {
        Reroll() { super("relancer", "Tire de nouveaux contrats pour aujourd'hui"); }

        protected boolean canGeneratePermission() { return false; }

        @Nonnull
        @Override
        public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
            return onWorld(ctx, (sender, world, pos) -> {
                MonsterContracts.roll();
                sender.sendMessage(Message.raw(MonsterContracts.getContracts().size() + " contrat(s) tiré(s) pour aujourd'hui."));
            });
        }
    }
}
