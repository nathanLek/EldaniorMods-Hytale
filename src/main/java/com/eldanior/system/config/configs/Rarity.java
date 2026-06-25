package com.eldanior.system.config.configs;

public enum Rarity {
    COMMON("Commun", ""),
    RARE("Rare", ""),
    EPIC("Epique", ""),
    UNIQUE("Unique", ""),
    LEGENDARY("Legendaire", ""),
    DIVINE("Divin", "");

    private final String displayName;
    private final String colorCode;

    Rarity(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }
}
