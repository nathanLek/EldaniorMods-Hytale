package com.eldanior.system.familiers;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3i;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** Oeufs de familier : pose (lien, un seul familier par joueur), casse (maitre ou admin), touche d'interaction (suivre / rester). */
public final class FamilierEvents {

    private FamilierEvents() {}

    /** Poses en cours de verification : deux oeufs poses coup sur coup ne creent pas deux liens. */
    private static final Map<UUID, Long> PENDING = new ConcurrentHashMap<>();

    public static class Place extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        public Place() { super(PlaceBlockEvent.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl PlaceBlockEvent event) {
            Espece espece = event.getItemInHand() != null ? Espece.ofEgg(event.getItemInHand().getItemId()) : null;
            if (espece == null) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            PlayerRef pr = chunk.getComponent(index, PlayerRef.getComponentType());
            World world = player != null ? player.getWorld() : null;
            if (pr == null || world == null) return;
            UUID uuid = pr.getUuid();
            Familier mine = FamilierManager.of(uuid);
            Long pending = PENDING.get(uuid);
            if ((mine != null && (mine.hasEgg() || mine.espece != espece) && !FamilierManager.isAdmin(uuid)) || (pending != null && System.currentTimeMillis() - pending < 2_000)) {
                event.setCancelled(true);
                if (mine != null) FamilierManager.say(pr, mine.espece != espece
                        ? "Vous êtes déjà lié à " + mine.name + " (" + mine.espece.label + "). Un seul compagnon par maître : "
                          + "relâchez-le depuis son œuf avant d'en adopter un autre."
                        : FamilierManager.refusal(mine), "#e8c060");
                return;
            }
            Vector3i at = new Vector3i(event.getTargetBlock());
            PENDING.put(uuid, System.currentTimeMillis());
            EldaniorLogger.SCHEDULER.schedule(() -> world.execute(() -> {
                try {
                    if (FamilierManager.isEgg(WorldBlockUtil.getBlockType(world, at.x(), at.y(), at.z()))
                            && FamilierManager.atEgg(world.getName(), at) == null)
                        FamilierManager.placed(world, at, pr, espece);
                } catch (Exception e) {
                    EldaniorLogger.error("[Familiers] pose", e);
                } finally {
                    PENDING.remove(uuid);
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
            if (!FamilierManager.isEgg(event.getBlockType())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            PlayerRef pr = chunk.getComponent(index, PlayerRef.getComponentType());
            World world = player != null ? player.getWorld() : null;
            if (pr == null || world == null) return;
            Familier f = FamilierManager.atEgg(world.getName(), event.getTargetBlock());
            if (f == null) return;                                                          // oeuf libre
            if (!f.owner.equals(pr.getUuid()) && !pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
                event.setCancelled(true);
                FamilierManager.say(pr, "Cet œuf est le nid de " + f.name + ", le compagnon de " + f.ownerName + ".", "#e8c060");
                return;
            }
            FamilierManager.pickedUp(world, f, pr);
        }

        @Override
        public Query<EntityStore> getQuery() { return Archetype.empty(); }
    }

    /** Traitee au tick suivant : la protection des parcelles a pu annuler l'evenement. */
    public static class Use extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {
        public Use() { super(UseBlockEvent.Pre.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl UseBlockEvent.Pre event) {
            if (event.getInteractionType() != InteractionType.Use || !FamilierManager.isEgg(event.getBlockType())) return;
            Ref<EntityStore> ref = event.getContext().getEntity();
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            World world = player != null ? player.getWorld() : null;
            if (pr == null || world == null) return;
            Vector3i at = new Vector3i(event.getTargetBlock());
            world.execute(() -> {
                try {
                    if (!event.isCancelled() && ref.isValid()) FamilierManager.useEgg(world, ref, pr, at);
                } catch (Exception e) {
                    EldaniorLogger.error("[Familiers] interaction", e);
                }
            });
        }

        @Override
        public Query<EntityStore> getQuery() { return Archetype.empty(); }
    }
}
