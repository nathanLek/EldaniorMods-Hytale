package com.eldanior.system.titles.enums;

public enum TitleCategory {
    COMBAT("Combat", "§c"),
    PVP("PvP", "§4"),
    EXPLORATION("Exploration", "§a"),
    SOCIAL("Social", "§b"),
    CRAFT("Artisanat", "§e"),
    QUEST("Quete", "§d"),
    PROGRESSION("Progression", "§2"),
    ECONOMIE("Economie", "§6"),
    DIGNITE("Dignite", "§5"),
    FOI("Foi", "§f"),
    DUEL("Duel", "§3"),
    SURVIE("Survie", "§7"),
    STATS("Stats", "§9"),
    CLASSE("Classe", "§1"),
    COMPETENCES("Competences", "§d"),
    TERRITOIRE("Territoire", "§2"),
    GUILDE("Guilde", "§b"),
    FAMILLE("Famille", "§5"),
    COLLECTION("Collection", "§6"),
    LEGENDAIRE("Legendaire", "§e"),
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