package com.eldanior.system.duel;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.classement.ClassementManager;
import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DuelManager {

    // Duels actifs: joueur UUID -> ActiveDuel
    private static final Map<UUID, ActiveDuel> activeDuels = new ConcurrentHashMap<>();

    // Invitations: cible UUID -> challenger UUID
    private static final Map<UUID, UUID> pendingDuels = new ConcurrentHashMap<>();

    // File d'attente de duels a terminer (pour eviter Store is currently processing)
    private static final Queue<UUID> pendingEndDuels = new java.util.concurrent.ConcurrentLinkedQueue<>();

    /** Appele depuis le tick pour marquer un duel a terminer plus tard */
    public static void scheduleEndDuel(UUID loserUUID) {
        pendingEndDuels.add(loserUUID);
    }

    /** Appele depuis un endroit sur (pas un tick) pour traiter les fins de duel */
    public static void processPendingEndDuels() {
        UUID loser;
        while ((loser = pendingEndDuels.poll()) != null) {
            endDuel(loser);
        }
    }

    private static java.util.Timer duelTimer;

    public static void init() {
        // Timer pour traiter les fins de duel hors du tick (toutes les 200ms)
        if (duelTimer != null) duelTimer.cancel();
        duelTimer = new java.util.Timer("EldaniorDuelProcessor", true);
        duelTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                try { processPendingEndDuels(); } catch (Exception e) { e.printStackTrace(); }
            }
        }, 200L, 200L);
        System.out.println("[Eldanior] Systeme de Duels initialise.");
    }

    // ==================== INVITATIONS ====================

    public static void sendChallenge(UUID challengerUUID, UUID targetUUID) {
        pendingDuels.put(targetUUID, challengerUUID);
    }

    public static boolean hasPendingChallenge(UUID targetUUID) {
        return pendingDuels.containsKey(targetUUID);
    }

    public static UUID getPendingChallenger(UUID targetUUID) {
        return pendingDuels.get(targetUUID);
    }

    public static void clearChallenge(UUID targetUUID) {
        pendingDuels.remove(targetUUID);
    }

    // ==================== DUEL ACTIF ====================

    public static boolean isInDuel(UUID playerUUID) {
        return activeDuels.containsKey(playerUUID);
    }

    public static ActiveDuel getDuel(UUID playerUUID) {
        return activeDuels.get(playerUUID);
    }

    private static final Object DUEL_LOCK = new Object();

    public static void startDuel(UUID player1, UUID player2) {
        synchronized (DUEL_LOCK) {
            if (isInDuel(player1) || isInDuel(player2)) return;
            ActiveDuel duel = new ActiveDuel(player1, player2);
            activeDuels.put(player1, duel);
            activeDuels.put(player2, duel);
        }
    }

    /**
     * Termine le duel. Appele quand un joueur tombe a 1 HP.
     * @param loserUUID le joueur qui a perdu
     */
    public static void endDuel(UUID loserUUID) {
        ActiveDuel duel = activeDuels.get(loserUUID);
        if (duel == null) return;

        UUID winnerUUID = duel.getOpponent(loserUUID);

        // Retirer le duel actif
        activeDuels.remove(duel.getPlayer1());
        activeDuels.remove(duel.getPlayer2());

        PlayerRef winnerRef = Universe.get().getPlayer(winnerUUID);
        PlayerRef loserRef = Universe.get().getPlayer(loserUUID);

        // Recuperer les HP restants pour l'historique
        float winnerHPPercent = getHPPercent(winnerRef);
        float loserHPPercent = 0.01f; // 1 HP = quasi 0

        // Mise a jour stats + XP transfer
        processResults(winnerUUID, winnerRef, loserUUID, loserRef, winnerHPPercent);

        // Heal les deux joueurs a 100%
        healFull(winnerRef);
        healFull(loserRef);

        // Messages
        String winnerName = winnerRef != null ? winnerRef.getUsername() : "?";
        String loserName = loserRef != null ? loserRef.getUsername() : "?";

        if (winnerRef != null) {
            winnerRef.sendMessage(Message.raw("§a§lDuel gagne ! §7Vous avez battu " + loserName));
        }
        if (loserRef != null) {
            loserRef.sendMessage(Message.raw("§c§lDuel perdu ! §7" + winnerName + " vous a vaincu."));
        }
    }

    private static void processResults(UUID winnerUUID, PlayerRef winnerRef, UUID loserUUID, PlayerRef loserRef,
                                        float winnerHPPercent) {
        try {
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

            // Loser: perd 10% XP
            if (loserRef != null) {
                var lRef = loserRef.getReference();
                if (lRef != null) {
                    var lStore = lRef.getStore();
                    PlayerLevelData lData = lStore.getComponent(lRef, type);
                    if (lData != null) {
                        int xpLost = lData.removeExperiencePercent(0.10);
                        lData.addDuelLoss();
                        String winnerName = winnerRef != null ? winnerRef.getUsername() : "?";
                        lData.addDuelHistory(winnerName, false, 1, winnerHPPercent);
                        lStore.putComponent(lRef, type, lData);

                        // Winner: gagne l'XP perdue
                        if (winnerRef != null) {
                            var wRef = winnerRef.getReference();
                            if (wRef != null) {
                                var wStore = wRef.getStore();
                                PlayerLevelData wData = wStore.getComponent(wRef, type);
                                if (wData != null) {
                                    wData.addExperience(xpLost);
                                    wData.addDuelWin();
                                    String loserName = loserRef.getUsername();
                                    wData.addDuelHistory(loserName, true, winnerHPPercent, 1);
                                    wStore.putComponent(wRef, type, wData);

                                    // Classement + quete
                                    ClassementManager.updateDuelWins(winnerRef.getUsername(), wData.getDuelWins());
                                    QuestManager.onDuelWin(winnerUUID);

                                    winnerRef.sendMessage(Message.raw("§a+" + xpLost + " XP (mise du duel)"));
                                }
                            }
                        }

                        loserRef.sendMessage(Message.raw("§c-" + xpLost + " XP (mise du duel)"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static float getHPPercent(PlayerRef playerRef) {
        try {
            if (playerRef == null) return 0f;
            var ref = playerRef.getReference();
            if (ref == null) return 0f;
            var store = ref.getStore();
            EntityStatMap statMap = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) return 0f;
            var hp = statMap.get(DefaultEntityStatTypes.getHealth());
            if (hp == null) return 0f;
            return hp.getMax() > 0 ? hp.get() / hp.getMax() : 0f;
        } catch (Exception e) { return 0f; }
    }

    private static void healFull(PlayerRef playerRef) {
        try {
            if (playerRef == null) return;
            var ref = playerRef.getReference();
            if (ref == null) return;
            var store = ref.getStore();
            EntityStatMap statMap = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap == null) return;
            var hp = statMap.get(DefaultEntityStatTypes.getHealth());
            if (hp != null) {
                statMap.setStatValue(DefaultEntityStatTypes.getHealth(), hp.getMax());
            }
            var mana = statMap.get(DefaultEntityStatTypes.getMana());
            if (mana != null) {
                statMap.setStatValue(DefaultEntityStatTypes.getMana(), mana.getMax());
            }
        } catch (Exception e) { EldaniorLogger.error("DuelManager", e); }
    }

    // ==================== CLEANUP ====================

    public static void cancelAllDuels() {
        activeDuels.clear();
        pendingDuels.clear();
    }

    // ==================== INNER CLASS ====================

    public static class ActiveDuel {
        private final UUID player1;
        private final UUID player2;
        private final long startTime;

        public ActiveDuel(UUID player1, UUID player2) {
            this.player1 = player1;
            this.player2 = player2;
            this.startTime = System.currentTimeMillis();
        }

        public UUID getPlayer1() { return player1; }
        public UUID getPlayer2() { return player2; }
        public long getStartTime() { return startTime; }

        public UUID getOpponent(UUID playerUUID) {
            return playerUUID.equals(player1) ? player2 : player1;
        }
    }
}
