package com.eldanior.system.bibliotheque;

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

/**
 * Touche d'interaction sur une bibliotheque. Traitee au tick suivant : la protection des parcelles
 * (ParcelInteractEvent) a alors eu le temps d'annuler l'evenement.
 */
public class BibliothequeUseEvent extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    public BibliothequeUseEvent() { super(UseBlockEvent.Pre.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl UseBlockEvent.Pre event) {
        if (event.getInteractionType() != InteractionType.Use || !Bibliotheque.isLibrary(event.getBlockType())) return;
        Ref<EntityStore> ref = event.getContext().getEntity();
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        UUIDComponent uuid = store.getComponent(ref, UUIDComponent.getComponentType());
        World world = player != null ? player.getWorld() : null;
        if (pr == null || uuid == null || world == null) return;
        Vector3i at = event.getTargetBlock();
        world.execute(() -> {
            if (!event.isCancelled() && ref.isValid())
                Bibliotheque.open(world, ref, player, pr, uuid.getUuid(), at.x(), at.y(), at.z());
        });
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
