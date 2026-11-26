package com.eldanior.system.anchors;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3i;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Pose d'un Cercle d'Ancrage : refusee si le joueur a deja toutes ses ancres. Sinon, un peu plus tard (le bloc n'est pas encore
 * dans le monde pendant l'evenement, et une protection de parcelle peut encore l'annuler), on verifie que le cercle est bien la,
 * on cree l'ancre avec un nom par defaut et on ouvre la fenetre du nom.
 */
public class AnchorPlaceEvent extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    /** Poses en cours de verification, par joueur : deux poses tres rapprochees ne depassent pas la limite. */
    private static final Map<UUID, Integer> PENDING = new ConcurrentHashMap<>();

    public AnchorPlaceEvent() { super(PlaceBlockEvent.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl PlaceBlockEvent event) {
        if (!AnchorManager.isCircle(event.getItemInHand())) return;
        Player player = chunk.getComponent(index, Player.getComponentType());
        PlayerRef pr = chunk.getComponent(index, PlayerRef.getComponentType());
        World world = player != null ? player.getWorld() : null;
        if (pr == null || world == null) return;
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        UUID uuid = pr.getUuid();
        int max = AnchorManager.maxAnchors(ref, store);
        int used = AnchorManager.countOf(uuid) + PENDING.getOrDefault(uuid, 0);
        if (used >= max) {
            event.setCancelled(true);
            List<AnchorManager.Anchor> mine = AnchorManager.anchorsOf(uuid, null);
            StringBuilder names = new StringBuilder();
            for (AnchorManager.Anchor a : mine) names.append(names.isEmpty() ? "" : ", ").append("« ").append(a.name).append(" » (").append(a.coords()).append(")");
            pr.sendMessage(Message.raw(max > 1 ? "Vous possédez déjà vos " + max + " ancres : " : "Vous possédez déjà une ancre : ").color("#e8c060")
                    .insert(Message.raw(names + ". Effacez-en une pour en tracer une autre.").color("#a9b8c8")));
            return;
        }
        Vector3i at = new Vector3i(event.getTargetBlock());
        PENDING.merge(uuid, 1, Integer::sum);
        EldaniorLogger.SCHEDULER.schedule(() -> world.execute(() -> {
            try {
                confirm(world, at, ref, player, pr);
            } catch (Exception e) {
                EldaniorLogger.error("[Ancres] pose", e);
            } finally {
                PENDING.computeIfPresent(uuid, (k, n) -> n > 1 ? n - 1 : null);
            }
        }), 250, TimeUnit.MILLISECONDS);
    }

    private static void confirm(World world, Vector3i at, Ref<EntityStore> ref, Player player, PlayerRef pr) {
        if (!AnchorManager.isCircle(WorldBlockUtil.getBlockType(world, at.x(), at.y(), at.z()))) return;   // pose annulee
        if (WorldBlockUtil.getFiller(world, at.x(), at.y(), at.z()) > 0) return;
        if (AnchorManager.at(world.getName(), at) != null) return;
        AnchorManager.Anchor anchor = AnchorManager.placed(world, at, pr.getUuid(), pr.getUsername());
        if (ref.isValid()) player.getPageManager().openCustomPage(ref, ref.getStore(), new AnchorNamePage(pr, anchor, true));
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
