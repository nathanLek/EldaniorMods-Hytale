package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GrandIllusionniste extends ClassModel {
    public GrandIllusionniste() {
        super("grand_illusionniste", "Grand Illusionniste", "Le maitre des illusions dont les mirages sont indiscernables du reel.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.PHANTOM_DODGE, PassiveSkill.STORM_STEP, PassiveSkill.BRILLIANT_MIND), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                10, 34, 78, 26, 70, 60);
    }
}
