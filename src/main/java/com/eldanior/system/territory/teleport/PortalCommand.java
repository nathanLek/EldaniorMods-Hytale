package com.eldanior.system.territory.teleport;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.territory.ParcelData;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/** /es portail info|retirer (admin, 2026-11-19) : teleporteur du Marquisat ou se tient l'admin. */
public class PortalCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;

    public PortalCommand() {
        super("portail", "Téléporteur Continental du Marquisat où vous êtes (admin) : info|retirer");
        this.actionArg = this.withRequiredArg("action", "info ou retirer", ArgTypes.STRING);
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
        world.execute(() -> {
            try {
                PlayerRef sender = store.getComponent(ref, PlayerRef.getComponentType());
                if (sender == null) return;
                if (!sender.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) { sender.sendMessage(Message.raw("Permission refusée.")); return; }
                TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
                ParcelData m = t != null ? ContinentalTeleporters.marquisatAt(world.getName(), t.getPosition().x, t.getPosition().y, t.getPosition().z) : null;
                if (m == null) { sender.sendMessage(Message.raw("Placez-vous dans un Marquisat.")); return; }
                var s = ContinentalTeleporters.spot(m);
                String out = switch (action) {
                    case "retirer" -> ContinentalGates.unregister(m, world);
                    case "info" -> s == null ? m.getName() + " : aucun téléporteur."
                            : m.getName() + " : " + (s.gate() ? "Portail" : "bloc") + " en " + s.x() + ", " + s.y() + ", " + s.z() + " ; "
                            + (ContinentalTeleporters.active(m) ? "ouvert" : "fermé") + ", " + ContinentalTeleporters.price(m) + " or le voyage.";
                    default -> "Usage : /es portail info|retirer";
                };
                sender.sendMessage(Message.raw(out));
            } catch (Exception e) {
                EldaniorLogger.error("PortalCommand", e);
            }
        });
        return CompletableFuture.completedFuture(null);
    }
}
