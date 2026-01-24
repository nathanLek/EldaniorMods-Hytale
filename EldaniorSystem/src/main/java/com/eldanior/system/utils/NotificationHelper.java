package com.eldanior.system.utils;

import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import java.util.UUID;
import javax.annotation.Nonnull;

public class NotificationHelper {

    // Envoie une notification "Toast" (en haut à droite)
    public static void sendNotification(@Nonnull PlayerRef playerRef, @Nonnull String message, @Nonnull NotificationStyle style) {
        NotificationUtil.sendNotification(playerRef.getPacketHandler(), TinyMsg.parse(message), style);
    }

    public static void sendNotification(@Nonnull PlayerRef playerRef, @Nonnull String message) {
        sendNotification(playerRef, message, NotificationStyle.Default);
    }

    public static void sendSuccess(@Nonnull PlayerRef playerRef, @Nonnull String message) {
        sendNotification(playerRef, message, NotificationStyle.Success);
    }

    public static void sendWarning(@Nonnull PlayerRef playerRef, @Nonnull String message) {
        sendNotification(playerRef, message, NotificationStyle.Warning);
    }

    public static void sendDanger(@Nonnull PlayerRef playerRef, @Nonnull String message) {
        sendNotification(playerRef, message, NotificationStyle.Danger);
    }

    // Affiche un gros titre au milieu de l'écran
    public static void showEventTitle(@Nonnull PlayerRef playerRef, @Nonnull String primaryTitle, @Nonnull String secondaryTitle, boolean isMajor) {
        UUID worldUuid = playerRef.getWorldUuid();
        if (worldUuid != null) {
            World world = Universe.get().getWorld(worldUuid);
            if (world != null && world.isAlive()) {
                world.execute(() -> safeShowTitle(playerRef, primaryTitle, secondaryTitle, isMajor));
                return;
            }
        }
        safeShowTitle(playerRef, primaryTitle, secondaryTitle, isMajor);
    }

    private static void safeShowTitle(PlayerRef playerRef, String primary, String secondary, boolean isMajor) {
        try {
            EventTitleUtil.showEventTitleToPlayer(playerRef, TinyMsg.parse(primary), TinyMsg.parse(secondary), isMajor);
        } catch (Exception e) {
            // Fallback si le titre échoue
            sendNotification(playerRef, primary + " - " + secondary, NotificationStyle.Success);
        }
    }

    public static void showLevelUpTitle(@Nonnull PlayerRef playerRef, int newLevel) {
        showEventTitle(playerRef, "LEVEL UP!", "Niveau" + newLevel, true);
    }
}