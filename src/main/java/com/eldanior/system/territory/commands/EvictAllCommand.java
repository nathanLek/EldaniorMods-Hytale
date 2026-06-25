package com.eldanior.system.territory.commands;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.economy.TransactionLogger;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Commande admin : /es evictall
 * Evince tous les locataires dont la location a expire (ignore la periode de grace).
 */
public class EvictAllCommand extends AbstractAsyncCommand {

    public EvictAllCommand() {
        super("evictall", "Evincer tous les locataires expires (admin)");
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

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef == null) return;

                if (!playerRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
                    playerRef.sendMessage(Message.raw("Vous n'avez pas la permission."));
                    return;
                }

                // Collecter les parcelles a evincer
                List<String> evicted = new ArrayList<>();
                for (ParcelData parcel : ParcelManager.getAll()) {
                    if (parcel.isRentExpired()) {
                        String name = parcel.getName();
                        String id = parcel.getId();
                        ParcelManager.evict(id);
                        TransactionLogger.logRentEviction(name, id);
                        evicted.add(name);
                    }
                }

                if (evicted.isEmpty()) {
                    playerRef.sendMessage(Message.raw("Aucune location expiree a evincer."));
                } else {
                    playerRef.sendMessage(Message.raw("" + evicted.size() + " locataire(s) evince(s) :"));
                    for (String name : evicted) {
                        playerRef.sendMessage(Message.raw("  - " + name));
                    }
                }
            } catch (Exception e) {
                EldaniorLogger.error("EvictAllCommand", e);
            }
        }, world);
    }
}
