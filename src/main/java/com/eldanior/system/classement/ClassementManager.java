package com.eldanior.system.classement;

import com.eldanior.system.guild.Guild;
import com.eldanior.system.guild.GuildManager;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stockage persistant des scores de classement.
 * Les scores sont mis a jour en temps reel (kills, morts, etc.)
 * et conserves meme quand le joueur se deconnecte.
 */
public class ClassementManager {

    // Joueur -> total mob kills
    private static final Map<String, Long> mobKillsBoard = new ConcurrentHashMap<>();

    // Joueur -> total player kills
    private static final Map<String, Long> pvpKillsBoard = new ConcurrentHashMap<>();

    // Joueur -> duel wins
    private static final Map<String, Long> duelWinsBoard = new ConcurrentHashMap<>();

    public static void init() {
        System.out.println("[Eldanior] Systeme de Classements initialise.");
    }

    // ==================== UPDATE ====================

    public static void updateMobKills(String playerName, long totalKills) {
        mobKillsBoard.put(playerName, totalKills);
    }

    public static void updatePvPKills(String playerName, long totalKills) {
        pvpKillsBoard.put(playerName, totalKills);
    }

    public static void updateDuelWins(String playerName, long totalWins) {
        duelWinsBoard.put(playerName, totalWins);
    }

    // ==================== GET RANKINGS ====================

    public static List<RankEntry> getMobRanking(int max) {
        return getSortedEntries(mobKillsBoard, max);
    }

    public static List<RankEntry> getPvPRanking(int max) {
        return getSortedEntries(pvpKillsBoard, max);
    }

    public static List<RankEntry> getDuelRanking(int max) {
        return getSortedEntries(duelWinsBoard, max);
    }

    public static List<RankEntry> getGuildFamilyRanking(int max) {
        List<RankEntry> entries = new ArrayList<>();

        // Guildes: score = contribution + tresorerie / 100
        for (Guild guild : GuildManager.getAll()) {
            long score = guild.getContribution() + (guild.getTreasury() / 100);
            if (score > 0 || guild.getMemberCount() > 0) {
                entries.add(new RankEntry("[G] " + guild.getName(), score));
            }
        }

        // Familles: score = contribution + tresorerie / 100
        for (NobleFamilyModel family : FamilyManager.getAll()) {
            FamilyManager.FamilyRuntimeData runtime = FamilyManager.getRuntimeData(family.getId());
            long score = runtime.getContribution() + (runtime.getTreasury() / 100);
            if (score > 0) {
                entries.add(new RankEntry("[F] Von " + family.getDisplayName(), score));
            }
        }

        entries.sort((a, b) -> Long.compare(b.value(), a.value()));
        return entries.subList(0, Math.min(max, entries.size()));
    }

    // ==================== UTILS ====================

    private static List<RankEntry> getSortedEntries(Map<String, Long> board, int max) {
        List<RankEntry> entries = new ArrayList<>();
        for (var e : board.entrySet()) {
            if (e.getValue() > 0) {
                entries.add(new RankEntry(e.getKey(), e.getValue()));
            }
        }
        entries.sort((a, b) -> Long.compare(b.value(), a.value()));
        return entries.subList(0, Math.min(max, entries.size()));
    }

    public record RankEntry(String name, long value) {}
}
