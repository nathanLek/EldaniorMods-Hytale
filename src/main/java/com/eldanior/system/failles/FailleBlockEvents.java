package com.eldanior.system.failles;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/** Blocs des failles : pose d'une Faille scellee (la faille s'ouvre a sa place) et F sur le Sceau de la salle du boss. */
public final class FailleBlockEvents {

    private FailleBlockEvents() {}

    /** Faille scellee posee : refusee en ville ou dans une faille ; sinon la faille s'ouvre a cet endroit (objet consomme). */
    public static class Place extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

        public Place() { super(PlaceBlockEvent.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl PlaceBlockEvent event) {
            ItemStack item = event.getItemInHand();
            if (item == null || !item.getItemId().startsWith(FailleManager.SEALED_PREFIX)) return;
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            World world = player != null ? player.getWorld() : null;
            if (pr == null || world == null) return;
            if (!FailleManager.placeSealed(player, pr, world, event.getTargetBlock(), item.getItemId())) event.setCancelled(true);
        }

        @Override
        public Query<EntityStore> getQuery() { return Archetype.empty(); }
    }
}
