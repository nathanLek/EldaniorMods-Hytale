package com.eldanior.system.rpg.enums;

public enum ClassType {
    WARRIOR("Guerrier"),
    MAGE("Mage"),
    ASSASSIN("Assassin"),
    ARCHER("Archer"),
    MERCHANT("Marchand");

    private final String label;

    ClassType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}