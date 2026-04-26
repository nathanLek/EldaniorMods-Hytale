package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class VisionAbsolue extends ClassModel {
    public VisionAbsolue() {
        super("vision_absolue", "Vision Absolue", "Celui qui voit tout, au-dela du visible. Sa vision transcende la realite.",
                Rarity.UNIQUE, ClassType.ARCHER, List.of(PassiveSkill.ABSOLUTE_PRECISION, PassiveSkill.ABYSS_BLADE, PassiveSkill.GOD_SLAYER_SWIFTNESS), List.of(WeaponMastery.BOW), List.of(), 400, false,
                102, 68, 34, 52, 204, 254);
    }
}
