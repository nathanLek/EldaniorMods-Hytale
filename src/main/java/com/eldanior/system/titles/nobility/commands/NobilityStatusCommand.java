package com.eldanior.system.titles.nobility.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
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
                    case "setvice" -> handleSetVice(sender, ctx);
                    case "info" -> handleStatusInfo(sender, ctx);
                    default -> senderRef.sendMessage(Message.raw("Usage : /es nstatus <setvice|info> <joueur>"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }

    private void handleSetVice(Player sender, CommandContext ctx) {
        String targetName = this.playerArg.get(ctx);

        try {
            var senderRef = sender.getReference();
            if (senderRef == null) return;
            Store<EntityStore> senderStore = senderRef.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData senderData = senderStore.getComponent(senderRef, type);
            if (senderData == null) return;

            if (!senderData.isPatriarch()) {
                sender.getPlayerRef().sendMessage(Message.raw("Seul le Patriarche peut nommer un Vice-Patriarche."));
                return;
            }

            String familyId = senderData.getNobleFamilyId();

            PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
            if (targetRef == null) { sender.getPlayerRef().sendMessage(Message.raw("Joueur introuvable.")); return; }

            UUID targetUUID = extractUUID(targetRef);
            PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
            if (targetPlayer == null) { sender.getPlayerRef().sendMessage(Message.raw("Le joueur doit etre connecte.")); return; }

            var ref = targetPlayer.getReference();
            if (ref == null) return;
            Store<EntityStore> store = ref.getStore();
            PlayerLevelData data = store.getComponent(ref, type);
            if (data == null) return;

            if (!familyId.equals(data.getNobleFamilyId())) {
                sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " n'est pas dans votre famille."));
                return;
            }

            PlayerLevelData copy = (PlayerLevelData) data.clone();
            if (copy == null) return;
            copy.setStatus("VICE");
            store.putComponent(ref, type, copy);

            sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " est maintenant Vice-Patriarche."));
            targetPlayer.sendMessage(Message.raw("Vous etes maintenant Vice-Patriarche !"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleStatusInfo(Player sender, CommandContext ctx) {
        String targetName = this.playerArg.get(ctx);

        try {
            PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
            if (targetRef == null) { sender.getPlayerRef().sendMessage(Message.raw("Joueur introuvable.")); return; }

            UUID targetUUID = extractUUID(targetRef);
            PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
            if (targetPlayer == null) { sender.getPlayerRef().sendMessage(Message.raw("Le joueur doit etre connecte.")); return; }

            var ref = targetPlayer.getReference();
            if (ref == null) return;
            Store<EntityStore> store = ref.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = store.getComponent(ref, type);
            if (data == null) { sender.getPlayerRef().sendMessage(Message.raw("Aucune donnee.")); return; }

            String status = data.getStatus();
            String statusDisplay = "Aucun";
            if (status != null && !status.isEmpty()) {
                statusDisplay = switch (status) {
                    case "PATRIARCH" -> "Patriarche";
                    case "VICE" -> "Vice-Patriarche";
                    case "MEMBER" -> "Membre";
                    default -> "" + status;
                };
            }

            sender.getPlayerRef().sendMessage(Message.raw("=== Status de " + targetName + " ==="));
            sender.getPlayerRef().sendMessage(Message.raw("Status : " + statusDisplay));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private UUID extractUUID(PlayerRef playerRef) throws Exception {
        return UUIDExtractor.getUUID(playerRef);
    }
}