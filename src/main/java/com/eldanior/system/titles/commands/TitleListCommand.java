package com.eldanior.system.titles.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.TitleManager;
import com.eldanior.system.titles.models.TitleModel;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class TitleListCommand extends AbstractAsyncCommand {

    public TitleListCommand() {
        super("titlelist", "Liste vos titres debloques");
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        assert sender.getWorld() != null;
        return CompletableFuture.runAsync(() -> {
            try {
                var ref = sender.getReference();
                if (ref == null) return;

                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData data = store.getComponent(ref, type);

                if (data == null) {
                    sender.sendMessage(Message.raw("§cAucune donnee trouvee."));
                    return;
                }

                sender.sendMessage(Message.raw("§6=== Vos Titres ==="));
                String currentTitleId = data.getCurrentTitle();
                for (String titleId : data.getUnlockedTitles()) {
                    TitleModel title = TitleManager.get(titleId);
                    if (title != null) {
                        String prefix = titleId.equals(currentTitleId) ? "§a[EQUIPE] " : "§7 - ";
                        sender.sendMessage(Message.raw(prefix + title.getFormattedName() + " §8(" + title.getId() + ")"));
                    } else {
                        sender.sendMessage(Message.raw("§7 - §8" + titleId + " (inconnu)"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, sender.getWorld());
    }
}