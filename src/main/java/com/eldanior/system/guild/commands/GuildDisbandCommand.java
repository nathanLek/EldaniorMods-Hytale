package com.eldanior.system.guild.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.guild.Guild;
import com.eldanior.system.guild.GuildManager;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * /es guilddisband — Dissoudre sa guilde (Chef uniquement)
 */
public class GuildDisbandCommand extends AbstractAsyncCommand {

    public GuildDisbandCommand() {
        super("guilddisband", "Dissoudre votre guilde (Chef uniquement)");
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        assert sender.getWorld() != null;
        return CompletableFuture.runAsync(() -> {
            try {
                var ref = sender.getReference();
                if (ref == null) return;
                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null || !data.isGuildChef()) {
                    sender.sendMessage(Message.raw("§cSeul le Chef peut dissoudre la guilde."));
                    return;
                }

                UUID senderUUID = getSenderUUID(sender);
                Guild guild = GuildManager.getPlayerGuild(senderUUID);
                if (guild == null) { sender.sendMessage(Message.raw("§cVous n'etes dans aucune guilde.")); return; }

                String guildName = guild.getFormattedName();

                // Reset le chef
                PlayerLevelData copy = (PlayerLevelData) data.clone();
                if (copy == null) return;
                copy.setGuildId("");
                copy.setGuildRole("");
                store.putComponent(ref, type, copy);

                // Dissoudre (retire tous les membres du map)
                GuildManager.disbandGuild(guild.getId());

                sender.sendMessage(Message.raw("§cLa guilde " + guildName + " §ca ete dissoute."));
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

    private UUID getSenderUUID(Player sender) throws Exception {
        var ref = sender.getReference();
        if (ref == null) return null;
        Store<EntityStore> store = ref.getStore();
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        Field uuidField = PlayerRef.class.getDeclaredField("uuid");
        uuidField.setAccessible(true);
        return (UUID) uuidField.get(pRef);
    }
}