package com.eldanior.system.boards;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/** Seuls les admins retirent un tableau. */
public class BoardBreakBlockEvent extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    public BoardBreakBlockEvent() { super(BreakBlockEvent.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl BreakBlockEvent event) {
        Player player = chunk.getComponent(index, Player.getComponentType());
        if (player == null || event.getBlockType() == null || BoardType.of(event.getBlockType().getId()) == null) return;
        if (!player.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION)) event.setCancelled(true);
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
