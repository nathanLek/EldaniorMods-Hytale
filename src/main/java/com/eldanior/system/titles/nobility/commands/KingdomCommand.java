package com.eldanior.system.titles.nobility.commands;

import com.eldanior.system.titles.nobility.NobilityManager;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * /es kingdom — Vue d'ensemble du royaume (0 arg)
 */
public class KingdomCommand extends AbstractAsyncCommand {

    public KingdomCommand() {
        super("kingdom", "Vue d'ensemble du royaume");
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

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = senderEntityStore.getComponent(senderEntityRef, PlayerRef.getComponentType());
                if (senderRef == null) return;

                String kingName = NobilityManager.getCurrentKingName();
                if (kingName.isEmpty()) {
                    senderRef.sendMessage(Message.raw("§7Aucun Roi n'a ete nomme."));
                    return;
                }

                senderRef.sendMessage(Message.raw("§6=== Royaume ==="));
                senderRef.sendMessage(Message.raw("§cRoi : §f" + kingName));

                for (NobilityRank rank : new NobilityRank[]{NobilityRank.MARQUIS, NobilityRank.DUC, NobilityRank.COMTE, NobilityRank.BARON}) {
                    int remaining = NobilityManager.getRemainingSlots(rank);
                    senderRef.sendMessage(Message.raw("§7" + rank.getDisplayName() + " : " + rank.getColorCode()
                            + remaining + "/" + rank.getMaxPerKingdom() + " places"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }
}
