package com.eldanior.system.luo;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.guards.LuoNpcs;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * /incliner [--profond true] : le joueur s'incline (salut du Clan Luo, ouvert a tous). Geste Inclinaison ou InclinaisonProfonde
 * du jeu d'animations Luo_Gestes.
 */
public class InclinerCommand extends AbstractAsyncCommand {

    private final OptionalArg<Boolean> deepArg;

    public InclinerCommand() {
        super("incliner", "S'incliner (salut du Clan Luo)");
        requireNoPermission();                                   // commande joueur : ouverte a tous
        this.deepArg = this.withOptionalArg("profond", "true : inclinaison profonde", ArgTypes.BOOLEAN);
    }


    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        Ref<EntityStore> ref = ctx.senderAsPlayerRef();
        if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);
        boolean deep = Boolean.TRUE.equals(this.deepArg.get(ctx));
        world.execute(() -> {
            try {
                if (ref.isValid())
                    AnimationUtils.playAnimation(ref, AnimationSlot.Action, LuoNpcs.GESTURES, deep ? "InclinaisonProfonde" : "Inclinaison", true, store);
            } catch (Exception e) {
                EldaniorLogger.error("[Luo] /incliner", e);
            }
        });
        return CompletableFuture.completedFuture(null);
    }
}
