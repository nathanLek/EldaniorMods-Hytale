package com.eldanior.system.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;

import java.lang.reflect.Field; // Nécessaire pour l'accès propre à l'UUID
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class SetLevelCommand extends AbstractAsyncCommand {

    // Déclaration propre des arguments (Comme AddXPCommand)
    private final RequiredArg<String> playerArg;
    private final RequiredArg<Integer> levelArg;

    public SetLevelCommand() {
        super("setlevel", "Définir le niveau d'un joueur");
        // Initialisation propre
        this.playerArg = this.withRequiredArg("joueur", "Nom du joueur", ArgTypes.STRING);
        this.levelArg = this.withRequiredArg("niveau", "Niveau", ArgTypes.INTEGER);
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {

        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        if (!sender.hasPermission("eldanior.command.setlevel")) {
            sender.sendMessage(Message.raw("Erreur : Pas de permission."));
            return CompletableFuture.completedFuture(null);
        }

        // Récupération des valeurs
        String playerName = this.playerArg.get(ctx);
        int level = this.levelArg.get(ctx);

        // 1. Récupération du Ticket (Ref)
        PlayerRef targetRef = Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT_IGNORE_CASE);

        if (targetRef == null) {
            sender.sendMessage(Message.raw("Erreur : Joueur introuvable."));
            return CompletableFuture.completedFuture(null);
        }

        // 2. Exécution sur le Thread Principal (Pour éviter les crashs)
        assert sender.getWorld() != null;
        return CompletableFuture.runAsync(() -> {
            try {
                Field uuidField = PlayerRef.class.getDeclaredField("uuid");
                uuidField.setAccessible(true);
                UUID targetUUID = (UUID) uuidField.get(targetRef);

                // On récupère l'entité Player directement via son UUID
                PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);

                if (targetPlayer == null) {
                    sender.sendMessage(Message.raw("Erreur : Le joueur doit être connecté."));
                    return;
                }

                // 3. Modification des données sur le Player récupéré
                var ref = targetPlayer.getReference();
                assert ref != null;
                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) data = new PlayerLevelData();

                data.setLevel(level);
                data.setExperience(0);
                data.setAttributePoints((level - 1) * 5);
                store.putComponent(ref, type, data);

                // Feedback
                sender.sendMessage(Message.raw("Niveau défini sur " + level + " pour " + playerName));
                targetPlayer.sendMessage(Message.raw("Votre niveau a été changé à : " + level));

            } catch (Exception e) {
                sender.sendMessage(Message.raw("Erreur technique : " + e.getMessage()));
                e.printStackTrace();
            }
        }, sender.getWorld());
    }
}