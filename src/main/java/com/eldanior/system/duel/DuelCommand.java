package com.eldanior.system.duel;

import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DuelCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;

    public DuelCommand() {
        super("duel", "Duel (accept/decline/player)");
        this.actionArg = this.withRequiredArg("action", "accept|decline|<player>", ArgTypes.STRING);
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

                UUID senderUUID = UUIDExtractor.getUUID(senderRef);
                if (senderUUID == null) return;

                switch (action.toLowerCase()) {
                    case "accept" -> {
                        if (!DuelManager.hasPendingChallenge(senderUUID)) {
                            sender.getPlayerRef().sendMessage(Message.raw("§cAucun defi en attente."));
                            return;
                        }

                        UUID challengerUUID = DuelManager.getPendingChallenger(senderUUID);
                        DuelManager.clearChallenge(senderUUID);

                        PlayerRef challengerRef = Universe.get().getPlayer(challengerUUID);
                        if (challengerRef == null) {
                            sender.getPlayerRef().sendMessage(Message.raw("§cLe joueur n'est plus connecte."));
                            return;
                        }

                        if (DuelManager.isInDuel(challengerUUID) || DuelManager.isInDuel(senderUUID)) {
                            sender.getPlayerRef().sendMessage(Message.raw("§cUn des joueurs est deja en duel."));
                            return;
                        }

                        // Lancer le duel
                        DuelManager.startDuel(senderUUID, challengerUUID);

                        sender.getPlayerRef().sendMessage(Message.raw("§6§lDUEL COMMENCE ! §7Battez-vous !"));
                        challengerRef.sendMessage(Message.raw("§6§lDUEL COMMENCE ! §7" + sender.getPlayerRef().getUsername() + " a accepte !"));
                    }
                    case "decline" -> {
                        if (!DuelManager.hasPendingChallenge(senderUUID)) {
                            sender.getPlayerRef().sendMessage(Message.raw("§cAucun defi en attente."));
                            return;
                        }
                        UUID challengerUUID = DuelManager.getPendingChallenger(senderUUID);
                        DuelManager.clearChallenge(senderUUID);

                        sender.getPlayerRef().sendMessage(Message.raw("§7Defi refuse."));
                        PlayerRef challengerRef = Universe.get().getPlayer(challengerUUID);
                        if (challengerRef != null) {
                            challengerRef.sendMessage(Message.raw("§c" + sender.getPlayerRef().getUsername() + " a refuse votre defi."));
                        }
                    }
                    default -> {
                        // Treat action as a player name to challenge
                        String targetName = action;
                        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
                        if (targetRef == null) {
                            sender.getPlayerRef().sendMessage(Message.raw("§cJoueur introuvable : " + targetName));
                            return;
                        }

                        UUID targetUUID = UUIDExtractor.getUUID(targetRef);
                        if (targetUUID == null) {
                            sender.getPlayerRef().sendMessage(Message.raw("§cErreur : impossible d'identifier ce joueur."));
                            return;
                        }

                        if (targetUUID.equals(senderUUID)) {
                            sender.getPlayerRef().sendMessage(Message.raw("§cVous ne pouvez pas vous defier vous-meme."));
                            return;
                        }

                        if (DuelManager.isInDuel(senderUUID)) {
                            sender.getPlayerRef().sendMessage(Message.raw("§cVous etes deja en duel."));
                            return;
                        }

                        if (DuelManager.isInDuel(targetUUID)) {
                            sender.getPlayerRef().sendMessage(Message.raw("§c" + targetName + " est deja en duel."));
                            return;
                        }

                        if (!DuelManager.sendChallenge(senderUUID, targetUUID)) {
                            sender.getPlayerRef().sendMessage(Message.raw("§cVous avez deja un defi en attente ! Attendez qu'il expire ou soit accepte/refuse."));
                            return;
                        }

                        sender.getPlayerRef().sendMessage(Message.raw("§6Defi envoye a " + targetRef.getUsername() + " ! §7(expire dans 60s)"));
                        targetRef.sendMessage(Message.raw("§6§l" + sender.getPlayerRef().getUsername() + " vous defie en duel !"));
                        targetRef.sendMessage(Message.raw("§7Tapez §f/es duel accept §7ou §f/es duel decline §7(expire dans 60s)"));
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }, world);
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