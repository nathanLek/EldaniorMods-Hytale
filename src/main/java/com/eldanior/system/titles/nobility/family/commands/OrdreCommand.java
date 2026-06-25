package com.eldanior.system.titles.nobility.family.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.KnightOrder;
import com.eldanior.system.titles.nobility.family.KnightOrderManager;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OrdreCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;
    private final RequiredArg<String> arg1;
    private final RequiredArg<String> arg2;

    public OrdreCommand() {
        super("ordre", "Gestion des Ordres de Chevalier");
        this.actionArg = this.withRequiredArg("action", "create/invite/approve/reject/leave/info", ArgTypes.STRING);
        this.arg1 = this.withRequiredArg("arg1", "Nom ou joueur", ArgTypes.STRING);
        this.arg2 = this.withRequiredArg("arg2", "Devise ou argument", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        Ref<EntityStore> senderEntityRef = ctx.senderAsPlayerRef();
        if (senderEntityRef == null || !senderEntityRef.isValid()) return CompletableFuture.completedFuture(null);

        Store<EntityStore> senderEntityStore = senderEntityRef.getStore();
        World world = ((EntityStore) senderEntityStore.getExternalData()).getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);

        String action = this.actionArg.get(ctx);
        String a1 = this.arg1.get(ctx);
        String a2 = this.arg2.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = senderEntityStore.getComponent(senderEntityRef, PlayerRef.getComponentType());
                Player sender = senderEntityStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                var ref = sender.getReference();
                if (ref == null) return;
                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) return;

                UUID senderUUID = UUIDExtractor.getUUID(senderRef);
                if (senderUUID == null) return;

                String familyId = data.getNobleFamilyId();
                if (familyId == null || familyId.isEmpty()) {
                    senderRef.sendMessage(Message.raw("Vous devez appartenir a une famille noble."));
                    return;
                }

                switch (action.toLowerCase()) {
                    case "create" -> handleCreate(senderRef, data, senderUUID, familyId, a1, a2);
                    case "approve" -> handleApprove(senderRef, data, senderUUID, familyId, a1);
                    case "reject" -> handleReject(senderRef, data, familyId, a1);
                    case "invite" -> handleInvite(senderRef, data, senderUUID, a1);
                    case "leave" -> handleLeave(senderRef, senderUUID);
                    case "info" -> handleInfo(senderRef, familyId);
                    default -> {
                        senderRef.sendMessage(Message.raw("Usage : /es ordre <create|approve|reject|invite|leave|info> <args>"));
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }, world);
    }

    private void handleCreate(PlayerRef senderRef, PlayerLevelData data, UUID senderUUID, String familyId, String name, String motto) {
        // Check rank >= CHEVALIER
        NobilityRank rank = NobilityRank.fromString(data.getNobilityRank());
        if (rank == null || rank.ordinal() < NobilityRank.CHEVALIER.ordinal()) {
            senderRef.sendMessage(Message.raw("Rang minimum requis : Chevalier."));
            return;
        }

        if (!KnightOrderManager.canCreateOrder(familyId)) {
            senderRef.sendMessage(Message.raw("Cette famille a deja 3 ordres (maximum atteint)."));
            return;
        }

        if (KnightOrderManager.getPlayerOrder(senderUUID) != null) {
            senderRef.sendMessage(Message.raw("Vous etes deja dans un ordre."));
            return;
        }

        String deviseCleaned = motto.replace("_", " ");
        String nameCleaned = name.replace("_", " ");

        if (data.isPatriarch()) {
            // Patriarch can create directly
            KnightOrder order = KnightOrderManager.createOrder(nameCleaned, deviseCleaned, familyId, senderUUID, senderRef.getUsername());
            if (order == null) {
                senderRef.sendMessage(Message.raw("Erreur lors de la creation (nom 3-24 chars)."));
                return;
            }
            senderRef.sendMessage(Message.raw("Ordre \"" + order.getName() + "\" cree ! Vous en etes le Capitaine."));
        } else {
            // Non-patriarch: send request to patriarch
            KnightOrderManager.requestCreation(senderUUID, senderRef.getUsername(), nameCleaned, deviseCleaned, familyId);
            senderRef.sendMessage(Message.raw("Demande de creation de l'Ordre \"" + nameCleaned + "\" envoyee au Patriarche."));

            // Notify patriarch if online
            for (PlayerRef pRef : Universe.get().getPlayers()) {
                try {
                    var eRef = pRef.getReference();
                    if (eRef == null) continue;
                    var s = eRef.getStore();
                    PlayerLevelData pData = s.getComponent(eRef, EldaniorSystem.get().getPlayerLevelDataType());
                    if (pData != null && familyId.equals(pData.getNobleFamilyId()) && pData.isPatriarch()) {
                        pRef.sendMessage(Message.raw(senderRef.getUsername() + " demande a creer l'Ordre \"" + nameCleaned + "\"."));
                        pRef.sendMessage(Message.raw("Tapez /es ordre approve " + senderRef.getUsername() + " _ pour accepter."));
                        pRef.sendMessage(Message.raw("Tapez /es ordre reject " + senderRef.getUsername() + " _ pour refuser."));
                    }
                } catch (Exception e) { /* skip */ }
            }
        }
    }

    private void handleApprove(PlayerRef senderRef, PlayerLevelData data, UUID senderUUID, String familyId, String requesterName) {
        if (!data.isPatriarch()) {
            senderRef.sendMessage(Message.raw("Seul le Patriarche peut approuver les demandes."));
            return;
        }

        // Find requester UUID
        PlayerRef requesterRef = Universe.get().getPlayerByUsername(requesterName, NameMatching.EXACT_IGNORE_CASE);
        if (requesterRef == null) {
            senderRef.sendMessage(Message.raw("Joueur " + requesterName + " introuvable ou hors ligne."));
            return;
        }

        UUID requesterUUID = null;
        try { requesterUUID = UUIDExtractor.getUUID(requesterRef); } catch (Exception e) { return; }
        if (requesterUUID == null) return;

        KnightOrder order = KnightOrderManager.approveRequest(senderUUID, requesterUUID);
        if (order == null) {
            senderRef.sendMessage(Message.raw("Aucune demande en attente de " + requesterName + " ou limite atteinte."));
            return;
        }

        senderRef.sendMessage(Message.raw("Ordre \"" + order.getName() + "\" approuve ! " + requesterName + " en est le Capitaine."));
        requesterRef.sendMessage(Message.raw("Votre Ordre \"" + order.getName() + "\" a ete approuve par le Patriarche !"));
    }

    private void handleReject(PlayerRef senderRef, PlayerLevelData data, String familyId, String requesterName) {
        if (!data.isPatriarch()) {
            senderRef.sendMessage(Message.raw("Seul le Patriarche peut refuser les demandes."));
            return;
        }

        PlayerRef requesterRef = Universe.get().getPlayerByUsername(requesterName, NameMatching.EXACT_IGNORE_CASE);
        if (requesterRef == null) {
            senderRef.sendMessage(Message.raw("Joueur " + requesterName + " introuvable ou hors ligne."));
            return;
        }

        UUID requesterUUID = null;
        try { requesterUUID = UUIDExtractor.getUUID(requesterRef); } catch (Exception e) { return; }
        if (requesterUUID == null) return;

        if (KnightOrderManager.rejectRequest(requesterUUID)) {
            senderRef.sendMessage(Message.raw("Demande de " + requesterName + " refusee."));
            requesterRef.sendMessage(Message.raw("Votre demande de creation d'Ordre a ete refusee par le Patriarche."));
        } else {
            senderRef.sendMessage(Message.raw("Aucune demande en attente de " + requesterName + "."));
        }
    }

    private void handleInvite(PlayerRef senderRef, PlayerLevelData data, UUID senderUUID, String targetName) {
        KnightOrder order = KnightOrderManager.getPlayerOrder(senderUUID);
        if (order == null || !order.isCapitaine(senderUUID)) {
            senderRef.sendMessage(Message.raw("Vous devez etre Capitaine d'un Ordre pour inviter."));
            return;
        }

        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) {
            senderRef.sendMessage(Message.raw("Joueur " + targetName + " introuvable ou hors ligne."));
            return;
        }

        UUID targetUUID = null;
        try { targetUUID = UUIDExtractor.getUUID(targetRef); } catch (Exception e) { return; }
        if (targetUUID == null) return;

        // Check target is in same family and has no order
        try {
            var tRef = targetRef.getReference();
            if (tRef != null) {
                var tStore = tRef.getStore();
                PlayerLevelData tData = tStore.getComponent(tRef, EldaniorSystem.get().getPlayerLevelDataType());
                if (tData == null || !order.getFamilyId().equals(tData.getNobleFamilyId())) {
                    senderRef.sendMessage(Message.raw(targetName + " n'est pas dans votre famille."));
                    return;
                }
                NobilityRank tRank = NobilityRank.fromString(tData.getNobilityRank());
                if (tRank == null || tRank.ordinal() < NobilityRank.CHEVALIER.ordinal()) {
                    senderRef.sendMessage(Message.raw(targetName + " doit etre au moins Chevalier."));
                    return;
                }
            }
        } catch (Exception e) { EldaniorLogger.error("OrdreCommand", e); return; }

        if (KnightOrderManager.getPlayerOrder(targetUUID) != null) {
            senderRef.sendMessage(Message.raw(targetName + " est deja dans un ordre."));
            return;
        }

        if (!KnightOrderManager.joinOrder(targetUUID, order)) {
            senderRef.sendMessage(Message.raw("L'ordre est plein (10 membres max)."));
            return;
        }

        senderRef.sendMessage(Message.raw(targetName + " a rejoint l'Ordre " + order.getName() + " !"));
        targetRef.sendMessage(Message.raw("Vous avez rejoint l'Ordre " + order.getName() + " !"));
    }

    private void handleLeave(PlayerRef senderRef, UUID senderUUID) {
        KnightOrder order = KnightOrderManager.getPlayerOrder(senderUUID);
        if (order == null) {
            senderRef.sendMessage(Message.raw("Vous n'etes dans aucun ordre."));
            return;
        }

        String orderName = order.getName();
        KnightOrderManager.leaveOrder(senderUUID);
        senderRef.sendMessage(Message.raw("Vous avez quitte l'Ordre " + orderName + "."));
    }

    private void handleInfo(PlayerRef senderRef, String familyId) {
        List<KnightOrder> orders = KnightOrderManager.getOrdersForFamily(familyId);
        if (orders.isEmpty()) {
            senderRef.sendMessage(Message.raw("Aucun ordre dans cette famille."));
            return;
        }

        senderRef.sendMessage(Message.raw("=== Ordres de Chevalier (" + orders.size() + "/3) ==="));
        for (KnightOrder order : orders) {
            senderRef.sendMessage(Message.raw("  " + order.getName() + " - Cap: " + order.getCapitaineName() + " - " + order.getMemberCount() + "/10 membres - " + order.getTotalKills() + " kills"));
        }

        // Show pending requests
        var pending = KnightOrderManager.getPendingRequestsForFamily(familyId);
        if (!pending.isEmpty()) {
            senderRef.sendMessage(Message.raw("--- Demandes en attente ---"));
            for (var req : pending) {
                senderRef.sendMessage(Message.raw("  " + req.getRequesterName() + " veut creer \"" + req.getOrderName() + "\""));
            }
        }
    }
}
