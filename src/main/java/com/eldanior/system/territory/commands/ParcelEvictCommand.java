package com.eldanior.system.territory.commands;

import com.eldanior.system.territory.*;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * /es pevict — Annuler la location de la parcelle ou le joueur se trouve (admin)
 */
public class ParcelEvictCommand extends AbstractAsyncCommand {

    public ParcelEvictCommand() {
        super("pevict", "Annuler la location de la parcelle actuelle (admin)");
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

        return CompletableFuture.runAsync(() -> {
            try {
                var senderRef = senderStore.getComponent(senderEntityRef, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
                Player sender = senderStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                if (!senderRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
                    sender.getPlayerRef().sendMessage(Message.raw("§cCommande admin uniquement."));
                    return;
                }

                ParcelData parcel = getParcelAtPlayer(sender);
                if (parcel == null) {
                    sender.getPlayerRef().sendMessage(Message.raw("§cVous n'etes dans aucune parcelle."));
                    return;
                }

                if (!parcel.isRented()) {
                    sender.getPlayerRef().sendMessage(Message.raw("§cCette parcelle n'est pas louee."));
                    return;
                }

                String name = parcel.getName();
                ParcelManager.quitRental(parcel.getId());
                sender.getPlayerRef().sendMessage(Message.raw("§aLocation annulee pour la parcelle §f" + name + "§a."));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }

    private ParcelData getParcelAtPlayer(Player sender) {
        try {
            var ref = sender.getReference();
            if (ref == null) return null;
            var store = ref.getStore();
            var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
            if (transform == null || sender.getWorld() == null) return null;
            return ParcelManager.getParcelAt(sender.getWorld().getName(), transform.getPosition().x, transform.getPosition().y, transform.getPosition().z);
        } catch (Exception e) { return null; }
    }
}
