package com.eldanior.system.territory.teleport;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3i;

import java.util.concurrent.TimeUnit;

/** Pose de la pierre de fondation du Portail Continental (le petit bloc teleporteur a ete supprime le 2026-11-19). */
public final class TeleporterEvents {

    private TeleporterEvents() {}

    private static final String WARN = "#e8c060";

    private static boolean admin(PlayerRef pr) { return pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION); }

    /** Pierre de fondation du Portail Continental (admin) : bati la plateforme, la porte et le Gardien (ContinentalGates). */
    public static class GatePlace extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        public GatePlace() { super(PlaceBlockEvent.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl PlaceBlockEvent event) {
            var hand = event.getItemInHand();
            if (hand == null || hand.isEmpty() || !ContinentalGates.ITEM.equals(hand.getItemId())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            PlayerRef pr = chunk.getComponent(index, PlayerRef.getComponentType());
            World world = player != null ? player.getWorld() : null;
            if (pr == null || world == null) return;
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            Vector3i at = new Vector3i(event.getTargetBlock());
            ParcelData m = ContinentalTeleporters.marquisatAt(world.getName(), at.x() + 0.5, at.y() + 0.5, at.z() + 0.5);
            String refusal = !admin(pr) ? "Le Portail Continental est bâti par un admin ; le Marquis l'ouvre ou le ferme chez son intendant."
                    : m == null ? "Le Portail Continental se bâtit dans les limites d'un Marquisat."
                    : ContinentalTeleporters.spot(m) != null ? m.getName() + " a déjà son Portail Continental (/es portail retirer pour le libérer)."
                    : null;
            if (refusal != null) {
                event.setCancelled(true);
                pr.sendMessage(Message.raw(refusal).color(WARN));
                return;
            }
            var t = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
            int dir = t != null ? ContinentalGates.dirFrom(t.getPosition().x, t.getPosition().z, at.x(), at.z()) : 0;
            String id = m.getId();
            EldaniorLogger.SCHEDULER.schedule(() -> world.execute(() -> {
                try {
                    var placed = WorldBlockUtil.getBlockType(world, at.x(), at.y(), at.z());
                    if (placed == null || placed.getId() == null || !placed.getId().contains(ContinentalGates.ITEM)) return;   // pose annulee
                    ParcelData mq = ParcelManager.get(id);
                    if (mq == null) return;
                    String err = ContinentalGates.build(world, mq, at.x(), at.y(), at.z(), dir);
                    pr.sendMessage(Message.raw(err != null ? err : "Portail Continental de " + mq.getName() + " bâti. "
                            + (ContinentalTeleporters.active(mq) ? "Il est ouvert." : "Le Marquis l'ouvre chez son intendant (Domaines › "
                            + mq.getName() + " › Règles du domaine › Téléporteur Continental).")).color(err != null ? WARN : "#7fd4ff"));
                } catch (Exception e) {
                    EldaniorLogger.error("[Portail] construction", e);
                }
            }), 300, TimeUnit.MILLISECONDS);
        }

        @Override
        public Query<EntityStore> getQuery() { return Archetype.empty(); }
    }
}
