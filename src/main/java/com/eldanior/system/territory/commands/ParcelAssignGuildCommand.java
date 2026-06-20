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
 * /es passignguild <guilde> — Assigner une ville a une guilde (admin)
 */
public class ParcelAssignGuildCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> guildArg;

    public ParcelAssignGuildCommand() {
        super("passignguild", "Assigner une ville a une guilde (admin)");
        this.guildArg = this.withRequiredArg("guilde", "ID ou nom de la guilde", ArgTypes.STRING);
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

        String capturedGuild = this.guildArg.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = senderStore.getComponent(senderEntityRef, PlayerRef.getComponentType());
                Player sender = senderStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                UUID senderUUID = UUIDExtractor.getUUID(senderRef);
                if (senderUUID == null) return;
                boolean isAdmin = senderRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION);

                if (!isAdmin) {
                    sender.getPlayerRef().sendMessage(Message.raw("§cCommande admin uniquement."));
                    return;
                }

                ParcelData parcel = getParcelAtPlayer(sender);
                if (parcel == null) {
                    sender.getPlayerRef().sendMessage(Message.raw("§cVous n'etes dans aucune parcelle."));
                    return;
                }

                if (parcel.getType() != ParcelType.CITY) {
                    sender.getPlayerRef().sendMessage(Message.raw("§cSeules les villes peuvent etre assignees a une guilde."));
                    return;
                }

                parcel.setGuildId(capturedGuild);
                ParcelManager.save();
                sender.getPlayerRef().sendMessage(Message.raw("§aVille §f" + parcel.getName() + " §aassignee a la guilde §f" + capturedGuild));
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
