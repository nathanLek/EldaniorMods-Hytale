package com.eldanior.system.TreasureChest.commands;


import com.eldanior.system.TreasureChest.pages.TreasureChestConfigPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class TreasureConfigCommand extends AbstractPlayerCommand {

    public TreasureConfigCommand() {
        // Commande : /treasureconfig (réservée aux admins par défaut dans Hytale si configurée)
        super("treasureconfig", "Ouvre la page de configuration des coffres Eldanior");
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player != null) {
            // Création de l'instance de la page UI
            TreasureChestConfigPage page = new TreasureChestConfigPage(playerRef);

            // Ouverture de la page via le PageManager du joueur
            player.getPageManager().openCustomPage(ref, store, page);
        }
    }
}