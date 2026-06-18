package com.eldanior.system.config;

public final class EldaniorLogger {

    /** Permission admin centralisee — utiliser partout au lieu de hardcoder la string */
    public static final String ADMIN_PERMISSION = "eldanior.command.setlevel";

    /** Scheduler partage pour les taches differees (remplace new Timer() par fichier) */
    public static final java.util.concurrent.ScheduledExecutorService SCHEDULER =
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "EldaniorScheduler");
                t.setDaemon(true);
                return t;
            });

    private static final String PREFIX = "[Eldanior] ";
    private static boolean debugMode = false;

    public static void info(String msg) {
        System.out.println(PREFIX + msg);
    }

    public static void warn(String msg) {
        System.err.println(PREFIX + "WARN: " + msg);
    }

    public static void error(String msg) {
        System.err.println(PREFIX + "ERROR: " + msg);
    }

    public static void error(String msg, Throwable e) {
        System.err.println(PREFIX + "ERROR: " + msg + " - " + e.getMessage());
    }

    public static void debug(String msg) {
        if (debugMode) {
            System.out.println(PREFIX + "DEBUG: " + msg);
        }
    }

    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
        info("Mode debug: " + (enabled ? "ACTIVE" : "DESACTIVE"));
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    /**
     * Parse un entier de facon securisee. Retourne defaultValue si la string est null ou non numerique.
     * Evite les NumberFormatException dans les handlers GUI.
     */
    public static int parseSafeInt(String s, int defaultValue) {
        if (s == null || s.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            warn("parseSafeInt: valeur invalide '" + s + "', utilisation de " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * Parse un long de facon securisee. Retourne defaultValue si la string est null ou non numerique.
     */
    public static long parseSafeLong(String s, long defaultValue) {
        if (s == null || s.isEmpty()) return defaultValue;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            warn("parseSafeLong: valeur invalide '" + s + "', utilisation de " + defaultValue);
            return defaultValue;
        }
    }
}
