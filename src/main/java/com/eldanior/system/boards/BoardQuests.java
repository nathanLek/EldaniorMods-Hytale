package com.eldanior.system.boards;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.PersistenceUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quetes ajoutees par un admin a un tableau des quetes (2026-11-13) : meme format que Post.quest d'un PNJ
 * (« chapitres:id1,id2 », guards.NpcServices), par tableau (cle « tableau@monde:x:y:z »). Sauvegarde :
 * eldanior_data/board_quests.properties.
 */
public final class BoardQuests {

    private BoardQuests() {}

    public static final String PREFIX = "tableau@";
    private static final Map<String, String> ENTRIES = new ConcurrentHashMap<>();
    private static Path file;

    public static String key(String world, int x, int y, int z) { return PREFIX + world + ":" + x + ":" + y + ":" + z; }

    public static boolean isBoard(String id) { return id != null && id.startsWith(PREFIX); }

    public static String get(String key) { return ENTRIES.get(key); }

    public static void set(String key, String value) {
        if (value == null || value.isEmpty()) ENTRIES.remove(key);
        else ENTRIES.put(key, value);
        save();
    }

    /** Tableau casse : ses quetes ajoutees sont oubliees. */
    public static void forget(String key) { if (ENTRIES.remove(key) != null) save(); }

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("board_quests.properties");
        if (!Files.exists(file)) return;
        try (InputStream in = Files.newInputStream(file)) {
            Properties p = new Properties();
            p.load(in);
            for (String k : p.stringPropertyNames()) ENTRIES.put(k, p.getProperty(k));
        } catch (Exception e) {
            EldaniorLogger.error("[Boards] chargement des quetes des tableaux", e);
        }
    }

    private static synchronized void save() {
        if (file == null) return;
        try {
            Files.createDirectories(file.getParent());
            Properties p = new Properties();
            ENTRIES.forEach(p::setProperty);
            PersistenceUtils.writeAtomicWithBackup(file, p, "Eldanior - quetes des tableaux");
        } catch (Exception e) {
            EldaniorLogger.error("[Boards] sauvegarde des quetes des tableaux", e);
        }
    }
}
