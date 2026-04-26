package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DemiCommercant extends ClassModel {
    public DemiCommercant() {
        super("demi_commercant", "Demi-Commercant", "Un etre transcendant dont chaque transaction altere la realite.",
                Rarity.DIVINE, ClassType.MERCHANT, List.of(PassiveSkill.ARTISANAT, PassiveSkill.CREATOR_CONSTITUTION, PassiveSkill.CREATOR_STRIKE), List.of(WeaponMastery.ANY), List.of(), 400, false,
                425, 425, 595, 425, 425, 2550);
    }
}
