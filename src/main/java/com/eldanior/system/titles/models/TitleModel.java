package com.eldanior.system.titles.models;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;

import java.util.Collections;
import java.util.List;

public abstract class TitleModel {

    private final String id;
    private final String displayName;
    private final String description;
    private final Rarity rarity;
    private final TitleCategory category;
    private final TitleBonus bonus;
    private final List<TitleEffect> effects;

    public TitleModel(
            String id,
            String displayName,
            String description,
            Rarity rarity,
            TitleCategory category,
            TitleBonus bonus,
            List<TitleEffect> effects
    ) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.rarity = rarity;
        this.category = category;
        this.bonus = (bonus != null) ? bonus : TitleBonus.NONE;
        this.effects = (effects != null) ? List.copyOf(effects) : Collections.emptyList();
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Rarity getRarity() { return rarity; }
    public TitleCategory getCategory() { return category; }
    public TitleBonus getBonus() { return bonus; }
    public List<TitleEffect> getEffects() { return effects; }

    public boolean hasEffect(TitleEffect.TitleEffectType type) {
        return effects.stream().anyMatch(e -> e.type() == type);
    }

    public double getEffectValue(TitleEffect.TitleEffectType type, String target) {
        return effects.stream()
                .filter(e -> e.type() == type && (target == null || target.equalsIgnoreCase(e.target())))
                .mapToDouble(TitleEffect::value)
                .sum();
    }

    public String getFormattedName() {
        return rarity.getColorCode() + displayName;
    }

    /**
     * Condition de deblocage automatique.
     * Retourne true si le joueur remplit les conditions pour obtenir ce titre.
     * Par defaut retourne false (titre non deblocable automatiquement, uniquement par commande).
     */
    public boolean checkUnlockCondition(PlayerLevelData data) {
        return false;
    }
}