package com.eldanior.system.guild.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.guild.Guild;
import com.eldanior.system.guild.GuildManager;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ComponentType;
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
        Ref<EntityStore> senderEntityRef = ctx.senderAsPlayerRef();
        if (senderEntityRef == null || !senderEntityRef.isValid()) return CompletableFuture.completedFuture(null);

        Store<EntityStore> senderEntityStore = senderEntityRef.getStore();
        World world = ((EntityStore) senderEntityStore.getExternalData()).getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);

        String name = this.nameArg.get(ctx);
        String tag = this.tagArg.get(ctx).toUpperCase();

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = senderEntityStore.getComponent(senderEntityRef, PlayerRef.getComponentType());
                Player sender = senderEntityStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                if (tag.length() < 2 || tag.length() > 5) {
                    senderRef.sendMessage(Message.raw("Le tag doit faire entre 2 et 5 caracteres."));
                    return;
                }

                if (GuildManager.guildExists(name)) {
                    senderRef.sendMessage(Message.raw("Une guilde avec ce nom existe deja."));
                    return;
                }

                if (GuildManager.tagExists(tag)) {
                    senderRef.sendMessage(Message.raw("Ce tag est deja utilise."));
                    return;
                }

                var ref = sender.getReference();
                if (ref == null) return;
                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) return;

                // Verifications
                if (data.getLevel() < 120) {
                    sender.getPlayerRef().sendMessage(Message.raw("Niveau minimum requis : 120 (vous etes niveau " + data.getLevel() + ")"));
                    return;
                }
                if (data.getMoney() < 150000) {
                    sender.getPlayerRef().sendMessage(Message.raw("Argent minimum requis : 150,000 (vous avez " + data.getMoney() + ")"));
                    return;
                }
                if (!data.canJoinGuild()) {
                    if (data.hasGuild()) {
                        sender.getPlayerRef().sendMessage(Message.raw("Vous etes deja dans une guilde."));
                    } else {
                        sender.getPlayerRef().sendMessage(Message.raw("Vous faites partie d'une famille noble. Les nobles ne peuvent pas creer de guilde."));
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
                Guild guild = GuildManager.createGuild(name, tag, senderUUID, sender.getPlayerRef().getUsername());
                if (guild == null) {
                    sender.getPlayerRef().sendMessage(Message.raw("Nom ou tag invalide (3-24 chars, tag 2-5 lettres, pas de doublon)."));
                    return;
                }
                copy.setGuildId(guild.getId());
                store.putComponent(ref, type, copy);

                // Verifier titres en temps reel apres creation de guilde
                com.eldanior.system.titles.TitleManager.checkAndUnlockTitles(ref, store, copy, sender.getPlayerRef());

                sender.getPlayerRef().sendMessage(Message.raw("Guilde " + guild.getFormattedName() + " " + guild.getFormattedTag() + " creee !"));
                sender.getPlayerRef().sendMessage(Message.raw("Vous etes le Chef. -150,000 or."));
                sender.getPlayerRef().sendMessage(Message.raw("Invitez des membres avec /es guild invite <joueur>"));
            } catch (Exception e) { e.printStackTrace(); }
        }, world);
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
