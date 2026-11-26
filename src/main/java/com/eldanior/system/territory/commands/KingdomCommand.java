package com.eldanior.system.territory.commands;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.systems.KingdomBarrierSystem;
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

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * /es royaume barriere on|off (OP) : mur infranchissable (Barrier + particules d'energie) autour du royaume ou
 * vous vous trouvez. Il se pose / se retire progressivement autour des joueurs qui approchent de la frontiere.
 */
public class KingdomCommand extends AbstractCommandCollection {

    public KingdomCommand() {
        super("royaume", "Royaume (OP) : barriere on|off");
        this.addSubCommand(new Barrier());
    }

    protected boolean canGeneratePermission() { return false; }

    private static final class Barrier extends AbstractAsyncCommand {
        private final RequiredArg<String> stateArg;

        Barrier() {
            super("barriere", "Active (on) ou retire (off) le mur du royaume ou vous etes");
            this.stateArg = this.withRequiredArg("etat", "on ou off", ArgTypes.STRING);
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
            String state = this.stateArg.get(ctx).toLowerCase();

            world.execute(() -> {
                try {
                    PlayerRef sender = store.getComponent(ref, PlayerRef.getComponentType());
                    if (sender == null) return;
                    if (!sender.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) { sender.sendMessage(Message.raw("Permission refusee.")); return; }
                    if (!state.equals("on") && !state.equals("off")) { sender.sendMessage(Message.raw("Usage : /es royaume barriere on|off")); return; }
                    TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
                    if (t == null) return;

                    ParcelData kingdom = KingdomBarrierSystem.kingdomAt(world.getName(),
                            (int) Math.floor(t.getPosition().x), (int) Math.floor(t.getPosition().z));
                    if (kingdom == null) { sender.sendMessage(Message.raw("Vous n'etes dans aucun royaume.")); return; }

                    boolean on = state.equals("on");
                    KingdomBarrierSystem.setEnabled(kingdom.getId(), on);
                    sender.sendMessage(Message.raw(on
                            ? "Mur active pour le royaume " + kingdom.getName() + ". Il se construit autour des joueurs qui approchent de la frontiere."
                            : "Mur desactive pour le royaume " + kingdom.getName() + ". Il est retire au passage des joueurs pres de la frontiere."));
                } catch (Exception e) {
                    EldaniorLogger.error("KingdomCommand", e);
                }
            });
            return CompletableFuture.completedFuture(null);
        }
    }
}
