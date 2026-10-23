package com.eldanior.system.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Regard impose a un PNJ pendant un temps (ex. un villageois qui parle a un autre PNJ ou regarde un etabli). Applique a
 * chaque tick par DoppelPoseSystem, apres l'IA des PNJ (sinon elle le retourne dans le sens de sa marche).
 */
public final class NpcFacing {

    private NpcFacing() {}

    public record Look(Vector3d point, long until) {}

    private static final Map<Ref<EntityStore>, Look> LOOKS = new ConcurrentHashMap<>();

    /** Le PNJ regarde point jusqu'a until (horloge systeme, ms). */
    public static void lookAt(Ref<EntityStore> npc, Vector3d point, long until) {
        if (npc != null && point != null) LOOKS.put(npc, new Look(new Vector3d(point), until));
    }

    public static void clear(Ref<EntityStore> npc) {
        if (npc != null) LOOKS.remove(npc);
    }

    /** Regard en cours (null si aucun ou expire). */
    public static Look get(Ref<EntityStore> npc) {
        if (LOOKS.isEmpty()) return null;
        Look l = LOOKS.get(npc);
        if (l == null) return null;
        if (l.until() < System.currentTimeMillis() || !npc.isValid()) { LOOKS.remove(npc); return null; }
        return l;
    }
}
