package com.eldanior.system.titles.nobility;

public enum NobilityRank {
    ROTURIER("Roturier", "§7", 0, 0, 0),
    CHEVALIER("Chevalier", "§f", 5, 0, 0),
    BARON("Baron", "§a", 15, 1, 1),
    COMTE("Comte", "§9", 30, 2, 2),
    DUC("Duc", "§5", 50, 3, 3),
    MARQUIS("Marquis", "§6", 75, 4, 4),
    ROI("Roi", "§c", 100, 10, 0);

    private final String displayName;
    private final String colorCode;
    private final int baseDignity;
    private final int maxKnights;       // Nombre de chevaliers que ce rang peut promouvoir
    private final int maxPerKingdom;    // Nombre max de ce rang que le Roi peut attribuer

    NobilityRank(String displayName, String colorCode, int baseDignity, int maxKnights, int maxPerKingdom) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.baseDignity = baseDignity;
        this.maxKnights = maxKnights;
        this.maxPerKingdom = maxPerKingdom;
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
    public String getFormattedName() { return colorCode + displayName; }
    public int getBaseDignity() { return baseDignity; }
    public int getMaxKnights() { return maxKnights; }
    public int getMaxPerKingdom() { return maxPerKingdom; }

    public boolean isNoble() { return this != ROTURIER; }

    /**
     * Retourne le rang superieur, ou null si deja Roi.
     */
    public NobilityRank next() {
        NobilityRank[] ranks = values();
        int idx = this.ordinal();
        return (idx + 1 < ranks.length) ? ranks[idx + 1] : null;
    }

    /**
     * Retourne le rang inferieur, ou ROTURIER si deja au minimum.
     */
    public NobilityRank previous() {
        int idx = this.ordinal();
        return (idx > 0) ? values()[idx - 1] : ROTURIER;
    }

    public static NobilityRank fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}