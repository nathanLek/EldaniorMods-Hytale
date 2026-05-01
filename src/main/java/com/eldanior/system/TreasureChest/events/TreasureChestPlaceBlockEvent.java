package com.eldanior.system.TreasureChest.events;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.TreasureChest.resources.TreasureChestTemplate;
import com.eldanior.system.config.configs.LootTableConfig;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreasureChestPlaceBlockEvent extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    // Associe un itemId de coffre à l'ID de sa loot table (null = coffre custom vide)
    // Pour ajouter un nouveau type de coffre : AUTO_REGISTER_CHESTS.put("MonCoffre", "ma_loot_table");
    private static final Map<String, String> AUTO_REGISTER_CHESTS = new HashMap<>();
    private static final Map<String, Integer> chestCounts = new java.util.concurrent.ConcurrentHashMap<>();

    static {
        AUTO_REGISTER_CHESTS.put("ChestTreasureDefault", "default");
        AUTO_REGISTER_CHESTS.put("ChestTreasureDungeonDefault", "donjon_common");
        AUTO_REGISTER_CHESTS.put("ChestTreasureGold", "gold");
        AUTO_REGISTER_CHESTS.put("ChestTreasureDungeon", "donjon");
        AUTO_REGISTER_CHESTS.put("ChestTreasureLegendary", "legendary");
    }

    public static int getChestCount(String lootTableId) {
        return chestCounts.getOrDefault(lootTableId, 0);
    }

    public static int getTotalChestCount() {
        int total = 0;
        for (int c : chestCounts.values()) total += c;
        return total;
    }

    public TreasureChestPlaceBlockEvent() {
        super(PlaceBlockEvent.class);
    }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl PlaceBlockEvent event) {
        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        if (player == null) return;

        ItemStack item = event.getItemInHand();
        if (item == null || !item.getItemId().toLowerCase().contains("chest")) return;

        Vector3i pos = event.getTargetBlock();
        World world = player.getWorld();

        if (isProtectedChest(world, pos.getX() + 1, pos.getY(), pos.getZ()) ||
                isProtectedChest(world, pos.getX() - 1, pos.getY(), pos.getZ()) ||
                isProtectedChest(world, pos.getX(), pos.getY(), pos.getZ() + 1) ||
                isProtectedChest(world, pos.getX(), pos.getY(), pos.getZ() - 1)) {
            event.setCancelled(true);
            return;
        }

        // Auto-enregistrement si l'item posé est un coffre au trésor reconnu
        String itemId = item.getItemId();
        if (AUTO_REGISTER_CHESTS.containsKey(itemId)) {
            autoRegisterChest(player, world, pos, AUTO_REGISTER_CHESTS.get(itemId));
        }
    }

    private void autoRegisterChest(Player player, World world, Vector3i pos, String lootTableId) {
        // Incrementer le compteur de coffres
        if (lootTableId != null) {
            chestCounts.merge(lootTableId, 1, Integer::sum);
        }

        TreasureChestTemplate template = world.getChunkStore().getStore().getResource(EldaniorSystem.CHEST_TEMPLATE_TYPE);
        if (template == null) return;

        List<ItemStack> loot = new ArrayList<>();
        String dropListName;

        if (lootTableId != null) {
            LootTableConfig table;
            try {
                table = LootTableConfig.getById(lootTableId);
            } catch (Exception e) {
                table = LootTableConfig.DEFAULT;
            }
            loot = table.generateLoot(pos.hashCode() + world.getWorldConfig().getSeed());
            dropListName = table.getTableId();
        } else {
            dropListName = "custom";
        }

        template.saveTemplate(pos.getX(), pos.getY(), pos.getZ(), loot, dropListName);
        player.sendMessage(Message.raw("§2✔ [Eldanior] Coffre au trésor enregistré automatiquement !"));
    }

    // ✅ Update 4 : BlockModule.getComponent() remplace player.getWorld().getState()
    // Le World est passé en paramètre pour éviter de l'appeler N fois via player.getWorld()
    private boolean isProtectedChest(World world, int x, int y, int z) {
        ItemContainerBlock container = BlockModule.getComponent(
                ItemContainerBlock.getComponentType(),
                world, x, y, z
        );

        if (container == null) return false;

        // ✅ Le template est récupéré directement depuis le ChunkStore du World
        TreasureChestTemplate template = world.getChunkStore().getStore().getResource(EldaniorSystem.CHEST_TEMPLATE_TYPE);
        return template != null && template.hasTemplate(x, y, z);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}