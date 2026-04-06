package com.eldanior.system.TreasureChest.events;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.TreasureChest.resources.TreasureChestConfig;
import com.eldanior.system.TreasureChest.resources.TreasureChestTemplate;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class TreasureChestDamageBlockEvent extends EntityEventSystem<EntityStore, DamageBlockEvent> {

    public TreasureChestDamageBlockEvent() {
        super(DamageBlockEvent.class);
    }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl DamageBlockEvent event) {
        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        if (player == null) return;

        Vector3i target = event.getTargetBlock();
        World world = player.getWorld();

        if (isProtectedChest(world, target.getX(), target.getY(), target.getZ())) {
            assert world != null;
            if (shouldCancelDamage(world, target.getX(), target.getY(), target.getZ())) {
                event.setCancelled(true);
                return;
            }
        }

        if (isProtectedChest(world, target.getX(), target.getY() + 1, target.getZ()) ||
                isProtectedChest(world, target.getX() + 1, target.getY() + 1, target.getZ()) ||
                isProtectedChest(world, target.getX(), target.getY() + 1, target.getZ() + 1)) {
            event.setCancelled(true);
        }
    }

    // ✅ getWindows() est disponible directement sur ItemContainerBlock (confirmé par la décompilation)
    private boolean shouldCancelDamage(World world, int x, int y, int z) {
        TreasureChestConfig config = world.getChunkStore().getStore().getResource(EldaniorSystem.CONFIG_RESOURCE_TYPE);

        ItemContainerBlock container = BlockModule.getComponent(
                ItemContainerBlock.getComponentType(), world, x, y, z
        );

        if (config.isCanPlayerBreakLootChests() && container != null) {
            return !container.getWindows().isEmpty();
        }

        return true;
    }

    // ✅ World passé en paramètre, calculé une seule fois dans handle()
    private boolean isProtectedChest(World world, int x, int y, int z) {
        ItemContainerBlock container = BlockModule.getComponent(
                ItemContainerBlock.getComponentType(), world, x, y, z
        );

        if (container == null) return false;

        TreasureChestTemplate template = world.getChunkStore().getStore().getResource(EldaniorSystem.CHEST_TEMPLATE_TYPE);
        return template.hasTemplate(x, y, z);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}