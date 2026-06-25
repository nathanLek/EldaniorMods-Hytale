package com.eldanior.system.titles.nobility.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.NobilityManager;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
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

// /es family [action] [joueur/familyId] — Actions: choose, invite, info
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
                    case "choose" -> handleChoose(sender, ctx, world);
                    case "invite" -> handleInvite(sender, ctx, world);
                    case "info" -> handleInfo(sender, ctx);
                    default -> senderRef.sendMessage(Message.raw("Usage : /es family <choose|invite|info> <familyId/joueur>"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }

    // ==================== CHOOSE ====================
    private void handleChoose(Player sender, CommandContext ctx, World world) {
        String familyId = this.arg1.get(ctx).toLowerCase();

        NobleFamilyModel family = FamilyManager.get(familyId);
        if (family == null) {
            sender.getPlayerRef().sendMessage(Message.raw("Famille '" + familyId + "' inconnue."));
            sender.getPlayerRef().sendMessage(Message.raw("Disponibles : " + FamilyManager.getAvailableIds()));
            return;
        }

        if (FamilyManager.isFamilyTaken(familyId)) {
            sender.getPlayerRef().sendMessage(Message.raw("Cette famille est deja prise."));
            return;
        }

        try {
            var ref = sender.getReference();
            if (ref == null) return;
            Store<EntityStore> store = ref.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = store.getComponent(ref, type);
            if (data == null) return;

            NobilityRank rank = NobilityRank.fromString(data.getNobilityRank());
            if (rank == null || (rank != NobilityRank.MARQUIS && rank != NobilityRank.DUC)) {
                sender.getPlayerRef().sendMessage(Message.raw("Seuls les Marquis et Ducs peuvent choisir une famille."));
                return;
            }

            if (data.getNobleFamilyId() != null && !data.getNobleFamilyId().isEmpty()) {
                sender.getPlayerRef().sendMessage(Message.raw("Vous appartenez deja a une famille."));
                return;
            }

            if (family.getMinimumRank() != rank) {
                sender.getPlayerRef().sendMessage(Message.raw("Cette famille est reservee aux " + family.getMinimumRank().getFormattedName() + "."));
                sender.getPlayerRef().sendMessage(Message.raw("Pour votre rang : " + FamilyManager.getAvailableFamilyIdsForRank(rank)));
                return;
            }

            PlayerLevelData copy = (PlayerLevelData) data.clone();
            if (copy == null) return;
            copy.setNobleFamilyId(family.getId());
            copy.setStatus("PATRIARCH");
            store.putComponent(ref, type, copy);

            FamilyManager.claimFamily(family.getId());

            // Verifier titres en temps reel apres choix de famille
            com.eldanior.system.titles.TitleManager.checkAndUnlockTitles(ref, store, copy, sender.getPlayerRef());

            sender.getPlayerRef().sendMessage(Message.raw("Vous etes Patriarche de la famille " + family.getFormattedName() + " !"));
            sender.getPlayerRef().sendMessage(Message.raw("Devise : " + family.getMotto()));
            sender.getPlayerRef().sendMessage(Message.raw("Competence : " + family.getFamilyPassive().getDisplayName()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== INVITE ====================
    private void handleInvite(Player sender, CommandContext ctx, World world) {
        String targetName = this.arg1.get(ctx);

        try {
            var senderRef = sender.getReference();
            if (senderRef == null) return;
            Store<EntityStore> senderStore = senderRef.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData senderData = senderStore.getComponent(senderRef, type);
            if (senderData == null) return;

            if (!senderData.canInviteToFamily()) {
                sender.getPlayerRef().sendMessage(Message.raw("Seul le Patriarche ou le Vice-Patriarche peut inviter."));
                return;
            }

            String familyId = senderData.getNobleFamilyId();
            if (familyId == null || familyId.isEmpty()) {
                sender.getPlayerRef().sendMessage(Message.raw("Vous n'appartenez a aucune famille."));
                return;
            }

            NobleFamilyModel family = FamilyManager.get(familyId);
            if (family == null) { sender.getPlayerRef().sendMessage(Message.raw("Famille introuvable.")); return; }

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

            if (data.getNobleFamilyId() != null && !data.getNobleFamilyId().isEmpty()) {
                sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " appartient deja a une famille."));
                return;
            }

            NobilityRank targetRank = NobilityRank.fromString(data.getNobilityRank());
            if (targetRank == null || !targetRank.isNoble()) {
                sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " n'est pas noble."));
                return;
            }

            PlayerLevelData copy = (PlayerLevelData) data.clone();
            if (copy == null) return;
            copy.setNobleFamilyId(familyId);
            copy.setStatus("MEMBER");
            store.putComponent(ref, type, copy);

            // Verifier titres en temps reel apres avoir rejoint une famille
            com.eldanior.system.titles.TitleManager.checkAndUnlockTitles(ref, store, copy, targetPlayer);

            sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " a rejoint " + family.getFormattedName() + " en tant que Membre."));
            targetPlayer.sendMessage(Message.raw("Vous avez rejoint la famille " + family.getFormattedName() + " - " + family.getMotto()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== INFO ====================
    private void handleInfo(Player sender, CommandContext ctx) {
        String familyId = this.arg1.get(ctx).toLowerCase();

        NobleFamilyModel family = FamilyManager.get(familyId);
        if (family == null) {
            sender.getPlayerRef().sendMessage(Message.raw("Famille '" + familyId + "' inconnue."));
            sender.getPlayerRef().sendMessage(Message.raw("Disponibles : " + FamilyManager.getAvailableIds()));
            return;
        }

        sender.getPlayerRef().sendMessage(Message.raw("=== " + family.getFormattedName() + " ==="));
        sender.getPlayerRef().sendMessage(Message.raw("Devise : " + family.getMotto()));
        sender.getPlayerRef().sendMessage(Message.raw("Rarete : " + family.getRarity().getDisplayName()));
        sender.getPlayerRef().sendMessage(Message.raw("Rang requis : " + family.getMinimumRank().getFormattedName()));
        sender.getPlayerRef().sendMessage(Message.raw("Competence : " + family.getFamilyPassive().getDisplayName()
                + " (" + family.getFamilyPassive().getDescription() + ")"));
        sender.getPlayerRef().sendMessage(Message.raw("Disponible : " + (FamilyManager.isFamilyTaken(familyId) ? "Non (prise)" : "Oui")));
    }

    private UUID extractUUID(PlayerRef playerRef) throws Exception {
        return UUIDExtractor.getUUID(playerRef);
    }
}
