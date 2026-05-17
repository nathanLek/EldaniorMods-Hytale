package com.eldanior.system.titles.nobility.family;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;

public abstract class NobleFamilyModel {

    private final String id;
    private final String displayName;
    private final String motto;
    private final String history;
    private final Rarity rarity;
    private final NobilityRank minimumRank;
    private final PassiveSkill familyPassive;

    public NobleFamilyModel(
            String id,
            String displayName,
            String motto,
            String history,
            Rarity rarity,
            NobilityRank minimumRank,
            PassiveSkill familyPassive
    ) {
        this.id = id;
        this.displayName = displayName;
        this.motto = motto;
        this.history = history;
        this.rarity = rarity;
        this.minimumRank = minimumRank;
        this.familyPassive = familyPassive;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getMotto() { return motto; }
    public String getHistory() { return history; }
    public Rarity getRarity() { return rarity; }
    public NobilityRank getMinimumRank() { return minimumRank; }
    public PassiveSkill getFamilyPassive() { return familyPassive; }

    public String getFormattedName() {
        return rarity.getColorCode() + "Von " + displayName;
    }
}