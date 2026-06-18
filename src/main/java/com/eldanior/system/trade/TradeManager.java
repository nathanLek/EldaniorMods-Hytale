package com.eldanior.system.trade;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TradeManager {

    private static final double MAX_DISTANCE = 15.0;

    // target UUID -> sender UUID
    private static final Map<UUID, UUID> pendingInvites = new ConcurrentHashMap<>();

    // player UUID -> active session
    private static final Map<UUID, TradeSession> activeSessions = new ConcurrentHashMap<>();

    // player UUID -> active trade screen (pour notifier l'autre joueur)
    private static final Map<UUID, TradeScreen> activeScreens = new ConcurrentHashMap<>();

    public static void init() {
        pendingInvites.clear();
        activeSessions.clear();
        activeScreens.clear();
        System.out.println("[Eldanior] TradeManager initialise.");
    }

    public static void registerScreen(UUID playerUUID, TradeScreen screen) {
        activeScreens.put(playerUUID, screen);
    }

    public static TradeScreen getScreen(UUID playerUUID) {
        return activeScreens.get(playerUUID);
    }

    private static final Object TRADE_LOCK = new Object();

    // ==================== INVITATIONS ====================

    public static boolean sendInvite(UUID senderUUID, UUID targetUUID) {
        synchronized (TRADE_LOCK) {
            if (senderUUID.equals(targetUUID)) return false;
            if (!com.eldanior.system.config.RateLimiter.canExecute(senderUUID, "trade.invite", 3000)) return false;
            if (isInTrade(senderUUID) || isInTrade(targetUUID)) return false;
            if (pendingInvites.containsKey(targetUUID)) return false;

            pendingInvites.put(targetUUID, senderUUID);
            return true;
        }
    }

    public static boolean hasPendingInvite(UUID targetUUID) {
        return pendingInvites.containsKey(targetUUID);
    }

    public static UUID getPendingInviter(UUID targetUUID) {
        return pendingInvites.get(targetUUID);
    }

    public static void cancelInvite(UUID senderUUID) {
        pendingInvites.entrySet().removeIf(e -> e.getValue().equals(senderUUID));
    }

    public static void clearInvite(UUID targetUUID) {
        pendingInvites.remove(targetUUID);
    }

    // ==================== SESSIONS ====================

    public static TradeSession startTrade(UUID player1, UUID player2) {
        synchronized (TRADE_LOCK) {
            if (isInTrade(player1) || isInTrade(player2)) return null;
            TradeSession session = new TradeSession(player1, player2);
            activeSessions.put(player1, session);
            activeSessions.put(player2, session);
            clearInvite(player2);
            return session;
        }
    }

    public static boolean isInTrade(UUID playerUUID) {
        return activeSessions.containsKey(playerUUID);
    }

    public static TradeSession getSession(UUID playerUUID) {
        return activeSessions.get(playerUUID);
    }

    public static void endTrade(TradeSession session, boolean execute) {
        if (session == null) return;

        synchronized (TRADE_LOCK) {
            // Anti-duplication : si deja annule, ne rien faire
            if (session.isCancelled()) return;

            UUID p1 = session.getPlayer1();
            UUID p2 = session.getPlayer2();

            if (execute && session.isBothValidated()) {
                // Transferer les items
                transferItems(p1, p2, session);
            } else {
                // Annulation : rendre les items à chaque joueur
                returnItems(p1, session.getMyItems(p1));
                returnItems(p2, session.getMyItems(p2));
            }

            // Fermer les fenetres des deux joueurs
            TradeScreen screen1 = activeScreens.remove(p1);
            TradeScreen screen2 = activeScreens.remove(p2);
            if (screen1 != null) screen1.closeScreen();
            if (screen2 != null) screen2.closeScreen();

            activeSessions.remove(p1);
            activeSessions.remove(p2);
            session.cancel();
        }
    }

    private static void transferItems(UUID from, UUID to, TradeSession session) {
        ItemStack[] fromItems = session.getMyItems(from);
        ItemStack[] toItems = session.getMyItems(to);

        // Items de 'from' vont dans la hotbar de 'to'
        giveItems(to, fromItems);
        // Items de 'to' vont dans la hotbar de 'from'
        giveItems(from, toItems);
    }

    private static void giveItems(UUID targetUUID, ItemStack[] items) {
        try {
            PlayerRef targetRef = Universe.get().getPlayer(targetUUID);
            if (targetRef == null) return;
            var ref = targetRef.getReference();
            if (ref == null) return;
            var store = ref.getStore();
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;

            ItemContainer hotbar = player.getInventory().getHotbar();
            for (ItemStack item : items) {
                if (item != null && !item.isEmpty()) {
                    var transaction = hotbar.addItemStack(item);
                    if (transaction != null && !transaction.succeeded()) {
                        System.err.println("[Trade] ALERTE: hotbar pleine pour " + targetUUID + ", items non ajoutes !");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Trade] Erreur transfert items: " + e.getMessage());
        }
    }

    /**
     * Compte les slots vides dans la hotbar d'un joueur.
     */
    public static int countEmptySlots(UUID playerUUID) {
        try {
            PlayerRef playerRef = Universe.get().getPlayer(playerUUID);
            if (playerRef == null) return 0;
            var ref = playerRef.getReference();
            if (ref == null) return 0;
            var store = ref.getStore();
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return 0;

            ItemContainer hotbar = player.getInventory().getHotbar();
            int empty = 0;
            for (short i = 0; i < 9; i++) {
                ItemStack item = hotbar.getItemStack(i);
                if (item == null || item.isEmpty()) empty++;
            }
            return empty;
        } catch (Exception e) {
            return 0;
        }
    }

    private static void returnItems(UUID playerUUID, ItemStack[] items) {
        giveItems(playerUUID, items);
    }

    // ==================== CLEANUP ====================

    public static void cancelAllTrades() {
        synchronized (TRADE_LOCK) {
            for (TradeSession session : new HashSet<>(activeSessions.values())) {
                if (!session.isCancelled()) {
                    endTrade(session, false);
                }
            }
            pendingInvites.clear();
        }
    }

    public static void handleDisconnect(UUID playerUUID) {
        // Annuler les invitations
        cancelInvite(playerUUID);
        pendingInvites.remove(playerUUID);

        // Annuler les trades actifs
        TradeSession session = activeSessions.get(playerUUID);
        if (session != null) {
            endTrade(session, false);
        }
    }
}
