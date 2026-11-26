package com.eldanior.system.config.configs;

public enum ClassType {
    NOVICE("Novice"),
    WARRIOR("Guerrier"),
    MAGE("Mage"),
    ASSASSIN("Assassin"),
    ARCHER("Archer"),
    MERCHANT("Marchand"),
    MOUSQUETAIRE("Mousquetaire"),   // seule a manier les armes a feu ; evolution de l'Archer depuis le 2026-11-16
    DRAGON("Dragon Ancestral"),
    OBSERVATEUR("Observateur");        // classe unique, donnee par le Roi ou l'Empereur Luo (2026-11-16) : toutes armes et runes

    private final String label;

    ClassType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}