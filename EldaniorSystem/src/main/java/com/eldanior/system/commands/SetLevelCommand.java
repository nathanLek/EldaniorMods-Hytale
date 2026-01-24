package com.eldanior.system.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.utils.StatCalculator; // ✅ Import INDISPENSABLE
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

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class SetLevelCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> playerArg;
    private final RequiredArg<Integer> levelArg;

    public SetLevelCommand() {
        super("setlevel", "Définir le niveau d'un joueur");
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

        String playerName = this.playerArg.get(ctx);
        int level = this.levelArg.get(ctx);

        PlayerRef targetRef = Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT_IGNORE_CASE);

        if (targetRef == null) {
            sender.sendMessage(Message.raw("Erreur : Joueur introuvable."));
            return CompletableFuture.completedFuture(null);
        }

        assert sender.getWorld() != null;
        return CompletableFuture.runAsync(() -> {
            try {
                Field uuidField = PlayerRef.class.getDeclaredField("uuid");
                uuidField.setAccessible(true);
                UUID targetUUID = (UUID) uuidField.get(targetRef);

                PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);

                if (targetPlayer == null) {
                    sender.sendMessage(Message.raw("Erreur : Le joueur doit être connecté."));
                    return;
                }

                var ref = targetPlayer.getReference();
                assert ref != null;
                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) data = new PlayerLevelData();

                // --- MODIFICATIONS ---

                // 1. On définit le niveau
                data.setLevel(level);
                data.setExperience(0);

                // 2. On remet toutes les stats à 1 (valeur de base "Humain")
                // Comme ça le joueur peut redistribuer ses points proprement
                data.setStrength(1);
                data.setVitality(1);
                data.setAgility(1);
                data.setIntelligence(1);
                data.setEndurance(1);
                data.setLuck(1);
                data.setMoney(1000);

                // 3. On calcule les points disponibles
                if (level <= 1) {
                    data.setAttributePoints(0);
                } else {
                    // 5 points par niveau gagné
                    data.setAttributePoints((level - 1) * 3);
                }

                // 4. IMPORTANT : On applique les changements au moteur (PV, Mana)
                // Sans ça, le joueur garde ses PV max jusqu'à sa prochaine connexion
                StatCalculator.updatePlayerStats(ref, store, data);

                // 5. Sauvegarde
                store.putComponent(ref, type, data);

                // Feedback
                sender.sendMessage(Message.raw("Niveau défini sur " + level + " pour " + playerName + " (Stats réinitialisées)."));
                targetPlayer.sendMessage(Message.raw("Votre niveau a été changé à : " + level + ". Vos stats ont été réinitialisées."));

            } catch (Exception e) {
                sender.sendMessage(Message.raw("Erreur technique : " + e.getMessage()));
                e.printStackTrace();
            }
        }, sender.getWorld());
    }
}