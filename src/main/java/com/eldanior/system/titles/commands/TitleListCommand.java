package com.eldanior.system.titles.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.TitleManager;
import com.eldanior.system.titles.models.TitleModel;
import com.hypixel.hytale.component.ComponentType;
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

public class TitleListCommand extends AbstractAsyncCommand {

    public TitleListCommand() {
        super("titlelist", "Liste vos titres debloques");
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
                Player sender = senderEntityStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                var ref = sender.getReference();
                if (ref == null) return;

                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData data = store.getComponent(ref, type);

                if (data == null) {
                    sender.getPlayerRef().sendMessage(Message.raw("Aucune donnee trouvee."));
                    return;
                }

                sender.getPlayerRef().sendMessage(Message.raw("=== Vos Titres (" + data.getUnlockedTitles().size() + ") ==="));
                sender.getPlayerRef().sendMessage(Message.raw("Tous les bonus se cumulent automatiquement."));
                String currentTitleId = data.getCurrentTitle();
                for (String titleId : data.getUnlockedTitles()) {
                    TitleModel title = TitleManager.get(titleId);
                    if (title != null) {
                        String prefix = titleId.equals(currentTitleId) ? "[AFFICHE] " : " - ";
                        sender.getPlayerRef().sendMessage(Message.raw(prefix + title.getFormattedName() + " (" + title.getId() + ")"));
                    } else {
                        sender.getPlayerRef().sendMessage(Message.raw(" - " + titleId + " (inconnu)"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }
}