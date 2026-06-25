package com.eldanior.system.titles.church;

public enum ChurchRank {
    LAIQUE("Laique", "", 0, 0, 0),
    RELIGIEUX("Religieux", "", 5, 0, 0),
    PRETRE("Pretre", "", 15, 1, 4),
    ARCHEVEQUE("Archeveque", "", 30, 2, 3),
    CARDINAL("Cardinal", "", 50, 3, 2),
    SAINT("Saint", "", 80, 0, 0),         // Ne peut pas etre attribue, uniquement via classe Saint
    PAPE("Pape", "", 100, 10, 0);

    private final String displayName;
    private final String colorCode;
    private final int baseFaith;
    private final int maxAcolytes;      // Nombre de religieux que ce rang peut promouvoir
    private final int maxPerChurch;     // Nombre max de ce rang que le Pape peut attribuer

    ChurchRank(String displayName, String colorCode, int baseFaith, int maxAcolytes, int maxPerChurch) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.baseFaith = baseFaith;
        this.maxAcolytes = maxAcolytes;
        this.maxPerChurch = maxPerChurch;
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
    public String getFormattedName() { return colorCode + displayName; }
    public int getBaseFaith() { return baseFaith; }
    public int getMaxAcolytes() { return maxAcolytes; }
    public int getMaxPerChurch() { return maxPerChurch; }

    public boolean isClergy() { return this != LAIQUE; }

    public ChurchRank next() {
        ChurchRank[] ranks = values();
        int idx = this.ordinal();
        return (idx + 1 < ranks.length) ? ranks[idx + 1] : null;
    }

    public ChurchRank previous() {
        int idx = this.ordinal();
        return (idx > 0) ? values()[idx - 1] : LAIQUE;
    }

    public static ChurchRank fromString(String name) {
        if (name == null) return null;
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}