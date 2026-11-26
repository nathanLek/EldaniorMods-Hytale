package com.eldanior.system.gui;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.protocol.packets.window.ClientOpenWindow;
import com.hypixel.hytale.protocol.packets.window.CloseWindow;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;

/**
 * Sonde (2026-11-12) : l'inventaire de la touche Tab est dessine par le client (CharacterPanel.ui + code du client), le
 * serveur ne recoit aucun paquet « page ouverte ». Seule piste : le client demande peut-etre la fenetre d'artisanat de poche
 * (ClientOpenWindow PocketCrafting) en ouvrant l'inventaire. Cette sonde journalise les demandes de fenetre recues
 * ("[Sonde Tab]" dans le journal du serveur) pour le verifier en jeu. A retirer une fois la question tranchee.
 */
public final class InventoryOpenProbe {

    private InventoryOpenProbe() {}

    public static void register() {
        PacketAdapters.registerInbound((PlayerPacketWatcher) (pr, packet) -> {
            try {
                if (packet instanceof ClientOpenWindow open)
                    EldaniorLogger.info("[Sonde Tab] " + pr.getUsername() + " demande la fenetre " + open.type);
                else if (packet instanceof CloseWindow)
                    EldaniorLogger.info("[Sonde Tab] " + pr.getUsername() + " ferme une fenetre");
            } catch (Exception ignored) { }
        });
    }
}
