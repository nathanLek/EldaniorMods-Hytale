package com.eldanior.system.territory.laws;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * /es invasion lancer|arreter|liste [vagues] [niveau] [prime] (admin seulement, regle de l'utilisateur du 2026-11-18) : sur le
 * domaine ou se tient l'admin (la ville, sinon le territoire ou le Marquisat). Les monstres n'apparaissent jamais en ville.
 */
public class InvasionCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;
    private final OptionalArg<Integer> wavesArg, levelArg, prizeArg;

    public InvasionCommand() {
        super("invasion", "Invasion de monstres sur le domaine ou vous êtes (admin) : lancer|arreter|liste [vagues] [niveau] [prime]");
        this.actionArg = this.withRequiredArg("action", "lancer, arreter ou liste", ArgTypes.STRING);
        this.wavesArg = this.withOptionalArg("vagues", "Nombre de vagues (1 a 10, defaut 3)", ArgTypes.INTEGER);
        this.levelArg = this.withOptionalArg("niveau", "Niveau des monstres (defaut 50)", ArgTypes.INTEGER);
        this.prizeArg = this.withOptionalArg("prime", "Prime partagee entre les defenseurs, en or (defaut 0)", ArgTypes.INTEGER);
    }

    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        var ref = ctx.senderAsPlayerRef();
        if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);
        var store = ref.getStore();
        var world = store.getExternalData().getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);
        String action = this.actionArg.get(ctx).toLowerCase();
        Integer w = this.wavesArg.get(ctx), l = this.levelArg.get(ctx), p = this.prizeArg.get(ctx);
        world.execute(() -> {
            try {
                PlayerRef sender = store.getComponent(ref, PlayerRef.getComponentType());
                if (sender == null) return;
                if (!sender.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) { sender.sendMessage(Message.raw("Permission refusée.")); return; }
                TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
                Vector3d pos = t != null ? new Vector3d(t.getPosition()) : null;
                var domain = pos != null ? Invasions.domainAt(world.getName(), pos) : null;
                String out = switch (action) {
                    case "lancer", "start" -> Invasions.start(domain, world, w != null ? w : 3, l != null ? l : 50, p != null ? p : 0);
                    case "arreter", "arrêter", "stop" -> Invasions.stop(domain);
                    case "liste", "list" -> {
                        List<String> all = Invasions.list();
                        yield all.isEmpty() ? "Aucune invasion en cours." : "Invasions : " + String.join(" | ", all);
                    }
                    default -> "Usage : /es invasion lancer|arreter|liste [vagues] [niveau] [prime]";
                };
                sender.sendMessage(Message.raw(out));
            } catch (Exception e) {
                EldaniorLogger.error("InvasionCommand", e);
            }
        });
        return CompletableFuture.completedFuture(null);
    }
}
