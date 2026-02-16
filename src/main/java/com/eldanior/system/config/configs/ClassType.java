package com.eldanior.system.config.configs;

public enum ClassType {
    NOVICE("Novice"),
    WARRIOR("Guerrier"),
    MAGE("Mage"),
    ASSASSIN("Assassin"),
    ARCHER("Archer"),
    MERCHANT("Marchand"),
    DRAGON("Dragon Ancestral");

    private final String label;

    ClassType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}