package com.eldanior.system.rpg.classes.skills.system.effects;

import com.eldanior.system.utils.NotificationHelper;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
// CORRECTION 1 : Le bon package pour NotificationStyle
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class EffectManager {

    public static double getDistance(Vector3d p1, Vector3d p2) {
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        double dz = p1.z - p2.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Fait apparaître un effet visuel via l'API Native de Hytale.
     * Nécessite le Store pour créer l'entité particule.
     */
    public static void spawnParticle(String particleName, Vector3d pos, Store<EntityStore> store) {
        if (store == null || pos == null) return;
        // Log de diagnostic
        System.out.println("Tentative de spawn particule: " + particleName + " à " + pos.toString());
        ParticleUtil.spawnParticleEffect(particleName, pos, store);
    }

    /**
     * Joue un son via l'API Native de Hytale.
     * Nécessite le CommandBuffer pour envoyer l'instruction au serveur.
     */
    public static void playSound(String soundName, Vector3d pos, float volume, float pitch, CommandBuffer<EntityStore> commandBuffer) {
        if (commandBuffer == null || pos == null) return;

        // Récupération de l'ID du son depuis le registre
        int soundId = SoundEvent.getAssetMap().getIndex(soundName);

        if (soundId != -1) {
            // CORRECTION 2 : Utilisation de playSoundEvent3d avec x, y, z
            SoundUtil.playSoundEvent3d(
                    soundId,
                    SoundCategory.SFX,
                    pos.x, pos.y, pos.z, // On décompose le Vector3d
                    volume,
                    pitch,
                    commandBuffer
            );
        } else {
            // Debug utile si tu te trompes de nom
            System.out.println("[EffectManager] Son introuvable : " + soundName);
        }
    }

    public static void sendCombatFeedback(PlayerRef player, String message, boolean isCritical) {
        NotificationHelper.sendNotification(player, message, isCritical ? NotificationStyle.Danger : NotificationStyle.Warning);
    }

    public static void sendBuffFeedback(PlayerRef player, String message) {
        NotificationHelper.sendNotification(player, message, NotificationStyle.Success);
    }
}