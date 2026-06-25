package com.eldanior.system.TreasureChest.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.TreasureChest.components.PlayerChestData;
import com.eldanior.system.TreasureChest.resources.TreasureChestTemplate;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.playerdata.PlayerStorage;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;

public class DeleteTreasureCommand extends AbstractPlayerCommand {

    public DeleteTreasureCommand() {
        super("deletetreasure", "Supprime un coffre au trésor Eldanior");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player executor = store.getComponent(ref, Player.getComponentType());
        if (executor == null) return;

        // Vérification permission admin
        if (!playerRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
            executor.getPlayerRef().sendMessage(Message.raw("Vous n'avez pas la permission d'utiliser cette commande."));
            return;
        }

        Vector3i pos = TargetUtil.getTargetBlock(ref, 10.0, store);
        if (pos == null) {
            executor.getPlayerRef().sendMessage(Message.raw("Regardez le coffre que vous voulez supprimer."));
            return;
        }

        // ✅ Update 4 : BlockModule.getComponent() remplace world.getState()
        ItemContainerBlock container = BlockModule.getComponent(
                ItemContainerBlock.getComponentType(),
                world,
                pos.x(), pos.y(), pos.z()
        );

        if (container == null) {
            executor.getPlayerRef().sendMessage(Message.raw("Ce bloc n'est pas un conteneur."));
            return;
        }

        TreasureChestTemplate template = world.getChunkStore().getStore().getResource(EldaniorSystem.CHEST_TEMPLATE_TYPE);
        if (template != null && template.hasTemplate(pos.x(), pos.y(), pos.z())) {
            template.removeTemplate(pos.x(), pos.y(), pos.z());
            cleanupPlayerData(executor, pos, world.getName(), store);
            executor.getPlayerRef().sendMessage(Message.raw("Le coffre au trésor a été supprimé avec succès."));
        } else {
            executor.getPlayerRef().sendMessage(Message.raw("Ce coffre n'est pas enregistré comme un coffre au trésor."));
        }
    }

    private void cleanupPlayerData(Player executor, Vector3i pos, String worldName, Store<EntityStore> store) {
        PlayerStorage storage = Universe.get().getPlayerStorage();
        Set<UUID> allPlayers;

        try {
            allPlayers = storage.getPlayers();
        } catch (Exception e) {
            executor.getPlayerRef().sendMessage(Message.raw("[Avertissement] Nettoyage partiel : impossible de lire la liste des joueurs hors-ligne."));
            return;
        }

        for (UUID uuid : allPlayers) {
            PlayerRef targetRef = Universe.get().getPlayer(uuid);

            if (targetRef != null && targetRef.isValid()) {
                PlayerChestData data = store.getComponent(targetRef.getReference(), EldaniorSystem.get().getPlayerChestDataType());
                if (data != null) {
                    data.resetChest(pos.x(), pos.y(), pos.z(), worldName);
                }
            } else {
                storage.load(uuid).thenAccept(holder -> {
                    if (holder != null) {
                        PlayerChestData data = holder.getComponent(EldaniorSystem.get().getPlayerChestDataType());
                        if (data != null) {
                            data.resetChest(pos.x(), pos.y(), pos.z(), worldName);
                            storage.save(uuid, holder, true);
                        }
                    }
                });
            }
        }
    }
}