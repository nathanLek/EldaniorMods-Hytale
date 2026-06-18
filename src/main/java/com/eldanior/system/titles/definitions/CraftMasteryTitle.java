package com.eldanior.system.titles.definitions;

import com.eldanior.system.Leveling.CraftTier;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.enums.TitleCategory;
import com.eldanior.system.titles.models.*;

import java.util.List;

/**
 * Titre de maitrise de craft generique.
 * 9 metiers x 4 paliers = 36 titres.
 */
public class CraftMasteryTitle extends TitleModel {

    private final String skillId;
    private final int requiredProcs;

    public CraftMasteryTitle(String id, String displayName, String description,
                             Rarity rarity, String skillId, int requiredProcs) {
        super(id, displayName, description, rarity, TitleCategory.CRAFT, TitleBonus.NONE, List.of());
        this.skillId = skillId;
        this.requiredProcs = requiredProcs;
    }

    @Override
    public boolean checkUnlockCondition(PlayerLevelData data) {
        return data.getSkillProcs(skillId) >= requiredProcs;
    }
}
