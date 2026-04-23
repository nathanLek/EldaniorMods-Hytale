package com.eldanior.system.quest;

public enum QuestType {
    CHASSE("Chasse", "#4CAF50"),           // Tuer X mobs d'un type
    MASSACRE("Massacre", "#cc4444"),        // Tuer X mobs total
    EXPLORATION("Exploration", "#8B7355"),  // Decouvrir X coffres
    COLLECTION("Collection", "#D4AF37"),   // Avoir X or
    DUEL("Duel", "#ff9800"),               // Gagner X duels
    EXECUTION("Execution", "#cc4444");     // Tuer un PK specifique

    private final String displayName;
    private final String color;

    QuestType(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
}
