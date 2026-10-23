package com.eldanior.system.util;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.lang.reflect.Field;

/**
 * Attitude forcee d'un PNJ envers une entite (WorldSupport.overrideAttitude), utilisable sur n'importe quel role.
 * Le moteur ne cree la memoire des attitudes forcees que si le role utilise lui-meme l'action OverrideAttitude : sinon
 * overrideAttitude plante (attitudeOverrideMemory null). On la cree a la demande ; le tick du moteur gere ensuite l'expiration.
 */
public final class NpcAttitudes {

    private NpcAttitudes() {}

    private static final Field MEMORY = memoryField();

    private static Field memoryField() {
        try {
            Field f = WorldSupport.class.getDeclaredField("attitudeOverrideMemory");
            f.setAccessible(true);
            return f;
        } catch (Throwable e) {
            EldaniorLogger.error("[NpcAttitudes] champ attitudeOverrideMemory introuvable", e);
            return null;
        }
    }

    /** Le PNJ npc a l'attitude attitude envers other pendant seconds secondes. False si impossible. */
    public static boolean force(Ref<EntityStore> npc, Ref<EntityStore> other, Attitude attitude, double seconds,
                                ComponentAccessor<EntityStore> accessor) {
        if (npc == null || other == null || !npc.isValid() || !other.isValid()) return false;
        WorldSupport ws = WorldSupport.get(npc, accessor);
        if (ws == null) return false;
        try {
            if (MEMORY != null && MEMORY.get(ws) == null) MEMORY.set(ws, new Int2ObjectOpenHashMap<>());
            ws.overrideAttitude(other, attitude, seconds);
            return true;
        } catch (Exception e) {
            EldaniorLogger.error("[NpcAttitudes] attitude forcee", e);
            return false;
        }
    }

    public static boolean hostile(Ref<EntityStore> npc, Ref<EntityStore> other, double seconds, ComponentAccessor<EntityStore> accessor) {
        return force(npc, other, Attitude.HOSTILE, seconds, accessor);
    }
}
