package com.eldanior.system.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.rpg.classes.ClassManager;
import com.eldanior.system.rpg.classes.ClassModel;
import com.eldanior.system.utils.StatCalculator;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
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
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SetClassCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> classIdArg;

    public SetClassCommand() {
        super("setclass", "Définir la classe RPG d'un joueur");
        // Exactement comme ton SetLevelCommand : On définit les arguments requis
        this.playerArg = this.withRequiredArg("joueur", "Nom du joueur", ArgTypes.STRING);
        this.classIdArg = this.withRequiredArg("classId", "ID de la classe (ex: warrior)", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return true; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {

        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        // Permission spécifique
        if (!sender.hasPermission("eldanior.command.setclass")) {
            sender.sendMessage(Message.raw("§cErreur : Pas de permission."));
            return CompletableFuture.completedFuture(null);
        }

        // Récupération des arguments
        String targetName = this.playerArg.get(ctx);
        String classId = this.classIdArg.get(ctx);

        // Recherche du joueur (Logique SetLevelCommand)
        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) {
            sender.sendMessage(Message.raw("§cErreur : Joueur '" + targetName + "' introuvable."));
            return CompletableFuture.completedFuture(null);
        }

        assert sender.getWorld() != null;
        return CompletableFuture.runAsync(() -> {
            try {
                // Reflection pour UUID (Ta méthode robuste)
                Field uuidField = PlayerRef.class.getDeclaredField("uuid");
                uuidField.setAccessible(true);
                UUID targetUUID = (UUID) uuidField.get(targetRef);

                PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);

                if (targetPlayer == null) {
                    sender.sendMessage(Message.raw("§cErreur : Le joueur doit être connecté."));
                    return;
                }

                var ref = targetPlayer.getReference();
                if (ref == null) return;

                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData data = store.getComponent(ref, type);

                if (data == null) {
                    sender.sendMessage(Message.raw("§cErreur : Données introuvables."));
                    return;
                }

                // Vérification de la Classe
                ClassModel model = ClassManager.get(classId.toLowerCase());
                if (model == null) {
                    sender.sendMessage(Message.raw("§cErreur : Classe '" + classId + "' inconnue."));

                    // ✅ CORRECTION : On affiche la liste dynamique réelle !
                    // Si cette liste est vide à l'écran, c'est que le ClassManager.init() n'a pas marché.
                    sender.sendMessage(Message.raw("§7IDs réellement chargés : " + ClassManager.getAvailableIds()));
                    return;
                }

                // --- APPLICATION ---
                data.setPlayerClass(model.getDisplayName());
                data.setPlayerClassId(model.getId());

                // Reset Stats & Bonus (Comme SetLevel reset les stats, ici on applique les nouvelles bases)
                data.setStrength(1 + model.getBonusStr());
                data.setVitality(1 + model.getBonusVit());
                data.setIntelligence(1 + model.getBonusInt());
                data.setEndurance(1 + model.getBonusEnd());
                data.setAgility(1 + model.getBonusAgl());
                data.setLuck(1 + model.getBonusLck());

                // Moteur & Sauvegarde
                StatCalculator.updatePlayerStats(ref, store, data);
                store.putComponent(ref, type, data);

                sender.sendMessage(Message.raw("§aSuccès : " + targetName + " est maintenant un " + model.getDisplayName()));
                targetPlayer.sendMessage(Message.raw("§eVotre classe a été changée en : §6" + model.getDisplayName()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, sender.getWorld());
    }
}