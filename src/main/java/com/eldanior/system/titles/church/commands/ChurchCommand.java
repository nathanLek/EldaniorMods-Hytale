package com.eldanior.system.titles.church.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.church.ChurchManager;
import com.eldanior.system.titles.church.ChurchRank;
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
                    case "setpope" -> handleSetPope(sender, ctx);
                    case "demote" -> handleDemote(sender, ctx);
                    case "ordain" -> handleOrdain(sender, ctx);
                    case "info" -> handleInfo(sender, ctx);
                    case "status" -> handleStatus(sender);
                    default -> senderRef.sendMessage(Message.raw("Usage : /es church <setpope|demote|ordain|info|status> <joueur>"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }

    // ==================== SET POPE (Admin only) ====================
    private void handleSetPope(Player sender, CommandContext ctx) {
        if (!sender.getPlayerRef().hasPermission("eldanior.command.church.setpope")) {
            sender.getPlayerRef().sendMessage(Message.raw("Erreur : Pas de permission (Admin requis)."));
            return;
        }

        String targetName = this.playerArg.get(ctx);
        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) { sender.getPlayerRef().sendMessage(Message.raw("Joueur introuvable.")); return; }

        try {
            UUID targetUUID = extractUUID(targetRef);
            PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
            if (targetPlayer == null) { sender.getPlayerRef().sendMessage(Message.raw("Le joueur doit etre connecte.")); return; }

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
                                oldPopeRef.sendMessage(Message.raw("Vous avez ete retrogade au rang de Cardinal."));
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

            sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " est maintenant " + ChurchRank.PAPE.getFormattedName() + " !"));
            targetPlayer.sendMessage(Message.raw("Vous etes desormais le " + ChurchRank.PAPE.getFormattedName() + " !"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== DEMOTE ====================
    private void handleDemote(Player sender, CommandContext ctx) {
        if (!sender.getPlayerRef().hasPermission("eldanior.command.church.demote")) {
            UUID senderUUID;
            try { senderUUID = getSenderUUID(sender); } catch (Exception e) { return; }
            if (senderUUID == null || !senderUUID.equals(ChurchManager.getCurrentPopeUUID())) {
                sender.getPlayerRef().sendMessage(Message.raw("Seul le Pape ou un Admin peut retrograder.")); return;
            }
        }

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
            if (data == null) return;

            ChurchRank currentRank = ChurchRank.fromString(data.getChurchRank());
            if (currentRank == null || currentRank == ChurchRank.LAIQUE) {
                sender.getPlayerRef().sendMessage(Message.raw("Ce joueur est deja Laique.")); return;
            }

            if (currentRank == ChurchRank.RELIGIEUX) ChurchManager.removeAcolyte(targetUUID);

            // Liberer le slot de promotion du Pape pour les rangs promus (hors Religieux/Pape)
            if (currentRank != ChurchRank.RELIGIEUX && currentRank != ChurchRank.PAPE) {
                ChurchManager.unrecordPopePromotion(currentRank);
            }

            ChurchRank newRank = currentRank.previous();
            // Skip SAINT en demote (rang non-attribuable, uniquement via classe Saint)
            if (newRank == ChurchRank.SAINT) {
                newRank = newRank.previous();
            }
            PlayerLevelData copy = (PlayerLevelData) data.clone();
            if (copy == null) return;

            copy.setChurchRank(newRank.name());
            copy.setFaith(newRank.getBaseFaith());
            store.putComponent(ref, type, copy);

            sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " retrogade a " + newRank.getFormattedName()));
            targetPlayer.sendMessage(Message.raw("Vous avez ete retrogade a " + newRank.getFormattedName()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== ORDAIN (Ordonner un Religieux) ====================
    private void handleOrdain(Player sender, CommandContext ctx) {
        String targetName = this.playerArg.get(ctx);

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
                sender.getPlayerRef().sendMessage(Message.raw("Vous devez etre au moins Pretre pour ordonner.")); return;
            }
            if (!ChurchManager.canPromoteAcolyte(senderUUID, senderRank)) {
                sender.getPlayerRef().sendMessage(Message.raw("Limite d'acolytes atteinte (" + senderRank.getMaxAcolytes() + " max).")); return;
            }

            PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
            if (targetRef == null) { sender.getPlayerRef().sendMessage(Message.raw("Joueur introuvable.")); return; }

            UUID targetUUID = extractUUID(targetRef);
            PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
            if (targetPlayer == null) { sender.getPlayerRef().sendMessage(Message.raw("Le joueur doit etre connecte.")); return; }

            var ref = targetPlayer.getReference();
            if (ref == null) return;
            Store<EntityStore> store = ref.getStore();
            PlayerLevelData data = store.getComponent(ref, type);
            if (data == null) data = new PlayerLevelData();

            ChurchRank targetRank = ChurchRank.fromString(data.getChurchRank());
            if (targetRank != null && targetRank != ChurchRank.LAIQUE) {
                sender.getPlayerRef().sendMessage(Message.raw("Ce joueur a deja un rang d'eglise.")); return;
            }

            PlayerLevelData copy = (PlayerLevelData) data.clone();
            if (copy == null) return;
            copy.setChurchRank(ChurchRank.RELIGIEUX.name());
            copy.setFaith(ChurchRank.RELIGIEUX.getBaseFaith());
            store.putComponent(ref, type, copy);
            ChurchManager.addAcolyte(senderUUID, targetUUID);

            sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " est maintenant " + ChurchRank.RELIGIEUX.getFormattedName()));
            targetPlayer.sendMessage(Message.raw("Vous avez ete ordonne " + ChurchRank.RELIGIEUX.getFormattedName() + " par " + sender.getPlayerRef().getUsername() + " !"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== INFO ====================
    private void handleInfo(Player sender, CommandContext ctx) {
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

            ChurchRank rank = ChurchRank.fromString(data.getChurchRank());
            if (rank == null) rank = ChurchRank.LAIQUE;

            sender.getPlayerRef().sendMessage(Message.raw("=== Eglise - " + targetName + " ==="));
            sender.getPlayerRef().sendMessage(Message.raw("Rang : " + rank.getFormattedName()));
            sender.getPlayerRef().sendMessage(Message.raw("Foi : " + data.getFaith()));

            if (rank == ChurchRank.RELIGIEUX) {
                UUID master = ChurchManager.getMasterOf(targetUUID);
                if (master != null) {
                    PlayerRef masterRef = Universe.get().getPlayer(master);
                    String masterName = (masterRef != null) ? masterRef.getUsername() : "Inconnu";
                    sender.getPlayerRef().sendMessage(Message.raw("Ordonne par : " + masterName));
                }
            }

            if (rank.getMaxAcolytes() > 0 && rank != ChurchRank.RELIGIEUX) {
                int count = ChurchManager.getAcolytesOf(targetUUID).size();
                sender.getPlayerRef().sendMessage(Message.raw("Acolytes : " + count + "/" + rank.getMaxAcolytes()));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== STATUS ====================
    private void handleStatus(Player sender) {
        String popeName = ChurchManager.getCurrentPopeName();
        if (popeName.isEmpty()) { sender.getPlayerRef().sendMessage(Message.raw("Aucun Pape n'a ete nomme.")); return; }

        sender.getPlayerRef().sendMessage(Message.raw("=== Eglise ==="));
        sender.getPlayerRef().sendMessage(Message.raw("Pape : " + popeName));
        for (ChurchRank rank : new ChurchRank[]{ChurchRank.CARDINAL, ChurchRank.ARCHEVEQUE, ChurchRank.PRETRE}) {
            int remaining = ChurchManager.getRemainingSlots(rank);
            sender.getPlayerRef().sendMessage(Message.raw("" + rank.getDisplayName() + " : " + rank.getColorCode() + remaining + "/" + rank.getMaxPerChurch() + " places"));
        }
    }

    // ==================== UTILS ====================
    private UUID extractUUID(PlayerRef playerRef) throws Exception {
        return UUIDExtractor.getUUID(playerRef);
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
