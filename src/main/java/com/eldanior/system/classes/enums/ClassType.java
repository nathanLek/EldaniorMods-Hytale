package com.eldanior.system.classes.enums;

public enum ClassType {
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