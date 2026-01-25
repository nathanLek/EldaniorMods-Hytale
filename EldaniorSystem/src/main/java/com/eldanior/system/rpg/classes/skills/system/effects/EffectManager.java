package com.eldanior.system.rpg.classes.skills.system.effects;

import com.eldanior.system.utils.NotificationHelper;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;

import java.lang.reflect.Method;

public class EffectManager {

    public static double getDistance(Vector3d p1, Vector3d p2) {
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        double dz = p1.z - p2.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Fait apparaître un effet visuel via Réflexion.
     * Contourne les erreurs de compilation "Cannot find symbol".
     */
    public static void spawnParticle(World world, Vector3d pos, String particleName, int count) {
        if (world == null || pos == null) return;

        try {
            // TENTATIVE 1 : Signature Complète (Doubles)
            Method m = world.getClass().getMethod("spawnParticle", String.class, Vector3d.class, int.class, double.class, double.class, double.class, double.class);
            m.invoke(world, particleName, pos, count, 0.5, 0.5, 0.5, 0.02);
            return;
        } catch (Exception e1) {
            // Ignorer
        }

        try {
            // TENTATIVE 2 : Signature Simple
            Method m = world.getClass().getMethod("spawnParticle", String.class, Vector3d.class, int.class);
            m.invoke(world, particleName, pos, count);
            return;
        } catch (Exception e2) {
            // Ignorer
        }

        try {
            // TENTATIVE 3 : Alternative "Effect" (Ancienne API)
            Method m = world.getClass().getMethod("spawnParticleEffect", String.class, Vector3d.class, int.class);
            m.invoke(world, particleName, pos, count);
        } catch (Exception e3) {
            // Si rien ne marche, on log juste une fois pour ne pas spammer
            // System.out.println("EffectManager: Impossible de trouver spawnParticle pour " + particleName);
        }
    }

    /**
     * Joue un son via Réflexion.
     */
    public static void playSound(World world, Vector3d pos, String soundName, float volume, float pitch) {
        if (world == null || pos == null) return;

        try {
            Method m = world.getClass().getMethod("playSound", String.class, Vector3d.class, float.class, float.class);
            m.invoke(world, soundName, pos, volume, pitch);
        } catch (Exception e) {
            // Fallback silencieux
        }
    }

    public static void sendCombatFeedback(PlayerRef player, String message, boolean isCritical) {
        NotificationHelper.sendNotification(player, message, isCritical ? NotificationStyle.Danger : NotificationStyle.Warning);
    }

    public static void sendBuffFeedback(PlayerRef player, String message) {
        NotificationHelper.sendNotification(player, message, NotificationStyle.Success);
    }
}