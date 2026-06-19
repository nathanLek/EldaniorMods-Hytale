package com.eldanior.system.titles.nobility;

import java.util.Map;
import java.util.EnumMap;

public enum NobilityRank {
    ROTURIER("Roturier", "§7", 0),
    CHEVALIER("Chevalier", "§f", 5),
    BARON("Baron", "§a", 15),
    COMTE("Comte", "§9", 30),
    DUC("Duc", "§5", 50),
    MARQUIS("Marquis", "§6", 75),
    ROI("Roi", "§c", 100);

    private final String displayName;
    private final String colorCode;
    private final int baseDignity;

    // Quotas de promotion par rang (initialises dans le bloc static)
    private Map<NobilityRank, Integer> promotionQuotas;

    NobilityRank(String displayName, String colorCode, int baseDignity) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.baseDignity = baseDignity;
    }

    static {
        // ROI : 1 marquis, 2 ducs, 3 comtes, 4 barons, 10 chevaliers
        ROI.promotionQuotas = Map.of(
                MARQUIS, 1, DUC, 2, COMTE, 3, BARON, 4, CHEVALIER, 10
        );

        // MARQUIS : 2 comtes, 1 baron, 4 chevaliers
        MARQUIS.promotionQuotas = Map.of(
                COMTE, 2, BARON, 1, CHEVALIER, 4
        );

        // DUC : 1 baron, 3 chevaliers
        DUC.promotionQuotas = Map.of(
                BARON, 1, CHEVALIER, 3
        );

        // COMTE : 2 chevaliers
        COMTE.promotionQuotas = Map.of(
                CHEVALIER, 2
        );

        // Les autres ne peuvent rien promouvoir
        BARON.promotionQuotas = Map.of();
        CHEVALIER.promotionQuotas = Map.of();
        ROTURIER.promotionQuotas = Map.of();
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
    public String getFormattedName() { return colorCode + displayName; }
    public int getBaseDignity() { return baseDignity; }

    /**
     * Nombre max de promotions de ce type que ce rang peut donner.
     */
    public int getMaxPromotions(NobilityRank targetRank) {
        return promotionQuotas.getOrDefault(targetRank, 0);
    }

    /**
     * Retourne tous les quotas de promotion pour ce rang.
     */
    public Map<NobilityRank, Integer> getPromotionQuotas() {
        return promotionQuotas;
    }

    // Backwards compat
    public int getMaxKnights() { return getMaxPromotions(CHEVALIER); }
    public int getMaxPerKingdom() {
        // Utilise par le systeme de decrets du Roi
        return ROI.getMaxPromotions(this);
    }

    public boolean isNoble() { return this != ROTURIER; }

    public NobilityRank next() {
        NobilityRank[] ranks = values();
        int idx = this.ordinal();
        return (idx + 1 < ranks.length) ? ranks[idx + 1] : null;
    }

    public NobilityRank previous() {
        int idx = this.ordinal();
        return (idx > 0) ? values()[idx - 1] : ROTURIER;
    }

    public static NobilityRank fromString(String name) {
        if (name == null) return null;
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
