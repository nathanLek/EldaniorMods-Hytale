package com.eldanior.system.economy;

import com.eldanior.system.config.EldaniorLogger;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Logger centralise pour toutes les transactions economiques.
 * Ecrit dans des fichiers rotatifs journaliers (transactions_YYYY-MM-DD.log).
 * Thread-safe : l'ecriture est synchronisee.
 */
public class TransactionLogger {

    private static Path logDir;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Object WRITE_LOCK = new Object();

    // Retention : nombre de jours de logs a conserver
    private static final int RETENTION_DAYS = 30;

    public static void init(Path dataDirectory) {
        logDir = dataDirectory.resolve("transactions");
        try {
            Files.createDirectories(logDir);
        } catch (IOException e) {
            EldaniorLogger.error("TransactionLogger: impossible de creer " + logDir, e);
        }
        cleanOldLogs();
        EldaniorLogger.info("TransactionLogger initialise (" + logDir + ")");
    }

    // ==================== LOG METHODS ====================

    public static void logShopBuy(String buyerName, String sellerName, String itemId,
                                   int quantity, long price, long tax) {
        log("SHOP_BUY", buyerName + " achete " + quantity + "x " + itemId
                + " a " + sellerName + " pour " + price + " Or (taxe: " + tax + ")");
    }

    public static void logShopCancel(String playerName, String itemId, int quantity, long price, boolean adminAction) {
        String type = adminAction ? "SHOP_CANCEL_ADMIN" : "SHOP_CANCEL";
        log(type, playerName + " retire " + quantity + "x " + itemId + " (prix: " + price + " Or)");
    }

    public static void logTrade(String player1Name, String player2Name,
                                 String[] p1Items, String[] p2Items) {
        String p1Str = p1Items.length > 0 ? String.join(", ", p1Items) : "(rien)";
        String p2Str = p2Items.length > 0 ? String.join(", ", p2Items) : "(rien)";
        log("TRADE", player1Name + " [" + p1Str + "] <-> " + player2Name + " [" + p2Str + "]");
    }

    public static void logTax(String parcelName, String parcelType, long amount, String recipientName) {
        log("TAX", amount + " Or preleve de " + parcelName + " (" + parcelType + ")"
                + " -> " + recipientName);
    }

    public static void logTaxDistribution(long taxAmount, String cityName, long cityShare,
                                           String territoryInfo) {
        log("TAX_DISTRIB", taxAmount + " Or distribue : " + cityShare + " -> " + cityName
                + (territoryInfo != null ? " | " + territoryInfo : ""));
    }

    public static void logRent(String playerName, String parcelName, long amount) {
        log("RENT", playerName + " loue " + parcelName + " pour " + amount + " Or");
    }

    public static void logRentEviction(String parcelName, String parcelId) {
        log("RENT_EVICT", "Location expiree : " + parcelName + " (id=" + parcelId + ")");
    }

    public static void logGuildTreasury(String guildName, long amount, String type) {
        log("GUILD_TREASURY", guildName + " " + type + " " + amount + " Or");
    }

    public static void logFamilyTreasury(String familyId, long amount, String source) {
        log("FAMILY_TREASURY", familyId + " recoit " + amount + " Or de " + source);
    }

    // ==================== READING (for admin command) ====================

    /**
     * Lit les N dernieres lignes du fichier de log du jour.
     * Retourne une liste vide si aucun log n'existe.
     */
    public static List<String> getRecentLines(int count) {
        if (logDir == null) return Collections.emptyList();

        Path todayFile = logDir.resolve("transactions_" + LocalDate.now().format(DATE_FMT) + ".log");
        if (!Files.exists(todayFile)) {
            // Essayer le fichier d'hier
            Path yesterdayFile = logDir.resolve("transactions_" + LocalDate.now().minusDays(1).format(DATE_FMT) + ".log");
            if (!Files.exists(yesterdayFile)) return Collections.emptyList();
            todayFile = yesterdayFile;
        }

        try {
            List<String> allLines = Files.readAllLines(todayFile);
            int start = Math.max(0, allLines.size() - count);
            return allLines.subList(start, allLines.size());
        } catch (IOException e) {
            EldaniorLogger.error("TransactionLogger: erreur lecture", e);
            return Collections.emptyList();
        }
    }

    // ==================== INTERNAL ====================

    private static void log(String type, String details) {
        if (logDir == null) return;

        String timestamp = LocalTime.now().format(TIME_FMT);
        String line = "[" + timestamp + "] " + type + " | " + details;

        synchronized (WRITE_LOCK) {
            Path file = logDir.resolve("transactions_" + LocalDate.now().format(DATE_FMT) + ".log");
            try (BufferedWriter writer = Files.newBufferedWriter(file,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(line);
                writer.newLine();
            } catch (IOException e) {
                EldaniorLogger.error("TransactionLogger: erreur ecriture", e);
            }
        }
    }

    /**
     * Supprime les fichiers de log de plus de RETENTION_DAYS jours.
     */
    private static void cleanOldLogs() {
        if (logDir == null) return;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(logDir, "transactions_*.log")) {
            LocalDate cutoff = LocalDate.now().minusDays(RETENTION_DAYS);
            for (Path file : stream) {
                String name = file.getFileName().toString();
                // transactions_2026-06-19.log -> 2026-06-19
                String dateStr = name.replace("transactions_", "").replace(".log", "");
                try {
                    LocalDate fileDate = LocalDate.parse(dateStr, DATE_FMT);
                    if (fileDate.isBefore(cutoff)) {
                        Files.delete(file);
                        EldaniorLogger.info("TransactionLogger: supprime ancien log " + name);
                    }
                } catch (Exception ignored) {
                    // Nom de fichier non parseable, on ignore
                }
            }
        } catch (IOException e) {
            EldaniorLogger.error("TransactionLogger: erreur nettoyage", e);
        }
    }
}
