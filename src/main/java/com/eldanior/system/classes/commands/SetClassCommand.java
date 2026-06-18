package com.eldanior.system.classes.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
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
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SetClassCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> classIdArg;

    public SetClassCommand() {
        super("setclass", "Définir la classe RPG d'un joueur");
        this.playerArg = this.withRequiredArg("joueur", "Nom du joueur", ArgTypes.STRING);
        this.classIdArg = this.withRequiredArg("classId", "ID de la classe (ex: warrior)", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return true; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {

        Ref<EntityStore> senderEntityRef = ctx.senderAsPlayerRef();
        if (senderEntityRef == null || !senderEntityRef.isValid()) return CompletableFuture.completedFuture(null);

        Store<EntityStore> senderEntityStore = senderEntityRef.getStore();
        World world = ((EntityStore) senderEntityStore.getExternalData()).getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);

        String targetName = this.playerArg.get(ctx);
        String classId = this.classIdArg.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = senderEntityStore.getComponent(senderEntityRef, PlayerRef.getComponentType());
                Player sender = senderEntityStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                if (!senderRef.hasPermission("eldanior.command.setclass")) {
                    senderRef.sendMessage(Message.raw("§cErreur : Pas de permission."));
                    return;
                }

                PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
                if (targetRef == null) {
                    senderRef.sendMessage(Message.raw("§cErreur : Joueur '" + targetName + "' introuvable."));
                    return;
                }

                UUID targetUUID = UUIDExtractor.getUUID(targetRef);

                PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
                if (targetPlayer == null) {
                    sender.getPlayerRef().sendMessage(Message.raw("§cErreur : Le joueur doit être connecté."));
                    return;
                }

                var ref = targetPlayer.getReference();
                if (ref == null) return;

                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData data = store.getComponent(ref, type);

                if (data == null) data = new PlayerLevelData();

                ClassModel model = ClassManager.get(classId.toLowerCase());
                if (model == null) {
                    sender.getPlayerRef().sendMessage(Message.raw("§cErreur : Classe '" + classId + "' inconnue."));
                    sender.getPlayerRef().sendMessage(Message.raw("§7IDs réellement chargés : " + ClassManager.getAvailableIds()));
                    return;
                }

                data.setPlayerClass(model.getDisplayName());
                data.setPlayerClassId(model.getId());
                // Classe Dragon : dignité de base à 1000
                if ("dragon".equalsIgnoreCase(model.getId())) {
                    data.setDignity(1000);
                }
                // Persiste d'abord, calcule ensuite
                store.putComponent(ref, type, data);
                StatCalculator.updatePlayerStats(ref, store, data);

                sender.getPlayerRef().sendMessage(Message.raw("§aSuccès : " + targetName + " est maintenant " + model.getDisplayName() + " (Skills mis à jour)"));
                targetPlayer.sendMessage(Message.raw("§eVotre classe a été changée en : §6" + model.getDisplayName()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }
}
