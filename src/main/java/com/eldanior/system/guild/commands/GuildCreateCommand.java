package com.eldanior.system.guild.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.guild.Guild;
import com.eldanior.system.guild.GuildManager;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GuildCreateCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> nameArg;
    private final RequiredArg<String> tagArg;

    public GuildCreateCommand() {
        super("guildcreate", "Creer une guilde (level 120 + 150000 or)");
        this.nameArg = this.withRequiredArg("nom", "Nom de la guilde", ArgTypes.STRING);
        this.tagArg = this.withRequiredArg("tag", "Tag court (3-5 lettres)", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        String name = this.nameArg.get(ctx);
        String tag = this.tagArg.get(ctx).toUpperCase();

        if (tag.length() < 2 || tag.length() > 5) {
            sender.sendMessage(Message.raw("§cLe tag doit faire entre 2 et 5 caracteres."));
            return CompletableFuture.completedFuture(null);
        }

        if (GuildManager.guildExists(name)) {
            sender.sendMessage(Message.raw("§cUne guilde avec ce nom existe deja."));
            return CompletableFuture.completedFuture(null);
        }

        if (GuildManager.tagExists(tag)) {
            sender.sendMessage(Message.raw("§cCe tag est deja utilise."));
            return CompletableFuture.completedFuture(null);
        }

        assert sender.getWorld() != null;
        return CompletableFuture.runAsync(() -> {
            try {
                var ref = sender.getReference();
                if (ref == null) return;
                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) return;

                // Verifications
                if (data.getLevel() < 120) {
                    sender.sendMessage(Message.raw("§cNiveau minimum requis : 120 (vous etes niveau " + data.getLevel() + ")"));
                    return;
                }
                if (data.getMoney() < 150000) {
                    sender.sendMessage(Message.raw("§cArgent minimum requis : 150,000 (vous avez " + data.getMoney() + ")"));
                    return;
                }
                if (!data.canJoinGuild()) {
                    if (data.hasGuild()) {
                        sender.sendMessage(Message.raw("§cVous etes deja dans une guilde."));
                    } else {
                        sender.sendMessage(Message.raw("§cVous faites partie d'une famille noble. Les nobles ne peuvent pas creer de guilde."));
                    }
                    return;
                }

                UUID senderUUID = getSenderUUID(sender);

                // Deduire l'argent
                PlayerLevelData copy = (PlayerLevelData) data.clone();
                if (copy == null) return;
                copy.removeMoney(150000);
                copy.setGuildRole("CHEF");

                // Creer la guilde
                Guild guild = GuildManager.createGuild(name, tag, senderUUID, sender.getDisplayName());
                if (guild == null) {
                    sender.sendMessage(Message.raw("§cNom ou tag invalide (3-24 chars, tag 2-5 lettres, pas de doublon)."));
                    return;
                }
                copy.setGuildId(guild.getId());
                store.putComponent(ref, type, copy);

                sender.sendMessage(Message.raw("§a§lGuilde " + guild.getFormattedName() + " " + guild.getFormattedTag() + " §a§lcreee !"));
                sender.sendMessage(Message.raw("§7Vous etes le Chef. -150,000 or."));
                sender.sendMessage(Message.raw("§7Invitez des membres avec §f/es guild invite <joueur>"));
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

    private UUID getSenderUUID(Player sender) throws Exception {
        var ref = sender.getReference();
        if (ref == null) return null;
        Store<EntityStore> store = ref.getStore();
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        return UUIDExtractor.getUUID(pRef);
    }
}