package com.eldanior.system.titles.nobility;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.church.ChurchManager;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

import java.util.*;

public class NobilityManager {

    // UUID du Roi actuel (null si aucun)
    private static UUID currentKingUUID = null;
    private static String currentKingName = "";

    // Compteurs : combien de chaque rang le Roi a attribue
    private static final Map<NobilityRank, Integer> kingdomCounts = new EnumMap<>(NobilityRank.class);

    // Suzerain de chaque joueur (UUID noble → UUID de son seigneur)
    private static final Map<UUID, UUID> lordOf = new HashMap<>();

    // Chevaliers par noble (UUID noble → liste UUID chevaliers)
    private static final Map<UUID, List<UUID>> knightsOf = new HashMap<>();

    public static void init() {
        for (NobilityRank rank : NobilityRank.values()) {
            kingdomCounts.put(rank, 0);
        }
        System.out.println("[Eldanior] Systeme de Noblesse initialise.");
    }

    // ==================== ROI ====================

    public static UUID getCurrentKingUUID() { return currentKingUUID; }
    public static String getCurrentKingName() { return currentKingName; }

    public static void setKing(UUID uuid, String name) {
        // Si un ancien roi existe, il devient Marquis
        if (currentKingUUID != null && !currentKingUUID.equals(uuid)) {
            // L'ancien roi sera retrogradé par la commande
        }
        currentKingUUID = uuid;
        currentKingName = name;

        // Reset les compteurs du royaume pour le nouveau roi
        for (NobilityRank rank : NobilityRank.values()) {
            kingdomCounts.put(rank, 0);
        }
    }

    // ==================== PROMOTION PAR LE ROI ====================

    public static boolean canKingPromote(NobilityRank rank) {
        if (rank == NobilityRank.ROI || rank == NobilityRank.ROTURIER || rank == NobilityRank.CHEVALIER) {
            return false;
        }
        int current = kingdomCounts.getOrDefault(rank, 0);
        return current < rank.getMaxPerKingdom();
    }

    public static void recordKingPromotion(NobilityRank rank) {
        kingdomCounts.put(rank, kingdomCounts.getOrDefault(rank, 0) + 1);
    }

    public static int getRemainingSlots(NobilityRank rank) {
        return rank.getMaxPerKingdom() - kingdomCounts.getOrDefault(rank, 0);
    }

    // ==================== CHEVALIERS ====================

    public static boolean canPromoteKnight(UUID lordUUID, NobilityRank lordRank) {
        List<UUID> knights = knightsOf.getOrDefault(lordUUID, Collections.emptyList());
        return knights.size() < lordRank.getMaxKnights();
    }

    public static void addKnight(UUID lordUUID, UUID knightUUID) {
        knightsOf.computeIfAbsent(lordUUID, k -> new ArrayList<>()).add(knightUUID);
        lordOf.put(knightUUID, lordUUID);
    }

    public static void removeKnight(UUID knightUUID) {
        UUID lord = lordOf.remove(knightUUID);
        if (lord != null) {
            List<UUID> knights = knightsOf.get(lord);
            if (knights != null) {
                knights.remove(knightUUID);
            }
        }
    }

    public static UUID getLordOf(UUID knightUUID) {
        return lordOf.get(knightUUID);
    }

    public static List<UUID> getKnightsOf(UUID lordUUID) {
        return knightsOf.getOrDefault(lordUUID, Collections.emptyList());
    }

    // ==================== NAMEPLATE ====================

    public static String buildNameplate(String playerName, PlayerLevelData data) {
        NobilityRank nobilityRank = NobilityRank.fromString(data.getNobilityRank());
        if (nobilityRank == null) nobilityRank = NobilityRank.ROTURIER;

        String churchPrefix = ChurchManager.buildChurchPrefix(data);

        boolean hasNobility = nobilityRank.isNoble();
        boolean hasChurch = !churchPrefix.isEmpty();
        boolean isPK = data.isPK();

        if (!hasNobility && !hasChurch && !isPK) {
            return playerName;
        }

        StringBuilder sb = new StringBuilder();

        // PK en premier (rouge vif)
        if (isPK) {
            sb.append("§4[PK] ");
        }

        // Prefixes : [PK] [Rang Noblesse] [Rang Eglise] Nom Von Famille
        if (hasNobility) {
            sb.append(nobilityRank.getColorCode()).append("[").append(nobilityRank.getDisplayName()).append("] ");
        }
        if (hasChurch) {
            sb.append(churchPrefix);
        }

        sb.append("§f").append(playerName);

        String familyId = data.getNobleFamilyId();
        if (familyId != null && !familyId.isEmpty()) {
            NobleFamilyModel family = FamilyManager.get(familyId);
            if (family != null) {
                sb.append(" §7Von ").append(family.getRarity().getColorCode()).append(family.getDisplayName());
            }
        }

        return sb.toString();
    }

    // ==================== DIGNITY ====================

    public static int getDignity(PlayerLevelData data) {
        NobilityRank rank = NobilityRank.fromString(data.getNobilityRank());
        if (rank == null) return 0;
        return rank.getBaseDignity();
    }
}