package com.eldanior.system.quest;

public enum QuestCategory {
    PRINCIPAL("Quete Principale", "#FFD700"),
    SECONDAIRE("Quete Secondaire", "#C8A2C8"),
    JOURNALIERE("Journaliere", "#4CAF50");

    private final String displayName;
    private final String color;

    QuestCategory(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
}
