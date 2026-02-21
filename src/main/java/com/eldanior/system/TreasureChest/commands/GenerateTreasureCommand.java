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
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"deprecation", "removal", "unchecked", "ConstantConditions"})
public class GenerateTreasureCommand extends AbstractPlayerCommand {
    private final OptionalArg<String> lootTableArg;

    public GenerateTreasureCommand() {
        super("generatetr", "Transforme le coffre visé en coffre au trésor Eldanior");
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

        BlockState state = world.getState(targetPos.getX(), targetPos.getY(), targetPos.getZ(), true);
        if (!(state instanceof ItemContainerState containerState)) {
            executor.sendMessage(Message.raw("§c[Eldanior] Erreur : Le bloc ciblé n'est pas un coffre."));
            return;
        }

        TreasureChestTemplate template = world.getChunkStore().getStore().getResource(EldaniorSystem.CHEST_TEMPLATE_TYPE);
        if (template == null) return;

        boolean isUpdate = template.hasTemplate(targetPos.getX(), targetPos.getY(), targetPos.getZ());
        List<ItemStack> finalLoot = new ArrayList<>();
        String dropListName = "undefined";

        // --- MODE : TABLE DE LOOT ---
        if (this.lootTableArg.provided(commandContext)) {
            // Hytale s'occupe de parser le flag --loottable= et nous donne juste la valeur
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
                    if (ltc != LootTableConfig.DEFAULT) {
                        availableTables.append(ltc.getTableId()).append(", ");
                    }
                }
                String tablesList = availableTables.length() > 21 ? availableTables.substring(0, availableTables.length() - 2) : availableTables.toString();
                executor.sendMessage(Message.raw(tablesList));
                return;
            }
        }
        // --- MODE : COFFRE CUSTOM ---
        else {
            for (short i = 0; i < containerState.getItemContainer().getCapacity(); i++) {
                ItemStack stack = containerState.getItemContainer().getItemStack(i);
                if (stack != null) finalLoot.add(stack);
            }
            dropListName = "custom";

            if (finalLoot.isEmpty()) {
                executor.sendMessage(Message.raw("§e[Eldanior] Attention : Coffre CUSTOM enregistré sans objets à l'intérieur."));
            } else {
                executor.sendMessage(Message.raw("§d[Eldanior] Coffre CUSTOM enregistré (" + finalLoot.size() + " objets)."));
            }
        }

        // Sauvegarde dans le Template
        template.saveTemplate(targetPos.getX(), targetPos.getY(), targetPos.getZ(), finalLoot, dropListName);

        // Nettoyage du coffre physique
        containerState.getItemContainer().clear();

        // Message de validation
        if (isUpdate) {
            executor.sendMessage(Message.raw("§e✔ Coffre au trésor mis à jour !"));
        } else {
            executor.sendMessage(Message.raw("§2✔ Coffre au trésor créé avec succès !"));
        }
    }
}