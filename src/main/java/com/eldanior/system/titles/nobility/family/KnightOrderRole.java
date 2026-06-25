package com.eldanior.system.titles.nobility.family;

public enum KnightOrderRole {
    CAPITAINE("Capitaine", "#FFD700"),
    LIEUTENANT("Lieutenant", "#5b9bd5"),
    MEMBRE("Membre", "#aabbcc");

    private final String displayName;
    private final String colorCode;

    KnightOrderRole(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
    public String getFormattedName() { return colorCode + displayName; }

    public static KnightOrderRole fromString(String name) {
        if (name == null) return null;
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
