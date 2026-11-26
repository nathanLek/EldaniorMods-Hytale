package com.eldanior.system.presentoir;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3i;

/** Un presentoir garni ne se casse que par son proprietaire ou un admin ; son contenu tombe alors au sol. */
public class PresentoirBreakEvent extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    public PresentoirBreakEvent() { super(BreakBlockEvent.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl BreakBlockEvent event) {
        Player player = chunk.getComponent(index, Player.getComponentType());
        PlayerRef pr = chunk.getComponent(index, PlayerRef.getComponentType());
        World world = player != null ? player.getWorld() : null;
        if (pr == null || world == null || !PresentoirManager.isStand(event.getBlockType())) return;
        Vector3i base = PresentoirManager.base(world, event.getTargetBlock());
        if (PresentoirManager.canBreak(world.getName(), base, pr.getUuid(), pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION))) {
            PresentoirManager.broken(world, base);
            return;
        }
        event.setCancelled(true);
        pr.sendMessage(Message.raw("Ce présentoir appartient à " + PresentoirManager.ownerName(world.getName(), base) + ".").color("#e8c060"));
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
