package com.eldanior.system.locks;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/** Une porte verrouillee ne se contourne pas a la pioche : seul le mode creatif la retire. */
public class DoorLockBreakEvent extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    public DoorLockBreakEvent() { super(BreakBlockEvent.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl BreakBlockEvent event) {
        if (DoorLock.of(event.getBlockType()) == null) return;
        Player player = chunk.getComponent(index, Player.getComponentType());
        if (player == null || player.getGameMode() != GameMode.Creative) event.setCancelled(true);
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
