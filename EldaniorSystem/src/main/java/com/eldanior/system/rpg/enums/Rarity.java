package com.eldanior.system.rpg.enums;

public enum Rarity {
    COMMON("Commun", "§f"),      // Blanc
    RARE("Rare", "§9"),          // Bleu
    EPIC("Epique", "§5"),        // Violet
    UNIQUE("Unique", "§e"),      // Jaune
    LEGENDARY("Legendaire", "§6"), // Orange/Or
    DIVINE("Divin", "§c");       // Rouge

    private final String displayName;
    private final String colorCode;

    Rarity(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return colorCode + displayName;
    }

    public String getColorCode() {
        return colorCode;
    }
}