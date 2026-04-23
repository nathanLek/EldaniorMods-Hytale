package com.eldanior.system.quest;

public enum QuestDifficulty {
    F("F", "#8899aa", 1.0),
    E("E", "#4CAF50", 1.2),
    D("D", "#2196F3", 1.5),
    C("C", "#9C27B0", 2.0),
    B("B", "#ff9800", 2.5),
    A("A", "#cc4444", 3.5),
    S("S", "#FFD700", 5.0);

    private final String displayName;
    private final String color;
    private final double rewardMultiplier;

    QuestDifficulty(String displayName, String color, double rewardMultiplier) {
        this.displayName = displayName;
        this.color = color;
        this.rewardMultiplier = rewardMultiplier;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public double getRewardMultiplier() { return rewardMultiplier; }
}
