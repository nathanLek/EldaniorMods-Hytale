package com.eldanior.system.dojo;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.concurrent.TimeUnit;

/** Socle du mannequin d'entrainement : pose (le mannequin apparait) et casse (il disparait ; le socle tombe comme un bloc). */
public final class MannequinEvents {

    private MannequinEvents() {}

    public static class Place extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        public Place() { super(PlaceBlockEvent.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl PlaceBlockEvent event) {
            if (event.getItemInHand() == null || !Mannequins.ID.equals(event.getItemInHand().getItemId())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            PlayerRef pr = chunk.getComponent(index, PlayerRef.getComponentType());
            World world = player != null ? player.getWorld() : null;
            if (pr == null || world == null) return;
            Vector3i at = new Vector3i(event.getTargetBlock());
            Vector3d placer = PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
            // Traitee un peu plus tard : la protection des parcelles a pu annuler la pose
            EldaniorLogger.SCHEDULER.schedule(() -> world.execute(() -> {
                try {
                    if (Mannequins.isBlock(WorldBlockUtil.getBlockType(world, at.x(), at.y(), at.z())))
                        Mannequins.placed(world, at, placer != null ? new Vector3d(placer) : null);
                } catch (Exception e) {
                    EldaniorLogger.error("[Mannequin] pose", e);
                }
            }), 250, TimeUnit.MILLISECONDS);
        }

        @Override
        public Query<EntityStore> getQuery() { return Archetype.empty(); }
    }

    public static class Break extends EntityEventSystem<EntityStore, BreakBlockEvent> {
        public Break() { super(BreakBlockEvent.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl BreakBlockEvent event) {
            if (!Mannequins.isBlock(event.getBlockType())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            World world = player != null ? player.getWorld() : null;
            if (world == null) return;
            Vector3i at = new Vector3i(event.getTargetBlock());
            world.execute(() -> {                                                            // apres une eventuelle annulation (parcelle)
                try {
                    if (!event.isCancelled()) Mannequins.broken(world, at);
                } catch (Exception e) {
                    EldaniorLogger.error("[Mannequin] casse", e);
                }
            });
        }

        @Override
        public Query<EntityStore> getQuery() { return Archetype.empty(); }
    }

}
