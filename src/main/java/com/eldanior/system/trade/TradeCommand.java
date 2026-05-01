package com.eldanior.system.trade;

import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
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

public class TradeCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;

    public TradeCommand() {
        super("trade", "Echange (accept/decline/cancel)");
        this.actionArg = this.withRequiredArg("action", "accept|decline|cancel", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        String action = this.actionArg.get(ctx);

        assert sender.getWorld() != null;
        return CompletableFuture.runAsync(() -> {
            try {
                UUID senderUUID = getSenderUUID(sender);
                if (senderUUID == null) return;

                switch (action.toLowerCase()) {
                    case "accept" -> handleAccept(sender, senderUUID);
                    case "decline" -> handleDecline(sender, senderUUID);
                    case "cancel" -> handleCancel(sender, senderUUID);
                    default -> sender.sendMessage(Message.raw("§cUsage : /es trade <accept|decline|cancel>"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, sender.getWorld());
    }

    private void handleAccept(Player sender, UUID senderUUID) {
        if (!TradeManager.hasPendingInvite(senderUUID)) {
            sender.sendMessage(Message.raw("§cAucune demande d'echange en attente."));
            return;
        }

        if (TradeManager.isInTrade(senderUUID)) {
            sender.sendMessage(Message.raw("§cVous etes deja en echange."));
            TradeManager.clearInvite(senderUUID);
            return;
        }

        UUID inviterUUID = TradeManager.getPendingInviter(senderUUID);
        if (inviterUUID == null) return;

        PlayerRef inviterRef = Universe.get().getPlayer(inviterUUID);
        if (inviterRef == null) {
            sender.sendMessage(Message.raw("§cLe marchand n'est plus connecte."));
            TradeManager.clearInvite(senderUUID);
            return;
        }

        if (TradeManager.isInTrade(inviterUUID)) {
            sender.sendMessage(Message.raw("§cLe marchand est deja en echange."));
            TradeManager.clearInvite(senderUUID);
            return;
        }

        // Demarrer l'echange
        TradeSession session = TradeManager.startTrade(inviterUUID, senderUUID);
        if (session == null) {
            sender.sendMessage(Message.raw("§cImpossible de demarrer l'echange."));
            TradeManager.clearInvite(senderUUID);
            return;
        }

        // Ouvrir la fenetre pour les deux joueurs
        openTradeScreen(sender, senderUUID, session);
        openTradeScreenForRef(inviterRef, inviterUUID, session);

        sender.sendMessage(Message.raw("§a§lEchange commence !"));
        inviterRef.sendMessage(Message.raw("§a§l" + sender.getDisplayName() + " a accepte l'echange !"));
    }

    private void handleDecline(Player sender, UUID senderUUID) {
        if (!TradeManager.hasPendingInvite(senderUUID)) {
            sender.sendMessage(Message.raw("§cAucune demande d'echange en attente."));
            return;
        }

        UUID inviterUUID = TradeManager.getPendingInviter(senderUUID);
        TradeManager.clearInvite(senderUUID);

        sender.sendMessage(Message.raw("§7Demande d'echange refusee."));

        if (inviterUUID != null) {
            PlayerRef inviterRef = Universe.get().getPlayer(inviterUUID);
            if (inviterRef != null) {
                inviterRef.sendMessage(Message.raw("§c" + sender.getDisplayName() + " a refuse votre demande d'echange."));
            }
        }
    }

    private void handleCancel(Player sender, UUID senderUUID) {
        // Annuler une invitation en attente
        TradeManager.cancelInvite(senderUUID);

        // Ou annuler un trade actif
        TradeSession session = TradeManager.getSession(senderUUID);
        if (session != null) {
            UUID otherUUID = session.getOther(senderUUID);
            TradeManager.endTrade(session, false);

            sender.sendMessage(Message.raw("§7Echange annule."));
            PlayerRef otherRef = Universe.get().getPlayer(otherUUID);
            if (otherRef != null) {
                otherRef.sendMessage(Message.raw("§c" + sender.getDisplayName() + " a annule l'echange."));
            }
        } else {
            sender.sendMessage(Message.raw("§7Invitation annulee."));
        }
    }

    private void openTradeScreen(Player player, UUID playerUUID, TradeSession session) {
        try {
            var ref = player.getReference();
            if (ref == null) return;
            Store<EntityStore> store = ref.getStore();
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef == null) return;

            player.getPageManager().openCustomPage(ref, store,
                    new TradeScreen(playerRef, playerUUID, session));
        } catch (Exception e) {
            System.err.println("[Trade] Erreur ouverture fenetre: " + e.getMessage());
        }
    }

    private void openTradeScreenForRef(PlayerRef targetRef, UUID targetUUID, TradeSession session) {
        try {
            var ref = targetRef.getReference();
            if (ref == null) return;
            Store<EntityStore> store = ref.getStore();
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;

            player.getPageManager().openCustomPage(ref, store,
                    new TradeScreen(targetRef, targetUUID, session));
        } catch (Exception e) {
            System.err.println("[Trade] Erreur ouverture fenetre cible: " + e.getMessage());
        }
    }

    private UUID getSenderUUID(Player sender) throws Exception {
        var ref = sender.getReference();
        if (ref == null) return null;
        Store<EntityStore> store = ref.getStore();
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        return UUIDExtractor.getUUID(pRef);
    }
}
