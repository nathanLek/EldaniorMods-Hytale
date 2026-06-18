package com.eldanior.system.titles.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.TitleManager;
import com.eldanior.system.titles.models.TitleModel;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TitleTwoArgCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;
    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> titleIdArg;

    public TitleTwoArgCommand() {
        super("titleadmin", "Admin titres (grant/remove/reset)");
        this.actionArg = this.withRequiredArg("action", "grant | remove | reset", ArgTypes.STRING);
        this.playerArg = this.withRequiredArg("joueur", "Joueur cible", ArgTypes.STRING);
        this.titleIdArg = this.withRequiredArg("titleId", "ID du titre", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return true; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        Ref<EntityStore> senderEntityRef = ctx.senderAsPlayerRef();
        if (senderEntityRef == null || !senderEntityRef.isValid()) return CompletableFuture.completedFuture(null);

        Store<EntityStore> senderEntityStore = senderEntityRef.getStore();
        World world = ((EntityStore) senderEntityStore.getExternalData()).getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);

        String action = this.actionArg.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = senderEntityStore.getComponent(senderEntityRef, PlayerRef.getComponentType());
                Player sender = senderEntityStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                switch (action.toLowerCase()) {
                    case "grant" -> handleGrant(sender, ctx);
                    case "remove" -> handleRemove(sender, ctx);
                    case "reset" -> handleReset(sender, ctx);
                    default -> senderRef.sendMessage(Message.raw("§cUsage : /es titleadmin <grant|remove|reset> <joueur> <titleId>"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }

    private void handleGrant(Player sender, CommandContext ctx) {
        if (!sender.getPlayerRef().hasPermission("eldanior.command.title.grant")) {
            sender.getPlayerRef().sendMessage(Message.raw("§cErreur : Pas de permission."));
            return;
        }

        String targetName = this.playerArg.get(ctx);
        String titleId = this.titleIdArg.get(ctx);

        TitleModel title = TitleManager.get(titleId.toLowerCase());
        if (title == null) {
            sender.getPlayerRef().sendMessage(Message.raw("§cErreur : Titre '" + titleId + "' inconnu."));
            sender.getPlayerRef().sendMessage(Message.raw("§7Titres disponibles : " + TitleManager.getAvailableIds()));
            return;
        }

        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) {
            sender.getPlayerRef().sendMessage(Message.raw("§cErreur : Joueur '" + targetName + "' introuvable."));
            return;
        }

        try {
            UUID targetUUID = extractUUID(targetRef);

            PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
            if (targetPlayer == null) {
                sender.getPlayerRef().sendMessage(Message.raw("§cErreur : Le joueur doit etre connecte."));
                return;
            }

            var ref = targetPlayer.getReference();
            if (ref == null) return;

            Store<EntityStore> store = ref.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = store.getComponent(ref, type);

            if (data == null) data = new PlayerLevelData();

            data.addTitle(title.getId());
            store.putComponent(ref, type, data);

            sender.getPlayerRef().sendMessage(Message.raw("§aSucces : Titre " + title.getFormattedName() + " §aaccorde a " + targetName));
            targetPlayer.sendMessage(Message.raw("§eNouveau titre debloque : " + title.getFormattedName()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRemove(Player sender, CommandContext ctx) {
        if (!sender.getPlayerRef().hasPermission("eldanior.command.title.remove")) {
            sender.getPlayerRef().sendMessage(Message.raw("§cErreur : Pas de permission."));
            return;
        }

        String targetName = this.playerArg.get(ctx);
        String titleId = this.titleIdArg.get(ctx).toLowerCase();

        if (titleId.equals("novice")) {
            sender.getPlayerRef().sendMessage(Message.raw("§cErreur : Le titre 'novice' ne peut pas etre retire."));
            return;
        }

        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) {
            sender.getPlayerRef().sendMessage(Message.raw("§cErreur : Joueur '" + targetName + "' introuvable."));
            return;
        }

        try {
            UUID targetUUID = extractUUID(targetRef);

            PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
            if (targetPlayer == null) {
                sender.getPlayerRef().sendMessage(Message.raw("§cErreur : Le joueur doit etre connecte."));
                return;
            }

            var ref = targetPlayer.getReference();
            if (ref == null) return;

            Store<EntityStore> store = ref.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = store.getComponent(ref, type);

            if (data == null) data = new PlayerLevelData();

            if (!data.getUnlockedTitles().contains(titleId)) {
                sender.getPlayerRef().sendMessage(Message.raw("§cErreur : " + targetName + " ne possede pas le titre '" + titleId + "'."));
                return;
            }

            data.removeTitle(titleId);
            store.putComponent(ref, type, data);

            sender.getPlayerRef().sendMessage(Message.raw("§aSucces : Titre '" + titleId + "' retire a " + targetName));
            targetPlayer.sendMessage(Message.raw("§cTitre retire : " + titleId));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleReset(Player sender, CommandContext ctx) {
        if (!sender.getPlayerRef().hasPermission("eldanior.command.title.reset")) {
            sender.getPlayerRef().sendMessage(Message.raw("§cErreur : Pas de permission."));
            return;
        }

        String targetName = this.playerArg.get(ctx);

        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) {
            sender.getPlayerRef().sendMessage(Message.raw("§cErreur : Joueur '" + targetName + "' introuvable."));
            return;
        }

        try {
            UUID targetUUID = extractUUID(targetRef);

            PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
            if (targetPlayer == null) {
                sender.getPlayerRef().sendMessage(Message.raw("§cErreur : Le joueur doit etre connecte."));
                return;
            }

            var ref = targetPlayer.getReference();
            if (ref == null) return;

            Store<EntityStore> store = ref.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = store.getComponent(ref, type);

            if (data == null) data = new PlayerLevelData();

            data.resetTitles();
            store.putComponent(ref, type, data);

            sender.getPlayerRef().sendMessage(Message.raw("§aSucces : Titres et kills de " + targetName + " reinitialises."));
            targetPlayer.sendMessage(Message.raw("§cVos titres et statistiques de kills ont ete reinitialises."));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private UUID extractUUID(PlayerRef playerRef) throws Exception {
        return UUIDExtractor.getUUID(playerRef);
    }
}