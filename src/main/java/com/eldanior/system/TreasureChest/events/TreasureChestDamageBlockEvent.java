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
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

@SuppressWarnings({"deprecation", "removal", "ConstantConditions"})
public class TreasureChestDamageBlockEvent extends EntityEventSystem<EntityStore, DamageBlockEvent> {

    public TreasureChestDamageBlockEvent() {
        super(DamageBlockEvent.class);
    }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl DamageBlockEvent event) {
        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        if (player == null) return;

        Vector3i target = event.getTargetBlock();

        // 1. Vérification directe : Est-ce le coffre lui-même ?
        if (isProtectedChest(player, target.getX(), target.getY(), target.getZ())) {
            if (shouldCancelDamage(player, target.getX(), target.getY(), target.getZ())) {
                event.setCancelled(true);
                return;
            }
        }

        // 2. Vérification indirecte : Est-ce un bloc de support (sous le coffre) ?
        // On vérifie les coffres potentiellement posés au-dessus (Y + 1)
        if (isProtectedChest(player, target.getX(), target.getY() + 1, target.getZ()) ||
                isProtectedChest(player, target.getX() + 1, target.getY() + 1, target.getZ()) ||
                isProtectedChest(player, target.getX(), target.getY() + 1, target.getZ() + 1)) {

            event.setCancelled(true);
        }
    }

    /**
     * Détermine si les dégâts sur le bloc doivent être annulés selon la config.
     */
    private boolean shouldCancelDamage(Player player, int x, int y, int z) {
        TreasureChestConfig config = player.getWorld().getChunkStore().getStore().getResource(EldaniorSystem.CONFIG_RESOURCE_TYPE);
        com.hypixel.hytale.server.core.universe.world.meta.BlockState blockState = player.getWorld().getState(x, y, z, true);

        if (config != null && config.isCanPlayerBreakLootChests() && blockState instanceof ItemContainerState containerState) {
            // Si le coffre est autorisé à la casse, on ne bloque que si personne n'a la fenêtre ouverte
            return !containerState.getWindows().isEmpty();
        }

        // Par défaut, si c'est un coffre protégé et que la config interdit la casse : on annule.
        return true;
    }

    /**
     * Vérifie si le bloc à ces coordonnées est un Treasure Chest.
     */
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