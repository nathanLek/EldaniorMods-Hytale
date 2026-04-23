package com.eldanior.system.duel;

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
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DuelCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;

    public DuelCommand() {
        super("duel", "Duel (accept/decline)");
        this.actionArg = this.withRequiredArg("action", "accept|decline", ArgTypes.STRING);
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
                    case "accept" -> {
                        if (!DuelManager.hasPendingChallenge(senderUUID)) {
                            sender.sendMessage(Message.raw("§cAucun defi en attente."));
                            return;
                        }

                        UUID challengerUUID = DuelManager.getPendingChallenger(senderUUID);
                        DuelManager.clearChallenge(senderUUID);

                        PlayerRef challengerRef = Universe.get().getPlayer(challengerUUID);
                        if (challengerRef == null) {
                            sender.sendMessage(Message.raw("§cLe joueur n'est plus connecte."));
                            return;
                        }

                        if (DuelManager.isInDuel(challengerUUID) || DuelManager.isInDuel(senderUUID)) {
                            sender.sendMessage(Message.raw("§cUn des joueurs est deja en duel."));
                            return;
                        }

                        // Lancer le duel
                        DuelManager.startDuel(senderUUID, challengerUUID);

                        sender.sendMessage(Message.raw("§6§lDUEL COMMENCE ! §7Battez-vous !"));
                        challengerRef.sendMessage(Message.raw("§6§lDUEL COMMENCE ! §7" + sender.getDisplayName() + " a accepte !"));
                    }
                    case "decline" -> {
                        if (!DuelManager.hasPendingChallenge(senderUUID)) {
                            sender.sendMessage(Message.raw("§cAucun defi en attente."));
                            return;
                        }
                        UUID challengerUUID = DuelManager.getPendingChallenger(senderUUID);
                        DuelManager.clearChallenge(senderUUID);

                        sender.sendMessage(Message.raw("§7Defi refuse."));
                        PlayerRef challengerRef = Universe.get().getPlayer(challengerUUID);
                        if (challengerRef != null) {
                            challengerRef.sendMessage(Message.raw("§c" + sender.getDisplayName() + " a refuse votre defi."));
                        }
                    }
                    default -> sender.sendMessage(Message.raw("§cUsage : /es duel <accept|decline>"));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

    private UUID getSenderUUID(Player sender) throws Exception {
        var ref = sender.getReference();
        if (ref == null) return null;
        Store<EntityStore> store = ref.getStore();
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        Field f = PlayerRef.class.getDeclaredField("uuid");
        f.setAccessible(true);
        return (UUID) f.get(pRef);
    }
}
