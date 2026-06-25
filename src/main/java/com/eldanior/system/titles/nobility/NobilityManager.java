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
        currentKingUUID = null;
        currentKingName = "";
        kingdomCounts.clear();
        lordOf.clear();
        knightsOf.clear();
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

    // ==================== PROMOTIONS ====================

    // Compteurs de promotions par rang du promoteur
    // Cle: "ROI:MARQUIS" = combien de Marquis le Roi a nommes
    private static final Map<String, Integer> promotionCounts = new HashMap<>();

    private static String promoKey(NobilityRank promoterRank, NobilityRank targetRank) {
        return promoterRank.name() + ":" + targetRank.name();
    }

    /**
     * Verifie si un noble de ce rang peut encore promouvoir au rang cible.
     */
    public static boolean canPromote(NobilityRank promoterRank, NobilityRank targetRank) {
        int max = promoterRank.getMaxPromotions(targetRank);
        if (max <= 0) return false;
        int current = promotionCounts.getOrDefault(promoKey(promoterRank, targetRank), 0);
        return current < max;
    }

    public static void recordPromotion(NobilityRank promoterRank, NobilityRank targetRank) {
        String key = promoKey(promoterRank, targetRank);
        promotionCounts.put(key, promotionCounts.getOrDefault(key, 0) + 1);
        // Backwards compat: aussi dans kingdomCounts si c'est le Roi
        if (promoterRank == NobilityRank.ROI) {
            kingdomCounts.put(targetRank, kingdomCounts.getOrDefault(targetRank, 0) + 1);
        }
    }

    public static int getRemainingSlots(NobilityRank promoterRank, NobilityRank targetRank) {
        int max = promoterRank.getMaxPromotions(targetRank);
        int current = promotionCounts.getOrDefault(promoKey(promoterRank, targetRank), 0);
        return Math.max(0, max - current);
    }

    // Backwards compat pour le systeme existant
    public static boolean canKingPromote(NobilityRank rank) {
        return canPromote(NobilityRank.ROI, rank);
    }

    public static void recordKingPromotion(NobilityRank rank) {
        recordPromotion(NobilityRank.ROI, rank);
    }

    public static void unrecordKingPromotion(NobilityRank rank) {
        String key = promoKey(NobilityRank.ROI, rank);
        int current = promotionCounts.getOrDefault(key, 0);
        if (current > 0) promotionCounts.put(key, current - 1);
        int kc = kingdomCounts.getOrDefault(rank, 0);
        if (kc > 0) kingdomCounts.put(rank, kc - 1);
    }

    public static int getRemainingSlots(NobilityRank rank) {
        return getRemainingSlots(NobilityRank.ROI, rank);
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
            sb.append("[PK] ");
        }

        // Prefixes : [PK] [Rang Noblesse] [Rang Eglise] Nom Von Famille
        if (hasNobility) {
            sb.append(nobilityRank.getColorCode()).append("[").append(nobilityRank.getDisplayName()).append("] ");
        }
        if (hasChurch) {
            sb.append(churchPrefix);
        }

        sb.append(playerName);

        String familyId = data.getNobleFamilyId();
        if (familyId != null && !familyId.isEmpty()) {
            NobleFamilyModel family = FamilyManager.get(familyId);
            if (family != null) {
                sb.append(" Von ").append(family.getDisplayName());
            }
        }

        return sb.toString();
    }

    // ==================== PERSISTENCE ====================

    public static void saveTo(java.util.Properties props) {
        if (currentKingUUID != null) {
            props.setProperty("nobility.king.uuid", currentKingUUID.toString());
            props.setProperty("nobility.king.name", currentKingName);
        }
        for (Map.Entry<NobilityRank, Integer> e : kingdomCounts.entrySet()) {
            props.setProperty("nobility.count." + e.getKey().name(), String.valueOf(e.getValue()));
        }
        int lordIdx = 0;
        for (Map.Entry<UUID, UUID> e : lordOf.entrySet()) {
            props.setProperty("nobility.lord." + lordIdx + ".knight", e.getKey().toString());
            props.setProperty("nobility.lord." + lordIdx + ".lord", e.getValue().toString());
            lordIdx++;
        }
        props.setProperty("nobility.lord.count", String.valueOf(lordIdx));

        // Persist promotionCounts
        int promoIdx = 0;
        for (Map.Entry<String, Integer> e : promotionCounts.entrySet()) {
            props.setProperty("nobility.promo." + promoIdx + ".key", e.getKey());
            props.setProperty("nobility.promo." + promoIdx + ".val", String.valueOf(e.getValue()));
            promoIdx++;
        }
        props.setProperty("nobility.promo.count", String.valueOf(promoIdx));
    }

    public static void loadFrom(java.util.Properties props) {
        String kingUUID = props.getProperty("nobility.king.uuid");
        if (kingUUID != null && !kingUUID.isEmpty()) {
            try {
                currentKingUUID = UUID.fromString(kingUUID);
                currentKingName = props.getProperty("nobility.king.name", "");
            } catch (Exception e) { /* ignore invalid UUID */ }
        }
        for (NobilityRank rank : NobilityRank.values()) {
            String val = props.getProperty("nobility.count." + rank.name());
            if (val != null) {
                try { kingdomCounts.put(rank, Integer.parseInt(val)); } catch (Exception e) { /* skip */ }
            }
        }
        int lordCount = 0;
        try { lordCount = Integer.parseInt(props.getProperty("nobility.lord.count", "0")); } catch (Exception e) { /* skip */ }
        for (int i = 0; i < lordCount; i++) {
            String knightStr = props.getProperty("nobility.lord." + i + ".knight");
            String lordStr = props.getProperty("nobility.lord." + i + ".lord");
            if (knightStr != null && lordStr != null) {
                try {
                    UUID knightUUID = UUID.fromString(knightStr);
                    UUID lordUUID = UUID.fromString(lordStr);
                    lordOf.put(knightUUID, lordUUID);
                    knightsOf.computeIfAbsent(lordUUID, k -> new ArrayList<>()).add(knightUUID);
                } catch (Exception e) { /* skip invalid */ }
            }
        }

        // Load promotionCounts
        int promoCount = 0;
        try { promoCount = Integer.parseInt(props.getProperty("nobility.promo.count", "0")); } catch (Exception e) { /* skip */ }
        for (int i = 0; i < promoCount; i++) {
            String key = props.getProperty("nobility.promo." + i + ".key");
            String val = props.getProperty("nobility.promo." + i + ".val");
            if (key != null && val != null) {
                try { promotionCounts.put(key, Integer.parseInt(val)); } catch (Exception e) { /* skip */ }
            }
        }
    }

    // ==================== DIGNITY ====================

    public static int getDignity(PlayerLevelData data) {
        NobilityRank rank = NobilityRank.fromString(data.getNobilityRank());
        if (rank == null) return 0;
        return rank.getBaseDignity();
    }
}