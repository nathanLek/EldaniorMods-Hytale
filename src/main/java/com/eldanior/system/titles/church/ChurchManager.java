package com.eldanior.system.titles.church;

import com.eldanior.system.config.Player.PlayerLevelData;

import java.util.*;

public class ChurchManager {

    private static UUID currentPopeUUID = null;
    private static String currentPopeName = "";

    private static final Map<ChurchRank, Integer> churchCounts = new EnumMap<>(ChurchRank.class);
    private static final Map<UUID, List<UUID>> acolytesOf = new HashMap<>();
    private static final Map<UUID, UUID> masterOf = new HashMap<>();

    public static void init() {
        for (ChurchRank rank : ChurchRank.values()) {
            churchCounts.put(rank, 0);
        }
        System.out.println("[Eldanior] Systeme de l'Eglise initialise.");
    }

    // ==================== PAPE ====================

    public static UUID getCurrentPopeUUID() { return currentPopeUUID; }
    public static String getCurrentPopeName() { return currentPopeName; }

    public static void setPope(UUID uuid, String name) {
        currentPopeUUID = uuid;
        currentPopeName = name;
        for (ChurchRank rank : ChurchRank.values()) {
            churchCounts.put(rank, 0);
        }
    }

    // ==================== PROMOTION PAR LE PAPE ====================

    public static boolean canPopePromote(ChurchRank rank) {
        if (rank == ChurchRank.PAPE || rank == ChurchRank.LAIQUE || rank == ChurchRank.RELIGIEUX || rank == ChurchRank.SAINT) {
            return false;
        }
        int current = churchCounts.getOrDefault(rank, 0);
        return current < rank.getMaxPerChurch();
    }

    public static void recordPopePromotion(ChurchRank rank) {
        churchCounts.put(rank, churchCounts.getOrDefault(rank, 0) + 1);
    }

    public static int getRemainingSlots(ChurchRank rank) {
        return rank.getMaxPerChurch() - churchCounts.getOrDefault(rank, 0);
    }

    // ==================== ACOLYTES ====================

    public static boolean canPromoteAcolyte(UUID masterUUID, ChurchRank masterRank) {
        List<UUID> acolytes = acolytesOf.getOrDefault(masterUUID, Collections.emptyList());
        return acolytes.size() < masterRank.getMaxAcolytes();
    }

    public static void addAcolyte(UUID masterUUID, UUID acolyteUUID) {
        acolytesOf.computeIfAbsent(masterUUID, k -> new ArrayList<>()).add(acolyteUUID);
        masterOf.put(acolyteUUID, masterUUID);
    }

    public static void removeAcolyte(UUID acolyteUUID) {
        UUID master = masterOf.remove(acolyteUUID);
        if (master != null) {
            List<UUID> acolytes = acolytesOf.get(master);
            if (acolytes != null) acolytes.remove(acolyteUUID);
        }
    }

    public static UUID getMasterOf(UUID acolyteUUID) { return masterOf.get(acolyteUUID); }
    public static List<UUID> getAcolytesOf(UUID masterUUID) { return acolytesOf.getOrDefault(masterUUID, Collections.emptyList()); }

    // ==================== NAMEPLATE ====================

    public static String buildChurchPrefix(PlayerLevelData data) {
        ChurchRank rank = ChurchRank.fromString(data.getChurchRank());
        if (rank == null || !rank.isClergy()) return "";
        return rank.getColorCode() + "[" + rank.getDisplayName() + "] ";
    }

    // ==================== FAITH ====================

    public static int getFaith(PlayerLevelData data) {
        ChurchRank rank = ChurchRank.fromString(data.getChurchRank());
        if (rank == null) return 0;
        return rank.getBaseFaith();
    }
}