package com.eldanior.system.utils;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

public class TinyMsg {

    public static void send(Player player, String text) {
        if (player == null) return;

        // Nettoyage temporaire des balises
        String cleanText = text.replaceAll("<[^>]*>", "");

        // CORRECTION ICI : Utilisez .raw() au lieu de new Message()
        player.sendMessage(Message.raw(cleanText));
    }
}