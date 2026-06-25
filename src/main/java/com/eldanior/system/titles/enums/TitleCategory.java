package com.eldanior.system.titles.enums;

public enum TitleCategory {
    COMBAT("Combat", ""),
    PVP("PvP", ""),
    EXPLORATION("Exploration", ""),
    SOCIAL("Social", ""),
    CRAFT("Artisanat", ""),
    QUEST("Quete", ""),
    PROGRESSION("Progression", ""),
    ECONOMIE("Economie", ""),
    DIGNITE("Dignite", ""),
    FOI("Foi", ""),
    DUEL("Duel", ""),
    SURVIE("Survie", ""),
    STATS("Stats", ""),
    CLASSE("Classe", ""),
    COMPETENCES("Competences", ""),
    TERRITOIRE("Territoire", ""),
    GUILDE("Guilde", ""),
    FAMILLE("Famille", ""),
    COLLECTION("Collection", ""),
    LEGENDAIRE("Legendaire", ""),
    SPECIAL("Special", "");

    private final String displayName;
    private final String colorCode;

    TitleCategory(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
}
