package com.eldanior.system.territory.commands;

import com.eldanior.system.territory.*;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * /es psetregen <secondes> — Definir le delai de regeneration (mine/farm/forest, admin)
 */
public class ParcelSetRegenCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> delayArg;

    public ParcelSetRegenCommand() {
        super("psetregen", "Definir le delai de regeneration (mine/farm/forest, admin)");
        this.delayArg = this.withRequiredArg("secondes", "Delai en secondes", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        Ref<EntityStore> senderEntityRef = ctx.senderAsPlayerRef();
        if (senderEntityRef == null || !senderEntityRef.isValid()) return CompletableFuture.completedFuture(null);

        Store<EntityStore> senderStore = senderEntityRef.getStore();
        World world = ((EntityStore) senderStore.getExternalData()).getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);

        String capturedDelay = this.delayArg.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = senderStore.getComponent(senderEntityRef, PlayerRef.getComponentType());
                Player sender = senderStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                UUID senderUUID = UUIDExtractor.getUUID(senderRef);
                if (senderUUID == null) return;
                boolean isAdmin = senderRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION);

                if (!isAdmin) {
                    sender.getPlayerRef().sendMessage(Message.raw("Commande admin uniquement."));
                    return;
                }

                int delaySec;
                try { delaySec = Integer.parseInt(capturedDelay); }
                catch (NumberFormatException e) {
                    sender.getPlayerRef().sendMessage(Message.raw("Usage: /es psetregen <secondes>"));
                    return;
                }

                var ref = sender.getReference();
                if (ref == null) return;
                var transform = ref.getStore().getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
                if (transform == null) return;
                Vector3d pos = transform.getPosition();
                ParcelData parcel = ParcelManager.getParcelAt(sender.getWorld().getName(), pos.x, pos.y, pos.z);
                if (parcel == null || (parcel.getType() != ParcelType.FARM && parcel.getType() != ParcelType.MINE && parcel.getType() != ParcelType.FOREST)) {
                    sender.getPlayerRef().sendMessage(Message.raw("Vous devez etre dans une mine, farm ou foret."));
                    return;
                }
                parcel.setRegenDelaySec(delaySec);
                ParcelManager.save();
                sender.getPlayerRef().sendMessage(Message.raw("Delai de regeneration de " + parcel.getName() + " defini a " + delaySec + "s"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }
}
