package com.eldanior.system.boards;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/** Seuls les admins posent un tableau. */
public class BoardPlaceBlockEvent extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    public BoardPlaceBlockEvent() { super(PlaceBlockEvent.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl PlaceBlockEvent event) {
        Player player = chunk.getComponent(index, Player.getComponentType());
        if (player == null || event.getItemInHand() == null || BoardType.of(event.getItemInHand().getItemId()) == null) return;
        if (player.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION)) return;
        event.setCancelled(true);
        player.getPlayerRef().sendMessage(Message.raw("Seuls les administrateurs peuvent poser un tableau."));
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
