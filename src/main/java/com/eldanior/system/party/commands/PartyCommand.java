package com.eldanior.system.party.commands;

import com.eldanior.system.party.Party;
import com.eldanior.system.party.PartyManager;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
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

public class PartyCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;
    private final OptionalArg<String> playerArg;

    public PartyCommand() {
        super("party", "Groupe (create/invite/kick/leave/disband/accept/decline/list)");
        this.actionArg = this.withRequiredArg("action", "create|invite|kick|leave|disband|accept|decline|list", ArgTypes.STRING);
        this.playerArg = this.withOptionalArg("joueur", "Joueur cible", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        Ref<EntityStore> ref = ctx.senderAsPlayerRef();
        if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);

        Store<EntityStore> store = ref.getStore();
        World world = ((EntityStore) store.getExternalData()).getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);

        String action = this.actionArg.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = store.getComponent(ref, PlayerRef.getComponentType());
                Player sender = store.getComponent(ref, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                switch (action.toLowerCase()) {
                    case "create" -> handleCreate(sender);
                    case "invite" -> handleInvite(sender, ctx);
                    case "kick" -> handleKick(sender, ctx);
                    case "leave" -> handleLeave(sender);
                    case "disband" -> handleDisband(sender);
                    case "accept" -> handleAccept(sender);
                    case "decline" -> handleDecline(sender);
                    case "list" -> handleList(sender);
                    default -> senderRef.sendMessage(Message.raw("Usage : /es party <create|invite|kick|leave|disband|accept|decline|list> <joueur>"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }

    // ==================== CREATE ====================
    private void handleCreate(Player sender) {
        try {
            UUID senderUUID = getSenderUUID(sender);
            if (PartyManager.hasParty(senderUUID)) {
                sender.getPlayerRef().sendMessage(Message.raw("Vous etes deja dans un groupe."));
                return;
            }

            Party party = PartyManager.createParty(senderUUID, sender.getPlayerRef().getUsername());
            if (party == null) {
                sender.getPlayerRef().sendMessage(Message.raw("Impossible de creer le groupe."));
                return;
            }

            sender.getPlayerRef().sendMessage(Message.raw("Groupe cree ! Vous etes le Capitaine."));
            sender.getPlayerRef().sendMessage(Message.raw("Invitez des joueurs avec /es party invite <joueur>"));

            // Afficher le HUD
            PartyManager.showHudForPlayer(sender, senderUUID);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== INVITE ====================
    private void handleInvite(Player sender, CommandContext ctx) {
        try {
            UUID senderUUID = getSenderUUID(sender);
            Party party = PartyManager.getParty(senderUUID);

            if (party == null) {
                sender.getPlayerRef().sendMessage(Message.raw("Vous n'etes dans aucun groupe. Creez-en un avec /es party create"));
                return;
            }

            if (!party.isCaptain(senderUUID)) {
                sender.getPlayerRef().sendMessage(Message.raw("Seul le Capitaine peut inviter."));
                return;
            }

            if (party.isFull()) {
                sender.getPlayerRef().sendMessage(Message.raw("Groupe plein (max " + Party.MAX_MEMBERS + " membres)."));
                return;
            }

            String targetName = this.playerArg.get(ctx);
            if (targetName == null || targetName.isEmpty()) {
                sender.getPlayerRef().sendMessage(Message.raw("Usage : /es party invite <joueur>"));
                return;
            }
            PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
            if (targetRef == null) {
                sender.getPlayerRef().sendMessage(Message.raw("Joueur '" + targetName + "' introuvable."));
                return;
            }

            UUID targetUUID = extractUUID(targetRef);

            if (PartyManager.hasParty(targetUUID)) {
                sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " est deja dans un groupe."));
                return;
            }

            PartyManager.sendInvite(targetUUID, senderUUID);

            sender.getPlayerRef().sendMessage(Message.raw("Invitation envoyee a " + targetName));
            targetRef.sendMessage(Message.raw("" + sender.getPlayerRef().getUsername() + " vous invite a rejoindre son groupe !"));
            targetRef.sendMessage(Message.raw("Tapez /es party accept pour accepter ou /es party decline pour refuser."));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== ACCEPT ====================
    private void handleAccept(Player sender) {
        try {
            UUID senderUUID = getSenderUUID(sender);

            if (!PartyManager.hasPendingInvite(senderUUID)) {
                sender.getPlayerRef().sendMessage(Message.raw("Aucune invitation en attente."));
                return;
            }

            if (PartyManager.hasParty(senderUUID)) {
                sender.getPlayerRef().sendMessage(Message.raw("Vous etes deja dans un groupe."));
                PartyManager.clearInvite(senderUUID);
                return;
            }

            UUID captainUUID = PartyManager.getPendingInvite(senderUUID);
            Party party = PartyManager.getParty(captainUUID);

            if (party == null) {
                sender.getPlayerRef().sendMessage(Message.raw("Le groupe n'existe plus."));
                PartyManager.clearInvite(senderUUID);
                return;
            }

            if (party.isFull()) {
                sender.getPlayerRef().sendMessage(Message.raw("Le groupe est plein."));
                PartyManager.clearInvite(senderUUID);
                return;
            }

            PartyManager.joinParty(senderUUID, sender.getPlayerRef().getUsername(), party);
            PartyManager.clearInvite(senderUUID);

            // Notifier tous les membres
            for (UUID memberUUID : party.getMemberUUIDs()) {
                PlayerRef memberRef = Universe.get().getPlayer(memberUUID);
                if (memberRef != null) {
                    memberRef.sendMessage(Message.raw("" + sender.getPlayerRef().getUsername() + " a rejoint le groupe ! (" + party.getSize() + "/" + Party.MAX_MEMBERS + ")"));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== DECLINE ====================
    private void handleDecline(Player sender) {
        try {
            UUID senderUUID = getSenderUUID(sender);
            if (!PartyManager.hasPendingInvite(senderUUID)) {
                sender.getPlayerRef().sendMessage(Message.raw("Aucune invitation en attente."));
                return;
            }
            PartyManager.clearInvite(senderUUID);
            sender.getPlayerRef().sendMessage(Message.raw("Invitation refusee."));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== KICK ====================
    private void handleKick(Player sender, CommandContext ctx) {
        try {
            UUID senderUUID = getSenderUUID(sender);
            Party party = PartyManager.getParty(senderUUID);

            if (party == null) {
                sender.getPlayerRef().sendMessage(Message.raw("Vous n'etes dans aucun groupe."));
                return;
            }

            if (!party.isCaptain(senderUUID)) {
                sender.getPlayerRef().sendMessage(Message.raw("Seul le Capitaine peut exclure."));
                return;
            }

            String targetName = this.playerArg.get(ctx);
            if (targetName == null || targetName.isEmpty()) {
                sender.getPlayerRef().sendMessage(Message.raw("Usage : /es party kick <joueur>"));
                return;
            }
            UUID targetUUID = null;
            for (var entry : party.getMembers().entrySet()) {
                if (entry.getValue().equalsIgnoreCase(targetName)) {
                    targetUUID = entry.getKey();
                    break;
                }
            }

            if (targetUUID == null) {
                sender.getPlayerRef().sendMessage(Message.raw("" + targetName + " n'est pas dans votre groupe."));
                return;
            }

            if (targetUUID.equals(senderUUID)) {
                sender.getPlayerRef().sendMessage(Message.raw("Vous ne pouvez pas vous exclure. Utilisez /es party leave"));
                return;
            }

            PartyManager.leaveParty(targetUUID);

            // Notifier le joueur kick
            PlayerRef targetRef = Universe.get().getPlayer(targetUUID);
            if (targetRef != null) {
                targetRef.sendMessage(Message.raw("Vous avez ete exclu du groupe."));
            }

            // Notifier les autres
            Party updatedParty = PartyManager.getParty(senderUUID);
            if (updatedParty != null) {
                for (UUID memberUUID : updatedParty.getMemberUUIDs()) {
                    PlayerRef memberRef = Universe.get().getPlayer(memberUUID);
                    if (memberRef != null) {
                        memberRef.sendMessage(Message.raw("" + targetName + " a ete exclu du groupe."));
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== LEAVE ====================
    private void handleLeave(Player sender) {
        try {
            UUID senderUUID = getSenderUUID(sender);
            Party party = PartyManager.getParty(senderUUID);

            if (party == null) {
                sender.getPlayerRef().sendMessage(Message.raw("Vous n'etes dans aucun groupe."));
                return;
            }

            String leaverName = sender.getPlayerRef().getUsername();
            boolean wasCaptain = party.isCaptain(senderUUID);

            PartyManager.leaveParty(senderUUID);
            sender.getPlayerRef().sendMessage(Message.raw("Vous avez quitte le groupe."));

            // Notifier les membres restants
            // Le capitaine a pu changer si c'etait lui qui partait
            for (UUID memberUUID : new java.util.ArrayList<>(party.getMemberUUIDs())) {
                Party memberParty = PartyManager.getParty(memberUUID);
                if (memberParty != null) {
                    PlayerRef memberRef = Universe.get().getPlayer(memberUUID);
                    if (memberRef != null) {
                        memberRef.sendMessage(Message.raw("" + leaverName + " a quitte le groupe."));
                        if (wasCaptain && memberParty.isCaptain(memberUUID)) {
                            memberRef.sendMessage(Message.raw("Vous etes maintenant le Capitaine !"));
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== DISBAND ====================
    private void handleDisband(Player sender) {
        try {
            UUID senderUUID = getSenderUUID(sender);
            Party party = PartyManager.getParty(senderUUID);

            if (party == null) {
                sender.getPlayerRef().sendMessage(Message.raw("Vous n'etes dans aucun groupe."));
                return;
            }

            if (!party.isCaptain(senderUUID)) {
                sender.getPlayerRef().sendMessage(Message.raw("Seul le Capitaine peut dissoudre le groupe."));
                return;
            }

            // Notifier tous les membres avant dissolution
            for (UUID memberUUID : party.getMemberUUIDs()) {
                PlayerRef memberRef = Universe.get().getPlayer(memberUUID);
                if (memberRef != null) {
                    memberRef.sendMessage(Message.raw("Le groupe a ete dissout par le Capitaine."));
                }
            }

            PartyManager.disbandParty(party);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== LIST ====================
    private void handleList(Player sender) {
        try {
            UUID senderUUID = getSenderUUID(sender);
            Party party = PartyManager.getParty(senderUUID);

            if (party == null) {
                sender.getPlayerRef().sendMessage(Message.raw("Vous n'etes dans aucun groupe."));
                return;
            }

            sender.getPlayerRef().sendMessage(Message.raw("=== Groupe (" + party.getSize() + "/" + Party.MAX_MEMBERS + ") ==="));
            for (var entry : party.getMembers().entrySet()) {
                String prefix = party.isCaptain(entry.getKey()) ? "[CAP] " : " - ";
                sender.getPlayerRef().sendMessage(Message.raw(prefix + "" + entry.getValue()));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== UTILS ====================
    private UUID extractUUID(PlayerRef playerRef) throws Exception {
        return UUIDExtractor.getUUID(playerRef);
    }

    private UUID getSenderUUID(Player sender) throws Exception {
        var ref = sender.getReference();
        if (ref == null) return null;
        var store = ref.getStore();
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        return extractUUID(pRef);
    }
}
