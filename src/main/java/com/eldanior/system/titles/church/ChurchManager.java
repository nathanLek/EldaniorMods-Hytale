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
        currentPopeUUID = null;
        currentPopeName = "";
        churchCounts.clear();
        acolytesOf.clear();
        masterOf.clear();
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

    public static void unrecordPopePromotion(ChurchRank rank) {
        int current = churchCounts.getOrDefault(rank, 0);
        if (current > 0) churchCounts.put(rank, current - 1);
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

    // ==================== PERSISTENCE ====================

    public static void saveTo(java.util.Properties props) {
        if (currentPopeUUID != null) {
            props.setProperty("church.pope.uuid", currentPopeUUID.toString());
            props.setProperty("church.pope.name", currentPopeName);
        }
        for (Map.Entry<ChurchRank, Integer> e : churchCounts.entrySet()) {
            props.setProperty("church.count." + e.getKey().name(), String.valueOf(e.getValue()));
        }
        int idx = 0;
        for (Map.Entry<UUID, UUID> e : masterOf.entrySet()) {
            props.setProperty("church.master." + idx + ".acolyte", e.getKey().toString());
            props.setProperty("church.master." + idx + ".master", e.getValue().toString());
            idx++;
        }
        props.setProperty("church.master.count", String.valueOf(idx));
    }

    public static void loadFrom(java.util.Properties props) {
        String popeUUID = props.getProperty("church.pope.uuid");
        if (popeUUID != null && !popeUUID.isEmpty()) {
            try {
                currentPopeUUID = UUID.fromString(popeUUID);
                currentPopeName = props.getProperty("church.pope.name", "");
            } catch (Exception e) { /* skip */ }
        }
        for (ChurchRank rank : ChurchRank.values()) {
            String val = props.getProperty("church.count." + rank.name());
            if (val != null) {
                try { churchCounts.put(rank, Integer.parseInt(val)); } catch (Exception e) { /* skip */ }
            }
        }
        int count = 0;
        try { count = Integer.parseInt(props.getProperty("church.master.count", "0")); } catch (Exception e) { /* skip */ }
        for (int i = 0; i < count; i++) {
            String acolyteStr = props.getProperty("church.master." + i + ".acolyte");
            String masterStr = props.getProperty("church.master." + i + ".master");
            if (acolyteStr != null && masterStr != null) {
                try {
                    UUID acolyteUUID = UUID.fromString(acolyteStr);
                    UUID masterUUID = UUID.fromString(masterStr);
                    masterOf.put(acolyteUUID, masterUUID);
                    acolytesOf.computeIfAbsent(masterUUID, k -> new ArrayList<>()).add(acolyteUUID);
                } catch (Exception e) { /* skip */ }
            }
        }
    }

    // ==================== FAITH ====================

    public static int getFaith(PlayerLevelData data) {
        ChurchRank rank = ChurchRank.fromString(data.getChurchRank());
        if (rank == null) return 0;
        return rank.getBaseFaith();
    }
}