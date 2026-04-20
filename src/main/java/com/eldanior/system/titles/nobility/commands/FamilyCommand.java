package com.eldanior.system.titles.nobility.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.NobilityManager;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
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
 * /es family <action> <joueur/familyId>
 * Actions: choose, invite, info
 */
public class FamilyCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;
    private final RequiredArg<String> arg1;

    public FamilyCommand() {
        super("family", "Gestion des familles (choose/invite/info)");
        this.actionArg = this.withRequiredArg("action", "choose|invite|info", ArgTypes.STRING);
        this.arg1 = this.withRequiredArg("arg", "FamilyId ou joueur", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return true; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        String action = this.actionArg.get(ctx);

        switch (action.toLowerCase()) {
            case "choose" -> handleChoose(sender, ctx);
            case "invite" -> handleInvite(sender, ctx);
            case "info" -> handleInfo(sender, ctx);
            default -> sender.sendMessage(Message.raw("§cUsage : /es family <choose|invite|info> <familyId/joueur>"));
        }

        return CompletableFuture.completedFuture(null);
    }

    // ==================== CHOOSE ====================
    private void handleChoose(Player sender, CommandContext ctx) {
        String familyId = this.arg1.get(ctx).toLowerCase();

        NobleFamilyModel family = FamilyManager.get(familyId);
        if (family == null) {
            sender.sendMessage(Message.raw("§cFamille '" + familyId + "' inconnue."));
            sender.sendMessage(Message.raw("§7Disponibles : " + FamilyManager.getAvailableIds()));
            return;
        }

        if (FamilyManager.isFamilyTaken(familyId)) {
            sender.sendMessage(Message.raw("§cCette famille est deja prise."));
            return;
        }

        assert sender.getWorld() != null;
        CompletableFuture.runAsync(() -> {
            try {
                var ref = sender.getReference();
                if (ref == null) return;
                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) return;

                NobilityRank rank = NobilityRank.fromString(data.getNobilityRank());
                if (rank == null || (rank != NobilityRank.MARQUIS && rank != NobilityRank.DUC)) {
                    sender.sendMessage(Message.raw("§cSeuls les Marquis et Ducs peuvent choisir une famille."));
                    return;
                }

                if (data.getNobleFamilyId() != null && !data.getNobleFamilyId().isEmpty()) {
                    sender.sendMessage(Message.raw("§cVous appartenez deja a une famille."));
                    return;
                }

                if (family.getMinimumRank() != rank) {
                    sender.sendMessage(Message.raw("§cCette famille est reservee aux " + family.getMinimumRank().getFormattedName() + "§c."));
                    sender.sendMessage(Message.raw("§7Pour votre rang : §e" + FamilyManager.getAvailableFamilyIdsForRank(rank)));
                    return;
                }

                PlayerLevelData copy = (PlayerLevelData) data.clone();
                if (copy == null) return;
                copy.setNobleFamilyId(family.getId());
                copy.setStatus("PATRIARCH");
                store.putComponent(ref, type, copy);

                FamilyManager.claimFamily(family.getId());

                sender.sendMessage(Message.raw("§aVous etes Patriarche de la famille " + family.getFormattedName() + " §a!"));
                sender.sendMessage(Message.raw("§7Devise : §o" + family.getMotto()));
                sender.sendMessage(Message.raw("§7Competence : §e" + family.getFamilyPassive().getDisplayName()));
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

    // ==================== INVITE ====================
    private void handleInvite(Player sender, CommandContext ctx) {
        String targetName = this.arg1.get(ctx);

        assert sender.getWorld() != null;
        CompletableFuture.runAsync(() -> {
            try {
                var senderRef = sender.getReference();
                if (senderRef == null) return;
                Store<EntityStore> senderStore = senderRef.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData senderData = senderStore.getComponent(senderRef, type);
                if (senderData == null) return;

                if (!senderData.canInviteToFamily()) {
                    sender.sendMessage(Message.raw("§cSeul le Patriarche ou le Vice-Patriarche peut inviter."));
                    return;
                }

                String familyId = senderData.getNobleFamilyId();
                if (familyId == null || familyId.isEmpty()) {
                    sender.sendMessage(Message.raw("§cVous n'appartenez a aucune famille."));
                    return;
                }

                NobleFamilyModel family = FamilyManager.get(familyId);
                if (family == null) { sender.sendMessage(Message.raw("§cFamille introuvable.")); return; }

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

                if (data.getNobleFamilyId() != null && !data.getNobleFamilyId().isEmpty()) {
                    sender.sendMessage(Message.raw("§c" + targetName + " appartient deja a une famille."));
                    return;
                }

                NobilityRank targetRank = NobilityRank.fromString(data.getNobilityRank());
                if (targetRank == null || !targetRank.isNoble()) {
                    sender.sendMessage(Message.raw("§c" + targetName + " n'est pas noble."));
                    return;
                }

                PlayerLevelData copy = (PlayerLevelData) data.clone();
                if (copy == null) return;
                copy.setNobleFamilyId(familyId);
                copy.setStatus("MEMBER");
                store.putComponent(ref, type, copy);

                sender.sendMessage(Message.raw("§a" + targetName + " a rejoint " + family.getFormattedName() + " §aen tant que Membre."));
                targetPlayer.sendMessage(Message.raw("§eVous avez rejoint la famille " + family.getFormattedName() + " §7- §o" + family.getMotto()));
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

    // ==================== INFO ====================
    private void handleInfo(Player sender, CommandContext ctx) {
        String familyId = this.arg1.get(ctx).toLowerCase();

        NobleFamilyModel family = FamilyManager.get(familyId);
        if (family == null) {
            sender.sendMessage(Message.raw("§cFamille '" + familyId + "' inconnue."));
            sender.sendMessage(Message.raw("§7Disponibles : " + FamilyManager.getAvailableIds()));
            return;
        }

        sender.sendMessage(Message.raw("§6=== " + family.getFormattedName() + " §6==="));
        sender.sendMessage(Message.raw("§7Devise : §o" + family.getMotto()));
        sender.sendMessage(Message.raw("§7Rarete : " + family.getRarity().getDisplayName()));
        sender.sendMessage(Message.raw("§7Rang requis : " + family.getMinimumRank().getFormattedName()));
        sender.sendMessage(Message.raw("§7Competence : §e" + family.getFamilyPassive().getDisplayName()
                + " §8(" + family.getFamilyPassive().getDescription() + ")"));
        sender.sendMessage(Message.raw("§7Disponible : " + (FamilyManager.isFamilyTaken(familyId) ? "§cNon (prise)" : "§aOui")));
    }

    private UUID extractUUID(PlayerRef playerRef) throws Exception {
        Field uuidField = PlayerRef.class.getDeclaredField("uuid");
        uuidField.setAccessible(true);
        return (UUID) uuidField.get(playerRef);
    }
}
