package com.eldanior.system.TreasureChest.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.TreasureChest.components.PlayerChestData;
import com.eldanior.system.TreasureChest.resources.TreasureChestTemplate;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
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

        Vector3i pos = TargetUtil.getTargetBlock(ref, 10.0, store);
        if (pos == null) {
            executor.sendMessage(Message.raw("§cRegardez le coffre que vous voulez supprimer."));
            return;
        }

        // ✅ Update 4 : BlockModule.getComponent() remplace world.getState()
        ItemContainerBlock container = BlockModule.getComponent(
                ItemContainerBlock.getComponentType(),
                world,
                pos.getX(), pos.getY(), pos.getZ()
        );

        if (container == null) {
            executor.sendMessage(Message.raw("§cCe bloc n'est pas un conteneur."));
            return;
        }

        TreasureChestTemplate template = world.getChunkStore().getStore().getResource(EldaniorSystem.CHEST_TEMPLATE_TYPE);
        if (template != null && template.hasTemplate(pos.getX(), pos.getY(), pos.getZ())) {
            template.removeTemplate(pos.getX(), pos.getY(), pos.getZ());
            cleanupPlayerData(executor, pos, world.getName(), store);
            executor.sendMessage(Message.raw("§aLe coffre au trésor a été supprimé avec succès."));
        } else {
            executor.sendMessage(Message.raw("§eCe coffre n'est pas enregistré comme un coffre au trésor."));
        }
    }

    private void cleanupPlayerData(Player executor, Vector3i pos, String worldName, Store<EntityStore> store) {
        PlayerStorage storage = Universe.get().getPlayerStorage();
        Set<UUID> allPlayers;

        try {
            allPlayers = storage.getPlayers();
        } catch (Exception e) {
            executor.sendMessage(Message.raw("§6[Avertissement] Nettoyage partiel : impossible de lire la liste des joueurs hors-ligne."));
            return;
        }

        for (UUID uuid : allPlayers) {
            PlayerRef targetRef = Universe.get().getPlayer(uuid);

            if (targetRef != null && targetRef.isValid()) {
                PlayerChestData data = store.getComponent(targetRef.getReference(), EldaniorSystem.get().getPlayerChestDataType());
                if (data != null) {
                    data.resetChest(pos.getX(), pos.getY(), pos.getZ(), worldName);
                }
            } else {
                storage.load(uuid).thenAccept(holder -> {
                    if (holder != null) {
                        PlayerChestData data = holder.getComponent(EldaniorSystem.get().getPlayerChestDataType());
                        if (data != null) {
                            data.resetChest(pos.getX(), pos.getY(), pos.getZ(), worldName);
                            storage.save(uuid, holder);
                        }
                    }
                });
            }
        }
    }
}