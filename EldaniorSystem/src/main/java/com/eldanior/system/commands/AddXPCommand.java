package com.eldanior.system.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class AddXPCommand extends AbstractAsyncCommand {
    private final RequiredArg<Integer> amountArg;

    public AddXPCommand() {
        super("addxp", "Ajouter de l'expérience");
        this.amountArg = this.withRequiredArg("quantité", "Montant d'XP", ArgTypes.INTEGER);
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player player)) return CompletableFuture.completedFuture(null);

        int amount = amountArg.get(ctx);

        assert player.getWorld() != null;
        player.getWorld().execute(() -> {
            var ref = player.getReference();
            assert ref != null;
            Store<EntityStore> store = ref.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

            PlayerLevelData data = store.getComponent(ref, type);
            if (data == null) data = new PlayerLevelData();

            int oldLvl = data.getLevel();
            data.addExperience(amount);
            store.putComponent(ref, type, data);

            player.sendMessage(Message.raw("+" + amount + " XP !"));
            if (data.getLevel() > oldLvl) {
                player.sendMessage(Message.raw("LEVEL UP ! Vous êtes maintenant niveau " + data.getLevel()));
            }
        });

        return CompletableFuture.completedFuture(null);
    }
}