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

// /es family [action] [joueur/familyId] — Actions: choose, invite, info, agreer, revoquer
// agreer / revoquer : marchands agrees du Comptoir de la Porte du Dragon (Clan Luo), par le Patriarche ou un Doyen Luo.
public class FamilyCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;
    private final RequiredArg<String> arg1;

    public FamilyCommand() {
        super("family", "Gestion des familles (choose/invite/info/agreer/revoquer)");
        this.actionArg = this.withRequiredArg("action", "choose|invite|info|agreer|revoquer", ArgTypes.STRING);
        this.arg1 = this.withRequiredArg("arg", "FamilyId ou joueur", ArgTypes.STRING);
    }

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
                    case "agreer" -> handleMerchant(sender, senderRef, ctx, true);
                    case "revoquer" -> handleMerchant(sender, senderRef, ctx, false);
                    default -> senderRef.sendMessage(Message.raw("Usage : /es family <choose|invite|info|agreer|revoquer> <familyId/joueur>"));
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

            // Assigner la propriete des parcelles famille au nouveau Patriarche
            UUID senderUUID = com.eldanior.system.config.UUIDExtractor.getUUID(sender.getPlayerRef());
            if (senderUUID != null) {
                com.eldanior.system.territory.ParcelManager.transferFamilyParcelsOwnership(
                        family.getId(), senderUUID, sender.getPlayerRef().getUsername());
            }

            // Verifier titres en temps reel apres choix de famille
            com.eldanior.system.titles.TitleManager.checkAndUnlockTitles(ref, store, copy, sender.getPlayerRef());

            sender.getPlayerRef().sendMessage(Message.raw("Vous etes Patriarche de la famille " + family.getFormattedName() + " !"));
            sender.getPlayerRef().sendMessage(Message.raw("Devise : " + family.getMotto()));
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
                sender.getPlayerRef().sendMessage(Message.raw("Seuls le Patriarche, les Doyens et le Seigneur peuvent inviter."));
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
            copy.setStatus(com.eldanior.system.titles.nobility.family.FamilyRole.MEMBER.code());
            store.putComponent(ref, type, copy);

            // Verifier titres en temps reel apres avoir rejoint une famille
            com.eldanior.system.titles.TitleManager.checkAndUnlockTitles(ref, store, copy, targetPlayer);

            sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " a rejoint " + family.getFormattedName() + " en tant que membre sans titre."));
            targetPlayer.sendMessage(Message.raw("Vous avez rejoint la famille " + family.getFormattedName() + " - " + family.getMotto()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== MARCHANDS AGREES (Clan Luo) ====================
    // Le Patriarche ou un Doyen Luo agree un etranger : LuoPlaqueSystem lui remet la plaque de Marchand agree du Comptoir.
    // Les admins peuvent aussi agreer et revoquer (territoire gere par l'admin).
    private void handleMerchant(Player sender, PlayerRef senderRef, CommandContext ctx, boolean grant) {
        String targetName = this.arg1.get(ctx);
        String familyId = com.eldanior.system.luo.LuoPlaques.FAMILY;
        try {
            var ref = sender.getReference();
            if (ref == null) return;
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData senderData = ref.getStore().getComponent(ref, type);
            com.eldanior.system.titles.nobility.family.FamilyRole role =
                    senderData == null || !familyId.equals(senderData.getNobleFamilyId()) ? null
                            : com.eldanior.system.titles.nobility.family.FamilyRole.of(senderData.getStatus());
            boolean allowed = role == com.eldanior.system.titles.nobility.family.FamilyRole.PATRIARCH
                    || role == com.eldanior.system.titles.nobility.family.FamilyRole.ELDER
                    || senderRef.hasPermission(com.eldanior.system.config.EldaniorLogger.ADMIN_PERMISSION);
            if (!allowed) {
                senderRef.sendMessage(Message.raw("Seuls le Patriarche et les Doyens du Clan Luo agreent les marchands du Comptoir."));
                return;
            }

            if (!grant) {
                UUID removed = FamilyManager.removeMerchant(familyId, targetName);
                if (removed == null) {
                    senderRef.sendMessage(Message.raw(targetName + " n'est pas un marchand agree du Comptoir."));
                    return;
                }
                senderRef.sendMessage(Message.raw("Agrement de " + targetName + " retire : sa plaque de marchand lui sera reprise."));
                PlayerRef target = Universe.get().getPlayer(removed);
                if (target != null) target.sendMessage(Message.raw("Le Clan Luo vous retire votre agrement de marchand du Comptoir."));
                return;
            }

            PlayerRef target = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
            if (target == null || target.getReference() == null) {
                senderRef.sendMessage(Message.raw("Le joueur doit etre connecte."));
                return;
            }
            var targetRef = target.getReference();
            PlayerLevelData targetData = targetRef.getStore().getComponent(targetRef, type);
            if (targetData != null && familyId.equals(targetData.getNobleFamilyId())) {
                senderRef.sendMessage(Message.raw(target.getUsername() + " est du clan : sa plaque de rang lui suffit."));
                return;
            }
            UUID targetUUID = extractUUID(target);
            if (targetUUID == null) return;
            if (!FamilyManager.addMerchant(familyId, targetUUID, target.getUsername())) {
                senderRef.sendMessage(Message.raw(target.getUsername() + " est deja marchand agree du Comptoir."));
                return;
            }
            senderRef.sendMessage(Message.raw(target.getUsername() + " est desormais marchand agree du Comptoir de la Porte du Dragon."));
            target.sendMessage(Message.raw("Le Clan Luo vous agree comme marchand du Comptoir de la Porte du Dragon. Portez votre plaque pour y entrer."));
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
        sender.getPlayerRef().sendMessage(Message.raw("Disponible : " + (FamilyManager.isFamilyTaken(familyId) ? "Non (prise)" : "Oui")));
        java.util.Map<UUID, String> merchants = FamilyManager.getMerchants(family.getId());
        if (!merchants.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("Marchands agrees : " + String.join(", ", merchants.values())));
        }
    }

    private UUID extractUUID(PlayerRef playerRef) throws Exception {
        return UUIDExtractor.getUUID(playerRef);
    }
}
