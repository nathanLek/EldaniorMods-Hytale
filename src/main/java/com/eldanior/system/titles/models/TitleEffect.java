package com.eldanior.system.titles.models;

/**
 * Represente un effet special accorde par un titre.
 * Exemple : +10% de degats contre les Goblins.
 */
public record TitleEffect(
        TitleEffectType type,
        String target,
        double value
) {

    public enum TitleEffectType {
        DAMAGE_BONUS_VS_MOB,
        DAMAGE_REDUCTION_FROM_MOB,
        XP_BONUS_PERCENT,
        MONEY_BONUS_PERCENT,
        HEALTH_BONUS_FLAT,
        MANA_BONUS_FLAT
    }
}