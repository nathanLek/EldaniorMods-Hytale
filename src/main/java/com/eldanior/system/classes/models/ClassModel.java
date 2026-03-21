package com.eldanior.system.classes.models;

import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.Collections;
import java.util.List;

public abstract class ClassModel {

    // Identité
    private final String id;
    private final String displayName;
    private final String description;
    private final Rarity rarity;
    private final ClassType type;
    private final List<PassiveSkill> passiveSkills; // Utilisation de l'Enum pour la sécurité

    // Progression & Evolution
    private final List<String> nextClassId;
    private final int promotionLevel;
    private final boolean adminAccess;

    // Stats Bonus (Modificateurs de base de la classe)
    private final int bonusStr;
    private final int bonusVit;
    private final int bonusInt;
    private final int bonusEnd;
    private final int bonusAgl;
    private final int bonusLck;

    public ClassModel(
            String id,
            String displayName,
            String description,
            Rarity rarity,
            ClassType type,
            List<PassiveSkill> passiveSkills,
            List<String> nextClassId,
            int promotionLevel,
            boolean adminAccess,
            int str,
            int vit,
            int intel,
            int end,
            int agl,
            int lck
    ) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.rarity = rarity;
        this.type = type;
        // On s'assure que la liste n'est jamais nulle pour éviter les NullPointerException
        this.passiveSkills = (passiveSkills != null) ? List.copyOf(passiveSkills) : Collections.emptyList();
        this.nextClassId = (nextClassId != null) ? List.copyOf(nextClassId) : Collections.emptyList();
        this.promotionLevel = promotionLevel;
        this.adminAccess = adminAccess;
        this.bonusStr = str;
        this.bonusVit = vit;
        this.bonusInt = intel;
        this.bonusEnd = end;
        this.bonusAgl = agl;
        this.bonusLck = lck;
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Rarity getRarity() { return rarity; }
    public ClassType getType() { return type; }

    /**
     * @return Une vue immuable des capacités passives de la classe.
     */
    public List<PassiveSkill> getSkillsPassiveIds() { return passiveSkills; }

    public List<String> getNextClassId() { return nextClassId; }
    public int getPromotionLevel() { return promotionLevel; }
    public boolean isAdminAccess() { return adminAccess; }

    // --- Getters Stats ---
    public int getBonusStr() { return bonusStr; }
    public int getBonusVit() { return bonusVit; }
    public int getBonusInt() { return bonusInt; }
    public int getBonusEnd() { return bonusEnd; }
    public int getBonusAgl() { return bonusAgl; }
    public int getBonusLck() { return bonusLck; }

    /**
     * Vérifie si la classe possède un passif spécifique.
     */
    public boolean hasPassive(PassiveSkill skill) {
        return passiveSkills.contains(skill);
    }
}