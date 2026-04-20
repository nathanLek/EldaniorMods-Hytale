package com.eldanior.system.titles.nobility.commands;

import com.eldanior.system.titles.nobility.NobilityManager;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;

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
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        String kingName = NobilityManager.getCurrentKingName();
        if (kingName.isEmpty()) {
            sender.sendMessage(Message.raw("§7Aucun Roi n'a ete nomme."));
            return CompletableFuture.completedFuture(null);
        }

        sender.sendMessage(Message.raw("§6=== Royaume ==="));
        sender.sendMessage(Message.raw("§cRoi : §f" + kingName));

        for (NobilityRank rank : new NobilityRank[]{NobilityRank.MARQUIS, NobilityRank.DUC, NobilityRank.COMTE, NobilityRank.BARON}) {
            int remaining = NobilityManager.getRemainingSlots(rank);
            sender.sendMessage(Message.raw("§7" + rank.getDisplayName() + " : " + rank.getColorCode()
                    + remaining + "/" + rank.getMaxPerKingdom() + " places"));
        }

        return CompletableFuture.completedFuture(null);
    }
}