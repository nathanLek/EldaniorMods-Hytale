package com.eldanior.system.titles.nobility.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
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

/**
 * /es status <action> <joueur>
 * Actions: setvice, info
 */
public class NobilityStatusCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;
    private final RequiredArg<String> playerArg;

    public NobilityStatusCommand() {
        super("nstatus", "Gestion des status (setvice/info)");
        this.actionArg = this.withRequiredArg("action", "setvice|info", ArgTypes.STRING);
        this.playerArg = this.withRequiredArg("joueur", "Joueur cible", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return true; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        String action = this.actionArg.get(ctx);

        switch (action.toLowerCase()) {
            case "setvice" -> handleSetVice(sender, ctx);
            case "info" -> handleStatusInfo(sender, ctx);
            default -> sender.sendMessage(Message.raw("§cUsage : /es nstatus <setvice|info> <joueur>"));
        }

        return CompletableFuture.completedFuture(null);
    }

    private void handleSetVice(Player sender, CommandContext ctx) {
        String targetName = this.playerArg.get(ctx);

        assert sender.getWorld() != null;
        CompletableFuture.runAsync(() -> {
            try {
                var senderRef = sender.getReference();
                if (senderRef == null) return;
                Store<EntityStore> senderStore = senderRef.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData senderData = senderStore.getComponent(senderRef, type);
                if (senderData == null) return;

                if (!senderData.isPatriarch()) {
                    sender.sendMessage(Message.raw("§cSeul le Patriarche peut nommer un Vice-Patriarche."));
                    return;
                }

                String familyId = senderData.getNobleFamilyId();

                PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
                if (targetRef == null) { sender.sendMessage(Message.raw("§cJoueur introuvable.")); return; }

                UUID targetUUID = extractUUID(targetRef);
                PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
                if (targetPlayer == null) { sender.sendMessage(Message.raw("§cLe joueur doit etre connecte.")); return; }

                var ref = targetPlayer.getReference();
                if (ref == null) return;
                Store<EntityStore> store = ref.getStore();
                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) return;

                if (!familyId.equals(data.getNobleFamilyId())) {
                    sender.sendMessage(Message.raw("§c" + targetName + " n'est pas dans votre famille."));
                    return;
                }

                PlayerLevelData copy = (PlayerLevelData) data.clone();
                if (copy == null) return;
                copy.setStatus("VICE");
                store.putComponent(ref, type, copy);

                sender.sendMessage(Message.raw("§a" + targetName + " est maintenant Vice-Patriarche."));
                targetPlayer.sendMessage(Message.raw("§eVous etes maintenant Vice-Patriarche !"));
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

    private void handleStatusInfo(Player sender, CommandContext ctx) {
        String targetName = this.playerArg.get(ctx);

        assert sender.getWorld() != null;
        CompletableFuture.runAsync(() -> {
            try {
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
                if (data == null) { sender.sendMessage(Message.raw("§cAucune donnee.")); return; }

                String status = data.getStatus();
                String statusDisplay = "§7Aucun";
                if (status != null && !status.isEmpty()) {
                    statusDisplay = switch (status) {
                        case "PATRIARCH" -> "§6Patriarche";
                        case "VICE" -> "§eVice-Patriarche";
                        case "MEMBER" -> "§7Membre";
                        default -> "§7" + status;
                    };
                }

                sender.sendMessage(Message.raw("§6=== Status de " + targetName + " ==="));
                sender.sendMessage(Message.raw("§7Status : " + statusDisplay));
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

    private UUID extractUUID(PlayerRef playerRef) throws Exception {
        Field uuidField = PlayerRef.class.getDeclaredField("uuid");
        uuidField.setAccessible(true);
        return (UUID) uuidField.get(playerRef);
    }
}