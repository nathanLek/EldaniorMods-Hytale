package com.eldanior.system.classes.models;

import com.eldanior.system.classes.enums.ClassType;
import com.eldanior.system.classes.enums.Rarity;

public abstract class ClassModel {

    // Identité
    private final String id;
    private final String displayName;
    private final String description;
    private final Rarity rarity;
    private final ClassType type;

    // Progression & Evolution
    private final String nextClassId;
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
      String nextClassId, int promotionLevel, boolean adminAccess,
      int str, int vit, int intel, int end, int agl, int lck) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.rarity = rarity;
        this.type = type;
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