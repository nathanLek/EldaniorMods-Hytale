package com.eldanior.system.config;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RateLimiter {

    private static final Map<String, Long> lastAction = new ConcurrentHashMap<>();
    private static final long DEFAULT_COOLDOWN = 1000; // 1 seconde

    /**
     * Verifie si le joueur peut executer l'action.
     * @return true si autorise, false si en cooldown
     */
    public static boolean canExecute(UUID player, String action) {
        return canExecute(player, action, DEFAULT_COOLDOWN);
    }

    public static boolean canExecute(UUID player, String action, long cooldownMs) {
        String key = player.toString() + ":" + action;
        long now = System.currentTimeMillis();
        Long last = lastAction.get(key);
        if (last != null && now - last < cooldownMs) return false;
        lastAction.put(key, now);
        return true;
    }

    /** Nettoyage au disconnect pour eviter les leaks */
    public static void cleanup(UUID player) {
        String prefix = player.toString() + ":";
        lastAction.keySet().removeIf(k -> k.startsWith(prefix));
    }
}