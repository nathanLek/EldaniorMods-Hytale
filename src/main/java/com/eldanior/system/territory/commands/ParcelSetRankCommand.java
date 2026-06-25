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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * /es psetrank <rang> — Definir le rang d'un donjon (admin)
 */
public class ParcelSetRankCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> rankArg;

    public ParcelSetRankCommand() {
        super("psetrank", "Definir le rang d'un donjon (admin)");
        this.rankArg = this.withRequiredArg("rang", "E|D|C|B|A|S", ArgTypes.STRING);
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

        String capturedRank = this.rankArg.get(ctx);

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
                if (capturedRank == null || capturedRank.isEmpty() || !Set.of("E", "D", "C", "B", "A", "S").contains(capturedRank.toUpperCase())) {
                    sender.getPlayerRef().sendMessage(Message.raw("Usage: /es psetrank <E|D|C|B|A|S>"));
                    return;
                }

                var ref = sender.getReference();
                if (ref == null) return;
                var transform = ref.getStore().getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
                if (transform == null) return;
                Vector3d pos = transform.getPosition();
                ParcelData parcel = ParcelManager.getParcelAt(sender.getWorld().getName(), pos.x, pos.y, pos.z);
                if (parcel == null || parcel.getType() != ParcelType.DUNGEON) {
                    sender.getPlayerRef().sendMessage(Message.raw("Vous devez etre dans un donjon."));
                    return;
                }
                parcel.setDungeonRank(capturedRank.toUpperCase());
                ParcelManager.save();
                sender.getPlayerRef().sendMessage(Message.raw("Rank du donjon " + parcel.getName() + " defini a " + capturedRank.toUpperCase()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }
}
