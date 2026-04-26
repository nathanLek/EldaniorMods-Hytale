package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class OmbreInsaisissable extends ClassModel {
    public OmbreInsaisissable() {
        super("ombre_insaisissable", "Ombre Insaisissable", "Un fantome que nul ne peut attraper. Elle disparait avant meme d'etre vue.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.SHADOW_DODGE, PassiveSkill.RELENTLESS_HUNT, PassiveSkill.STORM_STEP), List.of(WeaponMastery.DAGGER), List.of(), 400, false,
                34, 26, 8, 18, 104, 78);
    }
}
