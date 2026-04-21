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

public class ChurchCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;
    private final RequiredArg<String> playerArg;

    public ChurchCommand() {
        super("church", "Gestion de l'Eglise (setpope/demote/ordain/info/status)");
        this.actionArg = this.withRequiredArg("action", "setpope|demote|ordain|info|status", ArgTypes.STRING);
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
            case "setpope" -> handleSetPope(sender, ctx);
            case "demote" -> handleDemote(sender, ctx);
            case "ordain" -> handleOrdain(sender, ctx);
            case "info" -> handleInfo(sender, ctx);
            case "status" -> handleStatus(sender);
            default -> sender.sendMessage(Message.raw("§cUsage : /es church <setpope|demote|ordain|info|status> <joueur>"));
        }

        return CompletableFuture.completedFuture(null);
    }

    // ==================== SET POPE (Admin only) ====================
    private void handleSetPope(Player sender, CommandContext ctx) {
        if (!sender.hasPermission("eldanior.command.church.setpope")) {
            sender.sendMessage(Message.raw("§cErreur : Pas de permission (Admin requis)."));
            return;
        }

        String targetName = this.playerArg.get(ctx);
        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) { sender.sendMessage(Message.raw("§cJoueur introuvable.")); return; }

        assert sender.getWorld() != null;
        CompletableFuture.runAsync(() -> {
            try {
                UUID targetUUID = extractUUID(targetRef);
                PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
                if (targetPlayer == null) { sender.sendMessage(Message.raw("§cLe joueur doit etre connecte.")); return; }

                var ref = targetPlayer.getReference();
                if (ref == null) return;
                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

                // Retrograder l'ancien pape en Cardinal
                UUID oldPopeUUID = ChurchManager.getCurrentPopeUUID();
                if (oldPopeUUID != null && !oldPopeUUID.equals(targetUUID)) {
                    PlayerRef oldPopeRef = Universe.get().getPlayer(oldPopeUUID);
                    if (oldPopeRef != null) {
                        var oldRef = oldPopeRef.getReference();
                        if (oldRef != null) {
                            Store<EntityStore> oldStore = oldRef.getStore();
                            PlayerLevelData oldData = oldStore.getComponent(oldRef, type);
                            if (oldData != null) {
                                PlayerLevelData oldCopy = (PlayerLevelData) oldData.clone();
                                if (oldCopy != null) {
                                    oldCopy.setChurchRank(ChurchRank.CARDINAL.name());
                                    oldCopy.setFaith(ChurchRank.CARDINAL.getBaseFaith());
                                    oldStore.putComponent(oldRef, type, oldCopy);
                                    oldPopeRef.sendMessage(Message.raw("§eVous avez ete retrogade au rang de §5Cardinal§e."));
                                }
                            }
                        }
                    }
                }

                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) data = new PlayerLevelData();
                PlayerLevelData copy = (PlayerLevelData) data.clone();
                if (copy == null) return;

                copy.setChurchRank(ChurchRank.PAPE.name());
                copy.setFaith(ChurchRank.PAPE.getBaseFaith());
                store.putComponent(ref, type, copy);

                ChurchManager.setPope(targetUUID, targetName);

                sender.sendMessage(Message.raw("§a" + targetName + " est maintenant " + ChurchRank.PAPE.getFormattedName() + " §a!"));
                targetPlayer.sendMessage(Message.raw("§6§lVous etes desormais le " + ChurchRank.PAPE.getFormattedName() + " §6§l!"));
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

    // ==================== DEMOTE ====================
    private void handleDemote(Player sender, CommandContext ctx) {
        if (!sender.hasPermission("eldanior.command.church.demote")) {
            UUID senderUUID;
            try { senderUUID = getSenderUUID(sender); } catch (Exception e) { return; }
            if (senderUUID == null || !senderUUID.equals(ChurchManager.getCurrentPopeUUID())) {
                sender.sendMessage(Message.raw("§cSeul le Pape ou un Admin peut retrograder.")); return;
            }
        }

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
                if (data == null) return;

                ChurchRank currentRank = ChurchRank.fromString(data.getChurchRank());
                if (currentRank == null || currentRank == ChurchRank.LAIQUE) {
                    sender.sendMessage(Message.raw("§cCe joueur est deja Laique.")); return;
                }

                if (currentRank == ChurchRank.RELIGIEUX) ChurchManager.removeAcolyte(targetUUID);

                ChurchRank newRank = currentRank.previous();
                // Skip SAINT en demote (passer directement de Cardinal a Archeveque au-dessus)
                PlayerLevelData copy = (PlayerLevelData) data.clone();
                if (copy == null) return;

                copy.setChurchRank(newRank.name());
                copy.setFaith(newRank.getBaseFaith());
                store.putComponent(ref, type, copy);

                sender.sendMessage(Message.raw("§a" + targetName + " retrogade a " + newRank.getFormattedName()));
                targetPlayer.sendMessage(Message.raw("§cVous avez ete retrogade a " + newRank.getFormattedName()));
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

    // ==================== ORDAIN (Ordonner un Religieux) ====================
    private void handleOrdain(Player sender, CommandContext ctx) {
        String targetName = this.playerArg.get(ctx);

        assert sender.getWorld() != null;
        CompletableFuture.runAsync(() -> {
            try {
                UUID senderUUID = getSenderUUID(sender);
                var senderRef = sender.getReference();
                if (senderRef == null) return;
                Store<EntityStore> senderStore = senderRef.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData senderData = senderStore.getComponent(senderRef, type);
                if (senderData == null) return;

                ChurchRank senderRank = ChurchRank.fromString(senderData.getChurchRank());
                if (senderRank == null || !senderRank.isClergy() || senderRank == ChurchRank.RELIGIEUX) {
                    sender.sendMessage(Message.raw("§cVous devez etre au moins Pretre pour ordonner.")); return;
                }
                if (!ChurchManager.canPromoteAcolyte(senderUUID, senderRank)) {
                    sender.sendMessage(Message.raw("§cLimite d'acolytes atteinte (" + senderRank.getMaxAcolytes() + " max).")); return;
                }

                PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
                if (targetRef == null) { sender.sendMessage(Message.raw("§cJoueur introuvable.")); return; }

                UUID targetUUID = extractUUID(targetRef);
                PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
                if (targetPlayer == null) { sender.sendMessage(Message.raw("§cLe joueur doit etre connecte.")); return; }

                var ref = targetPlayer.getReference();
                if (ref == null) return;
                Store<EntityStore> store = ref.getStore();
                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) data = new PlayerLevelData();

                ChurchRank targetRank = ChurchRank.fromString(data.getChurchRank());
                if (targetRank != null && targetRank != ChurchRank.LAIQUE) {
                    sender.sendMessage(Message.raw("§cCe joueur a deja un rang d'eglise.")); return;
                }

                PlayerLevelData copy = (PlayerLevelData) data.clone();
                if (copy == null) return;
                copy.setChurchRank(ChurchRank.RELIGIEUX.name());
                copy.setFaith(ChurchRank.RELIGIEUX.getBaseFaith());
                store.putComponent(ref, type, copy);
                ChurchManager.addAcolyte(senderUUID, targetUUID);

                sender.sendMessage(Message.raw("§a" + targetName + " est maintenant " + ChurchRank.RELIGIEUX.getFormattedName()));
                targetPlayer.sendMessage(Message.raw("§eVous avez ete ordonne " + ChurchRank.RELIGIEUX.getFormattedName() + " §epar " + sender.getDisplayName() + " !"));
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

    // ==================== INFO ====================
    private void handleInfo(Player sender, CommandContext ctx) {
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

                ChurchRank rank = ChurchRank.fromString(data.getChurchRank());
                if (rank == null) rank = ChurchRank.LAIQUE;

                sender.sendMessage(Message.raw("§6=== Eglise - " + targetName + " ==="));
                sender.sendMessage(Message.raw("§7Rang : " + rank.getFormattedName()));
                sender.sendMessage(Message.raw("§7Foi : §e" + data.getFaith()));

                if (rank == ChurchRank.RELIGIEUX) {
                    UUID master = ChurchManager.getMasterOf(targetUUID);
                    if (master != null) {
                        PlayerRef masterRef = Universe.get().getPlayer(master);
                        String masterName = (masterRef != null) ? masterRef.getUsername() : "Inconnu";
                        sender.sendMessage(Message.raw("§7Ordonne par : §e" + masterName));
                    }
                }

                if (rank.getMaxAcolytes() > 0 && rank != ChurchRank.RELIGIEUX) {
                    int count = ChurchManager.getAcolytesOf(targetUUID).size();
                    sender.sendMessage(Message.raw("§7Acolytes : §e" + count + "/" + rank.getMaxAcolytes()));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

    // ==================== STATUS ====================
    private void handleStatus(Player sender) {
        String popeName = ChurchManager.getCurrentPopeName();
        if (popeName.isEmpty()) { sender.sendMessage(Message.raw("§7Aucun Pape n'a ete nomme.")); return; }

        sender.sendMessage(Message.raw("§6=== Eglise ==="));
        sender.sendMessage(Message.raw("§6Pape : §f" + popeName));
        for (ChurchRank rank : new ChurchRank[]{ChurchRank.CARDINAL, ChurchRank.ARCHEVEQUE, ChurchRank.PRETRE}) {
            int remaining = ChurchManager.getRemainingSlots(rank);
            sender.sendMessage(Message.raw("§7" + rank.getDisplayName() + " : " + rank.getColorCode() + remaining + "/" + rank.getMaxPerChurch() + " places"));
        }
    }

    // ==================== UTILS ====================
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