package com.eldanior.system.presentoir;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3i;

/** Touche d'interaction sur un presentoir. Traitee au tick suivant du monde : l'entite decor s'ajoute hors du passage des systemes. */
public class PresentoirUseEvent extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    public PresentoirUseEvent() { super(UseBlockEvent.Pre.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl UseBlockEvent.Pre event) {
        if (event.getInteractionType() != InteractionType.Use || !PresentoirManager.isStand(event.getBlockType())) return;
        Ref<EntityStore> ref = event.getContext().getEntity();
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        UUIDComponent uuid = store.getComponent(ref, UUIDComponent.getComponentType());
        World world = player != null ? player.getWorld() : null;
        if (pr == null || uuid == null || world == null) return;
        Vector3i base = PresentoirManager.base(world, event.getTargetBlock());
        world.execute(() -> {
            if (ref.isValid()) PresentoirManager.use(world, ref, player, pr, uuid.getUuid(), base);
        });
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
