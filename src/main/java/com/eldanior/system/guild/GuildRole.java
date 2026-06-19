package com.eldanior.system.guild;

public enum GuildRole {
    CHEF("Chef", "§6"),
    OFFICER("Officier", "§e"),
    MEMBER("Membre", "§7");

    private final String displayName;
    private final String colorCode;

    GuildRole(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
    public String getFormattedName() { return colorCode + displayName; }

    public static GuildRole fromString(String name) {
        if (name == null) return null;
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
