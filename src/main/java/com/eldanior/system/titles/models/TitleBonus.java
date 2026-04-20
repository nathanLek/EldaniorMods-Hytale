package com.eldanior.system.titles.models;

/**
 * Bonus de stats fixes accordes par un titre.
 * Tous les champs sont additifs aux stats du joueur.
 */
public record TitleBonus(
        int strength,
        int vitality,
        int intelligence,
        int endurance,
        int agility,
        int luck
) {
    public static final TitleBonus NONE = new TitleBonus(0, 0, 0, 0, 0, 0);
}