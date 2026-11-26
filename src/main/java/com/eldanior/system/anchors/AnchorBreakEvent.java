package com.eldanior.system.anchors;

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

/** Un Cercle d'Ancrage ne s'efface que par son proprietaire ou un admin ; l'ancre est alors liberee. */
public class AnchorBreakEvent extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    public AnchorBreakEvent() { super(BreakBlockEvent.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl BreakBlockEvent event) {
        if (!AnchorManager.isCircle(event.getBlockType())) return;
        Player player = chunk.getComponent(index, Player.getComponentType());
        PlayerRef pr = chunk.getComponent(index, PlayerRef.getComponentType());
        World world = player != null ? player.getWorld() : null;
        if (pr == null || world == null) return;
        Vector3i base = AnchorManager.base(world, event.getTargetBlock());
        AnchorManager.Anchor anchor = AnchorManager.at(world.getName(), base);
        if (anchor == null) return;                                                     // cercle sans ancre : libre
        boolean mine = anchor.owner.equals(pr.getUuid());
        if (!mine && !pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
            event.setCancelled(true);
            pr.sendMessage(Message.raw("Ce cercle est l'ancre « " + anchor.name + " » de " + anchor.ownerName + ".").color("#e8c060"));
            return;
        }
        AnchorManager.removed(anchor);
        pr.sendMessage(Message.raw(mine ? "Votre ancre « " + anchor.name + " » est effacée."
                : "Ancre « " + anchor.name + " » de " + anchor.ownerName + " effacée.").color("#c8a8ff"));
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
