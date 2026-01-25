package com.eldanior.system.rpg.classes;

import com.eldanior.system.rpg.enums.ClassType;
import com.eldanior.system.rpg.enums.Rarity;
import com.eldanior.system.rpg.classes.skills.Skills.SkillModel;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List; // ✅ Import List

public abstract class ClassModel {

    // Identité
    private final String id;
    private final String displayName;
    private final String description;
    private final Rarity rarity;
    private final ClassType type;

    // Progression & Evolution
    private final List<SkillModel> passiveSkills; // ✅ Changement : C'est une LISTE maintenant
    private final String nextClassId;
    private final boolean passiveActive;
    private final int promotionLevel;
    private final boolean adminAccess;

    // Stats Bonus
    private final int bonusStr;
    private final int bonusVit;
    private final int bonusInt;
    private final int bonusEnd;
    private final int bonusAgl;
    private final int bonusLck;

    public ClassModel(String id, String displayName, String description, Rarity rarity, ClassType type,
                      @Nullable List<SkillModel> passiveSkills, // ✅ Paramètre modifié
                      boolean passiveActive,
                      String nextClassId, int promotionLevel, boolean adminAccess,
                      int str, int vit, int intel, int end, int agl, int lck) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.rarity = rarity;
        this.type = type;

        // Sécurité : Si on envoie null, on met une liste vide pour éviter les crashs
        this.passiveSkills = (passiveSkills != null) ? passiveSkills : Collections.emptyList();

        this.passiveActive = passiveActive;
        this.nextClassId = nextClassId;
        this.promotionLevel = promotionLevel;
        this.adminAccess = adminAccess;
        this.bonusStr = str;
        this.bonusVit = vit;
        this.bonusInt = intel;
        this.bonusEnd = end;
        this.bonusAgl = agl;
        this.bonusLck = lck;
    }

    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Rarity getRarity() { return rarity; }
    public ClassType getType() { return type; }

    public List<SkillModel> getPassiveSkills() { return passiveSkills; } // ✅ Getter modifié

    public boolean isPassiveActive() { return passiveActive; }
    public String getNextClassId() { return nextClassId; }
    public int getPromotionLevel() { return promotionLevel; }
    public boolean isAdminAccess() { return adminAccess; }

    public int getBonusStr() { return bonusStr; }
    public int getBonusVit() { return bonusVit; }
    public int getBonusInt() { return bonusInt; }
    public int getBonusEnd() { return bonusEnd; }
    public int getBonusAgl() { return bonusAgl; }
    public int getBonusLck() { return bonusLck; }
}