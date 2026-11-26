package com.eldanior.system.economy.commands;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.economy.PriceEditorPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * Commande admin : /es prix [recherche] — editeur des prix de tous les objets (economy/PriceEditorPage, bareme PriceBook).
 * Les prix modifies sont enregistres dans le dossier du mod (economie/prix_jeu.json) et repris par docs/economie.md.
 */
public class PrixCommand extends AbstractAsyncCommand {

    private final OptionalArg<String> searchArg;

    public PrixCommand() {
        super("prix", "Éditeur des prix de tous les objets (admin)");
        this.searchArg = this.withOptionalArg("recherche", "Nom ou identifiant d'objet", ArgTypes.STRING);
    }

    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        Ref<EntityStore> ref = ctx.senderAsPlayerRef();
        if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);
        Store<EntityStore> store = ref.getStore();
        World world = ((EntityStore) store.getExternalData()).getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);
        String search = this.searchArg.get(ctx);
        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                Player player = store.getComponent(ref, Player.getComponentType());
                if (playerRef == null || player == null) return;
                if (!playerRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
                    playerRef.sendMessage(Message.raw("Vous n'avez pas la permission d'utiliser cette commande."));
                    return;
                }
                player.getPageManager().openCustomPage(ref, store, new PriceEditorPage(playerRef, search));
            } catch (Exception e) {
                EldaniorLogger.error("PrixCommand", e);
            }
        }, world);
    }
}
