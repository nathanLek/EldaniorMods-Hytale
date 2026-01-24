package com.eldanior.system.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.utils.NotificationHelper; // Je garde tes notifs stylées
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.lang.reflect.Field; // Nécessaire pour l'accès propre à l'UUID
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AddXPCommand extends AbstractAsyncCommand {

    // Déclaration propre des arguments (Exactement comme ton SetLevelCommand)
    private final RequiredArg<String> playerArg;
    private final RequiredArg<Integer> amountArg;

    public AddXPCommand() {
        super("addxp", "Ajouter de l'expérience à un joueur");

        // Initialisation propre des arguments
        this.playerArg = this.withRequiredArg("joueur", "Nom du joueur", ArgTypes.STRING);
        this.amountArg = this.withRequiredArg("nombre", "Quantité d'XP", ArgTypes.INTEGER);
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {

        // 1. Vérification que c'est un joueur
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        // 2. Vérification Permission (Manuelle comme dans ton exemple)
        if (!sender.hasPermission("eldanior.command.addxp")) {
            // J'utilise ton NotificationHelper car c'est plus joli que Message.raw
            sender.sendMessage(Message.raw( "Erreur : Pas de permission."));
            return CompletableFuture.completedFuture(null);
        }

        // 3. Récupération des valeurs
        String playerName = this.playerArg.get(ctx);
        int amount = this.amountArg.get(ctx);

        // Validation simple
        if (amount <= 0) {
            sender.sendMessage(Message.raw( "Erreur : Le montant doit être positif."));
            return CompletableFuture.completedFuture(null);
        }

        // 4. Récupération du Ticket (Ref) initial
        PlayerRef targetRef = Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT_IGNORE_CASE);

        if (targetRef == null) {
            sender.sendMessage(Message.raw("Erreur : Joueur introuvable."));
            return CompletableFuture.completedFuture(null);
        }

        // 5. Exécution sur le Thread Principal (La méthode ROBUSTE de ton exemple)
        assert sender.getWorld() != null;
        return CompletableFuture.runAsync(() -> {
            try {
                // --- LA TECHNIQUE DE REFLEXION (La clé pour que ça marche) ---
                Field uuidField = PlayerRef.class.getDeclaredField("uuid");
                uuidField.setAccessible(true);
                UUID targetUUID = (UUID) uuidField.get(targetRef);

                // On récupère l'entité Player connectée via son UUID
                PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);

                if (targetPlayer == null) {
                    sender.sendMessage(Message.raw( "Erreur : Le joueur doit être connecté."));
                    return;
                }

                // 6. Modification des données ECS
                var ref = targetPlayer.getReference();
                if (ref == null) return;

                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) data = new PlayerLevelData();

                int oldLvl = data.getLevel();

                // --- ACTION : AJOUT D'XP ---
                data.addExperience(amount);

                // Sauvegarde
                store.putComponent(ref, type, data);

                // 7. Feedback (Messages)
                String msgTarget = "Reçu : <color:green>+" + amount + " XP</color> (Admin)";
                NotificationHelper.sendNotification(targetPlayer, msgTarget, NotificationStyle.Success);

                if (data.getLevel() > oldLvl) {
                    NotificationHelper.showLevelUpTitle(targetPlayer, data.getLevel());
                }

                String msgSender = "Donné : <color:green>" + amount + " XP</color> à <color:yellow>" + targetPlayer.getUsername() + "</color>";
                sender.sendMessage(Message.raw( msgSender));

            } catch (Exception e) {
                sender.sendMessage(Message.raw( "Erreur technique : " + e.getMessage()));
                e.printStackTrace();
            }
        }, sender.getWorld());
    }
}