package com.eldanior.system.TreasureChest.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.TreasureChest.resources.TreasureChestTemplate;
import com.eldanior.system.config.configs.LootTableConfig;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GenerateTreasureCommand extends AbstractPlayerCommand {

    private final OptionalArg<String> lootTableArg;

    public GenerateTreasureCommand() {
        super("generatetreasure", "Transforme le coffre visé en coffre au trésor Eldanior");
        this.lootTableArg = this.withOptionalArg("loottable", "ID de la LootTable", ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player executor = store.getComponent(ref, Player.getComponentType());
        if (executor == null) return;

        Vector3i targetPos = TargetUtil.getTargetBlock(ref, 10.0, store);
        if (targetPos == null) {
            executor.sendMessage(Message.raw("§c[Eldanior] Erreur : Vous ne regardez aucun bloc."));
            return;
        }

        // ✅ Update 4 : BlockModule.getComponent() remplace world.getState()
        ItemContainerBlock container = BlockModule.getComponent(
                ItemContainerBlock.getComponentType(),
                world,
                targetPos.getX(), targetPos.getY(), targetPos.getZ()
        );

        if (container == null) {
            executor.sendMessage(Message.raw("§c[Eldanior] Erreur : Le bloc ciblé n'est pas un coffre."));
            return;
        }

        TreasureChestTemplate template = world.getChunkStore().getStore().getResource(EldaniorSystem.CHEST_TEMPLATE_TYPE);

        boolean isUpdate = template.hasTemplate(targetPos.getX(), targetPos.getY(), targetPos.getZ());
        List<ItemStack> finalLoot = new ArrayList<>();
        String dropListName;

        if (this.lootTableArg.provided(commandContext)) {
            String tableIdInput = this.lootTableArg.get(commandContext);

            LootTableConfig table;
            try {
                table = LootTableConfig.getById(tableIdInput);
            } catch (Exception e) {
                table = LootTableConfig.DEFAULT;
            }

            if (table.getTableId().equalsIgnoreCase(tableIdInput)) {
                finalLoot = table.generateLoot(targetPos.hashCode() + world.getWorldConfig().getSeed());
                dropListName = table.getTableId();
                long cooldownMins = table.getCooldownMillis() / 60000;
                executor.sendMessage(Message.raw("§a[Eldanior] Table associée : §6" + dropListName + " §7(Recharge: " + cooldownMins + " min)"));
            } else {
                executor.sendMessage(Message.raw("§c[Eldanior] La table §f" + tableIdInput + " §cn'existe pas."));
                StringBuilder availableTables = new StringBuilder("§7Tables disponibles : §f");
                for (LootTableConfig ltc : LootTableConfig.values()) {
                    if (ltc != LootTableConfig.DEFAULT) availableTables.append(ltc.getTableId()).append(", ");
                }
                String tablesList = availableTables.length() > 21
                        ? availableTables.substring(0, availableTables.length() - 2)
                        : availableTables.toString();
                executor.sendMessage(Message.raw(tablesList));
                return;
            }
        } else {
            // ✅ Update 4 : container.getItemContainer() via ItemContainerBlock
            for (short i = 0; i < container.getCapacity(); i++) {
                ItemStack stack = container.getItemContainer().getItemStack(i);
                if (stack != null) finalLoot.add(stack);
            }
            dropListName = "custom";

            if (finalLoot.isEmpty()) {
                executor.sendMessage(Message.raw("§e[Eldanior] Attention : Coffre CUSTOM enregistré sans objets."));
            } else {
                executor.sendMessage(Message.raw("§d[Eldanior] Coffre CUSTOM enregistré (" + finalLoot.size() + " objets)."));
            }
        }

        template.saveTemplate(targetPos.getX(), targetPos.getY(), targetPos.getZ(), finalLoot, dropListName);
        container.getItemContainer().clear();

        executor.sendMessage(isUpdate
                ? Message.raw("§e✔ Coffre au trésor mis à jour !")
                : Message.raw("§2✔ Coffre au trésor créé avec succès !"));
    }
}