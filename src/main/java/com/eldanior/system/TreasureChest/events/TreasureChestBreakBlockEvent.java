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
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

@SuppressWarnings({"deprecation", "removal", "ConstantConditions"})
public class TreasureChestBreakBlockEvent extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    public TreasureChestBreakBlockEvent() {
        super(BreakBlockEvent.class);
    }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl BreakBlockEvent event) {
        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        if (player == null) return;

        Vector3i target = event.getTargetBlock();

        // 1. Protection du bloc cible (le coffre lui-même)
        if (isProtectedChest(player, target.getX(), target.getY(), target.getZ())) {
            if (shouldCancelBreak(player, target.getX(), target.getY(), target.getZ())) {
                event.setCancelled(true);
                return;
            }
        } else {
            // 2. Protection des supports (Y+1)
            // Empêche de casser un bloc si un Treasure Chest est posé dessus
            if (isProtectedChest(player, target.getX(), target.getY() + 1, target.getZ()) ||
                    isProtectedChest(player, target.getX() + 1, target.getY() + 1, target.getZ()) ||
                    isProtectedChest(player, target.getX(), target.getY() + 1, target.getZ() + 1)) {

                event.setCancelled(true);
            }
        }
    }

    private boolean shouldCancelBreak(Player player, int x, int y, int z) {
        TreasureChestConfig config = player.getWorld().getChunkStore().getStore().getResource(EldaniorSystem.CONFIG_RESOURCE_TYPE);
        com.hypixel.hytale.server.core.universe.world.meta.BlockState blockState = player.getWorld().getState(x, y, z, true);

        if (config != null && config.isCanPlayerBreakLootChests() && blockState instanceof ItemContainerState containerState) {
            // On autorise la casse SEULEMENT si personne n'a d'inventaire ouvert sur ce coffre
            return !containerState.getWindows().isEmpty();
        }
        return true;
    }

    private boolean isProtectedChest(Player player, int x, int y, int z) {
        com.hypixel.hytale.server.core.universe.world.meta.BlockState blockState = player.getWorld().getState(x, y, z, true);
        if (blockState instanceof ItemContainerState containerState) {
            TreasureChestTemplate template = containerState.getReference().getStore().getResource(EldaniorSystem.CHEST_TEMPLATE_TYPE);
            return template != null && template.hasTemplate(x, y, z);
        }
        return false;
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}