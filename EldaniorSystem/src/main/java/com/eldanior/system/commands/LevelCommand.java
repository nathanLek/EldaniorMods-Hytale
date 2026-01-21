package com.eldanior.system.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class LevelCommand extends AbstractAsyncCommand {

    public LevelCommand() {
        super("level", "Voir son niveau actuel");
        // AUCUN ARGUMENT DÉCLARÉ ICI -> Donc /level marchera parfaitement
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player player)) return CompletableFuture.completedFuture(null);

        // On bascule sur le Thread Principal pour lire les données en toute sécurité
        assert player.getWorld() != null;
        player.getWorld().execute(() -> {
            try {
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                assert player.getReference() != null;
                Store<EntityStore> store = player.getReference().getStore();

                PlayerLevelData data = store.getComponent(player.getReference(), type);
                // Si pas de données, on considère niveau 0
                int lvl = (data != null) ? data.getLevel() : 0;

                player.sendMessage(Message.raw("Niveau actuel : " + lvl));
            } catch (Exception e) {
                player.sendMessage(Message.raw("Erreur de lecture : " + e.getMessage()));
                e.printStackTrace();
            }
        });

        return CompletableFuture.completedFuture(null);
    }
}