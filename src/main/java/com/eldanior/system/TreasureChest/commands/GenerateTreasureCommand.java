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
            executor.sendMessage(Message.raw("§c[Eldanior] Erreur : Aucun coffre visé."));
            return;
        }

        BlockState state = world.getState(targetPos.getX(), targetPos.getY(), targetPos.getZ(), true);
        if (!(state instanceof ItemContainerState containerState)) {
            executor.sendMessage(Message.raw("§c[Eldanior] Erreur : Ce n'est pas un conteneur d'objets."));
            return;
        }

        TreasureChestTemplate template = world.getChunkStore().getStore().getResource(EldaniorSystem.CHEST_TEMPLATE_TYPE);
        List<ItemStack> finalLoot = new ArrayList<>();
        String dropListName = "undefined";

        // --- DÉBUT DE LA LOGIQUE ---
        if (this.lootTableArg.provided(commandContext)) {
            String tableIdInput = this.lootTableArg.get(commandContext);

            // On cherche dans l'Enum
            LootTableConfig table = LootTableConfig.getById(tableIdInput);

            // Vérification de sécurité : Est-ce qu'on a bien trouvé la table ou est-ce le fallback DEFAULT ?
            if (table.getTableId().equalsIgnoreCase(tableIdInput)) {
                // Succès : La table correspond à l'entrée
                finalLoot = table.generateLoot(targetPos.hashCode() + world.getWorldConfig().getSeed());
                dropListName = table.getTableId();
                executor.sendMessage(Message.raw("§a[Eldanior] Table détectée : §6" + dropListName));
            } else {
                // Échec : getById a renvoyé DEFAULT parce qu'il n'a pas trouvé l'ID
                executor.sendMessage(Message.raw("§c[Eldanior] Erreur : La table §f" + tableIdInput + " §cn'existe pas dans LootTableConfig."));
                return;
            }
        }

        // Si aucun argument n'est fourni, on peut garder la logique "Custom" (ce qu'il y a dans le coffre)
        if (!this.lootTableArg.provided(commandContext)) {
            for (short i = 0; i < containerState.getItemContainer().getCapacity(); i++) {
                ItemStack stack = containerState.getItemContainer().getItemStack(i);
                if (stack != null) finalLoot.add(stack);
            }
            dropListName = "custom";
            executor.sendMessage(Message.raw("§a[Eldanior] Création d'un coffre §dCUSTOM§a."));
        }

        // Sauvegarde (Modification par référence du Template)
        template.saveTemplate(targetPos.getX(), targetPos.getY(), targetPos.getZ(), finalLoot, dropListName);

        // On vide le coffre physique
        containerState.getItemContainer().clear();

        executor.sendMessage(Message.raw("§2✔ Coffre au trésor enregistré !"));
    }
}