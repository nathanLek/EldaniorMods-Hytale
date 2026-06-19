package com.eldanior.system.territory;

import com.eldanior.system.config.PersistenceUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ArenaManager {

    // arenaId -> (playerName -> ArenaStats)
    private static final Map<String, Map<String, ArenaStats>> arenaLeaderboards = new ConcurrentHashMap<>();
    private static Path dataDir;

    // Joueurs actuellement en arene: playerUUID -> arenaId
    private static final Map<UUID, String> playersInArena = new ConcurrentHashMap<>();

    public static void init(Path pluginDataDir) {
        dataDir = pluginDataDir;
        load();
        System.out.println("[Eldanior] ArenaManager: " + arenaLeaderboards.size() + " arenes chargees.");
    }

    // ==================== ENTREE / SORTIE ====================

    public static void enterArena(UUID playerUUID, String arenaId) {
        playersInArena.put(playerUUID, arenaId);
    }

    public static void leaveArena(UUID playerUUID) {
        playersInArena.remove(playerUUID);
    }

    public static boolean isInArena(UUID playerUUID) {
        return playersInArena.containsKey(playerUUID);
    }

    public static String getArenaId(UUID playerUUID) {
        return playersInArena.get(playerUUID);
    }

    // ==================== STATS ====================

    public static void recordKill(String arenaId, String killerName) {
        ArenaStats stats = getOrCreateStats(arenaId, killerName);
        stats.kills++;
        save();
    }

    public static void recordDeath(String arenaId, String victimName) {
        ArenaStats stats = getOrCreateStats(arenaId, victimName);
        stats.deaths++;
        save();
    }

    private static ArenaStats getOrCreateStats(String arenaId, String playerName) {
        Map<String, ArenaStats> board = arenaLeaderboards.computeIfAbsent(arenaId, k -> new ConcurrentHashMap<>());
        return board.computeIfAbsent(playerName, k -> new ArenaStats());
    }

    public static Map<String, ArenaStats> getLeaderboard(String arenaId) {
        return arenaLeaderboards.getOrDefault(arenaId, Collections.emptyMap());
    }

    /** Retourne le top N joueurs tries par kills descending */
    public static List<Map.Entry<String, ArenaStats>> getTopPlayers(String arenaId, int limit) {
        Map<String, ArenaStats> board = arenaLeaderboards.get(arenaId);
        if (board == null) return Collections.emptyList();

        return board.entrySet().stream()
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getValue().kills, a.getValue().kills);
                    if (cmp != 0) return cmp;
                    return Integer.compare(a.getValue().deaths, b.getValue().deaths);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    public static void handleDisconnect(UUID playerUUID) {
        playersInArena.remove(playerUUID);
    }

    // ==================== PERSISTENCE ====================

    public static void save() {
        try {
            Properties props = new Properties();
            props.setProperty("_version", "1");
            for (Map.Entry<String, Map<String, ArenaStats>> arena : arenaLeaderboards.entrySet()) {
                for (Map.Entry<String, ArenaStats> entry : arena.getValue().entrySet()) {
                    String prefix = arena.getKey() + "." + entry.getKey() + ".";
                    props.setProperty(prefix + "kills", String.valueOf(entry.getValue().kills));
                    props.setProperty(prefix + "deaths", String.valueOf(entry.getValue().deaths));
                }
            }

            PersistenceUtils.writeAtomicWithBackup(dataDir.resolve("arena_stats.properties"), props, "Eldanior Arena Stats");
        } catch (Exception e) {
            System.err.println("[ArenaManager] Erreur sauvegarde: " + e.getMessage());
        }
    }

    public static void load() {
        arenaLeaderboards.clear();
        File file = dataDir.resolve("arena_stats.properties").toFile();
        if (!file.exists()) return;

        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            }

            // Version check
            String fileVersion = props.getProperty("_version");
            if (fileVersion == null) {
                System.out.println("[ArenaManager] WARNING: arena_stats.properties n'a pas de _version — fichier ancien, migration future possible.");
            }

            // Format: arenaId.playerName.kills / arenaId.playerName.deaths
            Map<String, Map<String, int[]>> temp = new HashMap<>();
            for (String key : props.stringPropertyNames()) {
                String[] parts = key.split("\\.");
                if (parts.length != 3) continue;
                String arenaId = parts[0];
                String playerName = parts[1];
                String field = parts[2];

                temp.computeIfAbsent(arenaId, k -> new HashMap<>())
                    .computeIfAbsent(playerName, k -> new int[2]);

                int[] vals = temp.get(arenaId).get(playerName);
                if ("kills".equals(field)) vals[0] = Integer.parseInt(props.getProperty(key, "0"));
                else if ("deaths".equals(field)) vals[1] = Integer.parseInt(props.getProperty(key, "0"));
            }

            for (Map.Entry<String, Map<String, int[]>> arena : temp.entrySet()) {
                Map<String, ArenaStats> board = new ConcurrentHashMap<>();
                for (Map.Entry<String, int[]> entry : arena.getValue().entrySet()) {
                    ArenaStats stats = new ArenaStats();
                    stats.kills = entry.getValue()[0];
                    stats.deaths = entry.getValue()[1];
                    board.put(entry.getKey(), stats);
                }
                arenaLeaderboards.put(arena.getKey(), board);
            }
        } catch (Exception e) {
            System.err.println("[ArenaManager] Erreur chargement: " + e.getMessage());
        }
    }

    public static void saveAll() { save(); }

    // ==================== INNER CLASS ====================

    public static class ArenaStats {
        public int kills = 0;
        public int deaths = 0;

        public float getRatio() {
            return deaths == 0 ? kills : (float) kills / deaths;
        }
    }
}
