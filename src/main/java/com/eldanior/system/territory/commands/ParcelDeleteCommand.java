package com.eldanior.system.territory.commands;

import com.eldanior.system.territory.*;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
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
 * /es pdelete <id_ou_nom> — Supprimer une parcelle
 */
public class ParcelDeleteCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> idOrNameArg;

    public ParcelDeleteCommand() {
        super("pdelete", "Supprimer une parcelle par ID ou nom");
        this.idOrNameArg = this.withRequiredArg("id_ou_nom", "ID ou nom de la parcelle", ArgTypes.STRING);
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

        String capturedIdOrName = this.idOrNameArg.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = senderStore.getComponent(senderEntityRef, PlayerRef.getComponentType());
                Player sender = senderStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                UUID senderUUID = UUIDExtractor.getUUID(senderRef);
                if (senderUUID == null) return;
                boolean isAdmin = senderRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION);

                ParcelData parcel = findParcel(capturedIdOrName);
                if (parcel == null) {
                    sender.getPlayerRef().sendMessage(Message.raw("Parcelle introuvable: " + capturedIdOrName));
                    return;
                }

                if (!isAdmin && !parcel.isOwner(senderUUID)) {
                    sender.getPlayerRef().sendMessage(Message.raw("Vous n'etes pas proprietaire de cette parcelle."));
                    return;
                }

                String name = parcel.getName();
                ParcelManager.deleteParcel(parcel.getId());
                sender.getPlayerRef().sendMessage(Message.raw("Parcelle supprimee : " + name));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }

    private ParcelData findParcel(String idOrName) {
        ParcelData p = ParcelManager.get(idOrName);
        if (p != null) return p;
        for (ParcelData parcel : ParcelManager.getAll()) {
            if (parcel.getName().equalsIgnoreCase(idOrName)) return parcel;
        }
        return null;
    }
}
