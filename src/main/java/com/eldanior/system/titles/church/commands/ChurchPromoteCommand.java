package com.eldanior.system.titles.church.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.church.ChurchManager;
import com.eldanior.system.titles.church.ChurchRank;
import com.hypixel.hytale.component.ComponentType;
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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ChurchPromoteCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> rankArg;

    public ChurchPromoteCommand() {
        super("churchpromote", "Promouvoir dans l'Eglise (pretre/archeveque/cardinal)");
        this.playerArg = this.withRequiredArg("joueur", "Joueur cible", ArgTypes.STRING);
        this.rankArg = this.withRequiredArg("rang", "pretre|archeveque|cardinal", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return true; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        String targetName = this.playerArg.get(ctx);
        String rankStr = this.rankArg.get(ctx);

        ChurchRank newRank = ChurchRank.fromString(rankStr);
        if (newRank == null || newRank == ChurchRank.PAPE || newRank == ChurchRank.LAIQUE
                || newRank == ChurchRank.RELIGIEUX || newRank == ChurchRank.SAINT) {
            sender.sendMessage(Message.raw("§cRang invalide. Utilisez : pretre, archeveque, cardinal"));
            return CompletableFuture.completedFuture(null);
        }

        assert sender.getWorld() != null;
        return CompletableFuture.runAsync(() -> {
            try {
                UUID senderUUID = getSenderUUID(sender);
                if (senderUUID == null || !senderUUID.equals(ChurchManager.getCurrentPopeUUID())) {
                    if (!sender.hasPermission("eldanior.command.church.promote")) {
                        sender.sendMessage(Message.raw("§cSeul le Pape ou un Admin peut promouvoir."));
                        return;
                    }
                }

                if (!ChurchManager.canPopePromote(newRank)) {
                    sender.sendMessage(Message.raw("§cPlus de places pour " + newRank.getFormattedName()
                            + " §c(" + ChurchManager.getRemainingSlots(newRank) + "/" + newRank.getMaxPerChurch() + ")"));
                    return;
                }

                PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
                if (targetRef == null) { sender.sendMessage(Message.raw("§cJoueur introuvable.")); return; }

                UUID targetUUID = extractUUID(targetRef);
                PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
                if (targetPlayer == null) { sender.sendMessage(Message.raw("§cLe joueur doit etre connecte.")); return; }

                var ref = targetPlayer.getReference();
                if (ref == null) return;
                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) data = new PlayerLevelData();

                PlayerLevelData copy = (PlayerLevelData) data.clone();
                if (copy == null) return;
                copy.setChurchRank(newRank.name());
                copy.setFaith(newRank.getBaseFaith());
                store.putComponent(ref, type, copy);

                ChurchManager.recordPopePromotion(newRank);

                sender.sendMessage(Message.raw("§a" + targetName + " promu au rang de " + newRank.getFormattedName()));
                targetPlayer.sendMessage(Message.raw("§eVous avez ete promu au rang de " + newRank.getFormattedName() + " §e!"));
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

    private UUID extractUUID(PlayerRef playerRef) throws Exception {
        Field uuidField = PlayerRef.class.getDeclaredField("uuid");
        uuidField.setAccessible(true);
        return (UUID) uuidField.get(playerRef);
    }

    private UUID getSenderUUID(Player sender) throws Exception {
        var ref = sender.getReference();
        if (ref == null) return null;
        Store<EntityStore> store = ref.getStore();
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        return extractUUID(pRef);
    }
}