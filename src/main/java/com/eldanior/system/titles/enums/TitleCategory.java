package com.eldanior.system.titles.enums;

public enum TitleCategory {
    COMBAT("Combat", "§c"),
    EXPLORATION("Exploration", "§a"),
    SOCIAL("Social", "§b"),
    CRAFT("Artisanat", "§e"),
    QUEST("Quete", "§d"),
    SPECIAL("Special", "§6");

    private final String displayName;
    private final String colorCode;

    TitleCategory(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public String getDisplayName() { return colorCode + displayName; }
    public String getColorCode() { return colorCode; }
}