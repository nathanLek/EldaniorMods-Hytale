package com.eldanior.system.TreasureChest.events;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.TreasureChest.resources.TreasureChestTemplate;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class TreasureChestPlaceBlockEvent extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

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
        }
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