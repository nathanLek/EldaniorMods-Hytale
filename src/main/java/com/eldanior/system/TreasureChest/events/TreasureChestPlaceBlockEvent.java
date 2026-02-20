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
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

@SuppressWarnings({"deprecation", "removal", "ConstantConditions"})
public class TreasureChestPlaceBlockEvent extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    public TreasureChestPlaceBlockEvent() {
        super(PlaceBlockEvent.class);
    }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl PlaceBlockEvent event) {
        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        if (player == null) return;

        ItemStack item = event.getItemInHand();

        // On vérifie si le joueur tient un bloc de type "chest" (coffre)
        if (item != null && item.getItemId().toLowerCase().contains("chest")) {
            Vector3i pos = event.getTargetBlock();

            // On vérifie les 4 blocs adjacents horizontaux (X et Z)
            if (isProtectedChest(player, pos.getX() + 1, pos.getY(), pos.getZ()) ||
                    isProtectedChest(player, pos.getX() - 1, pos.getY(), pos.getZ()) ||
                    isProtectedChest(player, pos.getX(), pos.getY(), pos.getZ() + 1) ||
                    isProtectedChest(player, pos.getX(), pos.getY(), pos.getZ() - 1)) {

                // Si un des coffres voisins est un Treasure Chest, on bloque la pose
                event.setCancelled(true);
            }
        }
    }

    /**
     * Méthode utilitaire pour vérifier si un bloc à une coordonnée précise est un Treasure Chest enregistré.
     */
    private boolean isProtectedChest(Player player, int x, int y, int z) {
        // Utilisation du chemin complet (FQN) pour éviter l'avertissement de dépréciation sur l'import
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