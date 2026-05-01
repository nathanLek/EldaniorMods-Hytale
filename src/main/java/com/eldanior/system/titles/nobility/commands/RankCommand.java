package com.eldanior.system.titles.nobility.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.NobilityManager;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
import com.eldanior.system.config.UUIDExtractor;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RankCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;
    private final RequiredArg<String> playerArg;

    public RankCommand() {
        super("rank", "Gestion des rangs (setking/demote/knight/info/status)");
        this.actionArg = this.withRequiredArg("action", "setking|demote|knight|info", ArgTypes.STRING);
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
            case "setking" -> handleSetKing(sender, ctx);
            case "demote" -> handleDemote(sender, ctx);
            case "knight" -> handleKnight(sender, ctx);
            case "info" -> handleInfo(sender, ctx);
            default -> sender.sendMessage(Message.raw("§cUsage : /es rank <setking|demote|knight|info> <joueur>"));
        }

        return CompletableFuture.completedFuture(null);
    }

    private void handleSetKing(Player sender, CommandContext ctx) {
        if (!sender.hasPermission("eldanior.command.rank.setking")) {
            sender.sendMessage(Message.raw("§cErreur : Pas de permission (Admin requis)."));
            return;
        }

        String targetName = this.playerArg.get(ctx);
        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) {
            sender.sendMessage(Message.raw("§cErreur : Joueur '" + targetName + "' introuvable."));
            return;
        }

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

                UUID oldKingUUID = NobilityManager.getCurrentKingUUID();
                if (oldKingUUID != null && !oldKingUUID.equals(targetUUID)) {
                    PlayerRef oldKingRef = Universe.get().getPlayer(oldKingUUID);
                    if (oldKingRef != null) {
                        var oldRef = oldKingRef.getReference();
                        if (oldRef != null) {
                            Store<EntityStore> oldStore = oldRef.getStore();
                            PlayerLevelData oldData = oldStore.getComponent(oldRef, type);
                            if (oldData != null) {
                                PlayerLevelData oldCopy = (PlayerLevelData) oldData.clone();
                                if (oldCopy != null) {
                                    oldCopy.setNobilityRank(NobilityRank.MARQUIS.name());
                                    oldCopy.setDignity(NobilityRank.MARQUIS.getBaseDignity());
                                    oldStore.putComponent(oldRef, type, oldCopy);
                                    oldKingRef.sendMessage(Message.raw("§eVous avez ete retrogade au rang de §6Marquis§e."));
                                }
                            }
                        }
                    }
                }

                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) data = new PlayerLevelData();
                PlayerLevelData copy = (PlayerLevelData) data.clone();
                if (copy == null) return;

                copy.setNobilityRank(NobilityRank.ROI.name());
                copy.setDignity(NobilityRank.ROI.getBaseDignity());
                store.putComponent(ref, type, copy);

                NobilityManager.setKing(targetUUID, targetName);

                sender.sendMessage(Message.raw("§a" + targetName + " est maintenant " + NobilityRank.ROI.getFormattedName() + " §a!"));
                targetPlayer.sendMessage(Message.raw("§6§lVous etes desormais le " + NobilityRank.ROI.getFormattedName() + " §6§l!"));
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

    private void handleDemote(Player sender, CommandContext ctx) {
        if (!sender.hasPermission("eldanior.command.rank.demote")) {
            UUID senderUUID;
            try { senderUUID = getSenderUUID(sender); } catch (Exception e) { return; }
            if (senderUUID == null || !senderUUID.equals(NobilityManager.getCurrentKingUUID())) {
                sender.sendMessage(Message.raw("§cSeul le Roi ou un Admin peut retrograder."));
                return;
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

                NobilityRank currentRank = NobilityRank.fromString(data.getNobilityRank());
                if (currentRank == null || currentRank == NobilityRank.ROTURIER) {
                    sender.sendMessage(Message.raw("§cCe joueur est deja Roturier.")); return;
                }

                if (currentRank == NobilityRank.CHEVALIER) NobilityManager.removeKnight(targetUUID);

                NobilityRank newRank = currentRank.previous();
                PlayerLevelData copy = (PlayerLevelData) data.clone();
                if (copy == null) return;

                copy.setNobilityRank(newRank.name());
                copy.setDignity(newRank.getBaseDignity());
                if (!newRank.isNoble()) {
                    String oldFamilyId = data.getNobleFamilyId();
                    if (oldFamilyId != null && !oldFamilyId.isEmpty() && data.isPatriarch()) {
                        FamilyManager.releaseFamily(oldFamilyId);
                    }
                    copy.setNobleFamilyId("");
                    copy.setStatus("");
                }
                store.putComponent(ref, type, copy);

                sender.sendMessage(Message.raw("§a" + targetName + " retrogade a " + newRank.getFormattedName()));
                targetPlayer.sendMessage(Message.raw("§cVous avez ete retrogade a " + newRank.getFormattedName()));
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

    private void handleKnight(Player sender, CommandContext ctx) {
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

                NobilityRank senderRank = NobilityRank.fromString(senderData.getNobilityRank());
                if (senderRank == null || !senderRank.isNoble() || senderRank == NobilityRank.CHEVALIER) {
                    sender.sendMessage(Message.raw("§cVous devez etre au moins Baron pour adouber.")); return;
                }
                if (!NobilityManager.canPromoteKnight(senderUUID, senderRank)) {
                    sender.sendMessage(Message.raw("§cLimite de Chevaliers atteinte (" + senderRank.getMaxKnights() + " max).")); return;
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

                NobilityRank targetRank = NobilityRank.fromString(data.getNobilityRank());
                if (targetRank != null && targetRank != NobilityRank.ROTURIER) {
                    sender.sendMessage(Message.raw("§cCe joueur a deja un rang.")); return;
                }

                PlayerLevelData copy = (PlayerLevelData) data.clone();
                if (copy == null) return;
                copy.setNobilityRank(NobilityRank.CHEVALIER.name());
                copy.setDignity(NobilityRank.CHEVALIER.getBaseDignity());
                if (senderData.getNobleFamilyId() != null && !senderData.getNobleFamilyId().isEmpty()) {
                    copy.setNobleFamilyId(senderData.getNobleFamilyId());
                    copy.setStatus("MEMBER");
                }
                store.putComponent(ref, type, copy);
                NobilityManager.addKnight(senderUUID, targetUUID);

                sender.sendMessage(Message.raw("§a" + targetName + " est maintenant votre " + NobilityRank.CHEVALIER.getFormattedName()));
                targetPlayer.sendMessage(Message.raw("§eVous avez ete adoube " + NobilityRank.CHEVALIER.getFormattedName() + " §epar " + sender.getDisplayName() + " !"));
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

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

                NobilityRank rank = NobilityRank.fromString(data.getNobilityRank());
                if (rank == null) rank = NobilityRank.ROTURIER;

                sender.sendMessage(Message.raw("§6=== " + targetName + " ==="));
                sender.sendMessage(Message.raw("§7Rang : " + rank.getFormattedName()));
                sender.sendMessage(Message.raw("§7Dignite : §e" + data.getDignity()));

                String status = data.getStatus();
                if (status != null && !status.isEmpty()) {
                    String statusDisplay = switch (status) {
                        case "PATRIARCH" -> "§6Patriarche";
                        case "VICE" -> "§eVice-Patriarche";
                        case "MEMBER" -> "§7Membre";
                        default -> "§7" + status;
                    };
                    sender.sendMessage(Message.raw("§7Status : " + statusDisplay));
                }

                String familyId = data.getNobleFamilyId();
                if (familyId != null && !familyId.isEmpty()) {
                    NobleFamilyModel family = FamilyManager.get(familyId);
                    if (family != null) {
                        sender.sendMessage(Message.raw("§7Famille : " + family.getFormattedName()));
                        sender.sendMessage(Message.raw("§7Devise : §o" + family.getMotto()));
                        sender.sendMessage(Message.raw("§7Competence : §e" + family.getFamilyPassive().getDisplayName()));
                    }
                }

                if (rank == NobilityRank.CHEVALIER) {
                    UUID lord = NobilityManager.getLordOf(targetUUID);
                    if (lord != null) {
                        PlayerRef lordRef = Universe.get().getPlayer(lord);
                        String lordName = (lordRef != null) ? lordRef.getUsername() : "Inconnu";
                        sender.sendMessage(Message.raw("§7Seigneur : §e" + lordName));
                    }
                }

                if (rank.getMaxKnights() > 0 && rank != NobilityRank.CHEVALIER) {
                    int knightCount = NobilityManager.getKnightsOf(targetUUID).size();
                    sender.sendMessage(Message.raw("§7Chevaliers : §e" + knightCount + "/" + rank.getMaxKnights()));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
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
