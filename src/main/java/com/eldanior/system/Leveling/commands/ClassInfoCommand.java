package com.eldanior.system.Leveling.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.components.PlayerLevelData;
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

public class ClassInfoCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> playerArg;

    public ClassInfoCommand() {
        super("classinfo", "Voir les infos de classe d'un joueur");
        // Argument unique et requis : le joueur
        this.playerArg = this.withRequiredArg("joueur", "Joueur cible", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return true; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        String targetName = this.playerArg.get(ctx);

        // Même logique de récupération que SetLevelCommand
        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);

        if (targetRef == null) {
            sender.sendMessage(Message.raw("§cJoueur introuvable."));
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

                if (targetPlayer == null) return; // Sécurité

                var ref = targetPlayer.getReference();
                if (ref == null) return;

                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData data = store.getComponent(ref, type);

                if (data != null) {
                    sender.sendMessage(Message.raw("§e--- Info : " + targetName + " ---"));
                    sender.sendMessage(Message.raw("§fClasse : " + data.getPlayerClass()));
                    sender.sendMessage(Message.raw("§fID : §7" + data.getPlayerClassId()));
                    sender.sendMessage(Message.raw("§fNiveau : " + data.getLevel()));
                    sender.sendMessage(Message.raw("§fMana Max : " + (int)data.getMaxMana()));
                } else {
                    sender.sendMessage(Message.raw("§cPas de données pour ce joueur."));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, sender.getWorld());
    }
}