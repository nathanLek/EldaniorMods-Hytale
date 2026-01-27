package com.eldanior.system.Leveling.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.components.PlayerLevelData;
import com.eldanior.system.Leveling.utils.StatCalculator;
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
                data.setLevel(level);
                data.setExperience(0);
                data.setStrength(1);
                data.setVitality(1);
                data.setAgility(1);
                data.setIntelligence(1);
                data.setEndurance(1);
                data.setLuck(1);
                data.setMoney(1000);

                if (level <= 1) {
                    data.setAttributePoints(0);
                } else {
                    data.setAttributePoints((level - 1) * 3);
                }

                StatCalculator.updatePlayerStats(ref, store, data);

                store.putComponent(ref, type, data);

                sender.sendMessage(Message.raw("Niveau défini sur " + level + " pour " + playerName + " (Stats réinitialisées)."));
                targetPlayer.sendMessage(Message.raw("Votre niveau a été changé à : " + level + ". Vos stats ont été réinitialisées."));

            } catch (Exception e) {
                sender.sendMessage(Message.raw("Erreur technique : " + e.getMessage()));
                e.printStackTrace();
            }
        }, sender.getWorld());
    }
}