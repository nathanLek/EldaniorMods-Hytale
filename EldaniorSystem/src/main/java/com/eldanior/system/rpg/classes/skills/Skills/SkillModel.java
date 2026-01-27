package com.eldanior.system.rpg.classes.skills.Skills;

import com.eldanior.system.rpg.enums.ClassType;
import com.eldanior.system.rpg.enums.Rarity;

/**
 * Modèle de base pour TOUTES les compétences (Actives ou Passives).
 */
public abstract class SkillModel {

    private final String id;
    private final String name;
    private final String description;
    private final Rarity rarity;
    private final ClassType classType;

    // Le cooldown (temps de recharge)
    private final float cooldown;

    // Le coût (Mana par activation OU Mana par seconde selon le type)
    private final float resourceCost;

    public SkillModel(String id, String name, String description, Rarity rarity, ClassType classType, float cooldown, float resourceCost) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.classType = classType;
        this.cooldown = cooldown;
        this.resourceCost = resourceCost;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Rarity getRarity() { return rarity; }
    public ClassType getClassType() { return classType; }
    public float getCooldown() { return cooldown; }
    public float getResourceCost() { return resourceCost; }

    // Petit utilitaire pour le multiplicateur de rareté (commun à tous)
    public float getRarityMultiplier() {
        return switch (this.rarity) {
            case COMMON -> 1.0f;
            case RARE -> 1.25f;
            case EPIC -> 1.5f;
            case UNIQUE -> 2.0f;
            case LEGENDARY -> 2.5f;
            case DIVINE -> 3.5f;
            default -> 1.0f;
        };
    }
}