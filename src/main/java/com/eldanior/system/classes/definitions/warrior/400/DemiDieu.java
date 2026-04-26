package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DemiDieu extends ClassModel {
    public DemiDieu() {
        super("demi_dieu", "Demi-Dieu", "Le Demi-Dieu a transcende la condition mortelle pour toucher la divinite. Sa precision de createur et sa constitution cosmique en font un etre quasi-omnipotent.",
                Rarity.DIVINE, ClassType.WARRIOR, List.of(PassiveSkill.CREATOR_EDGE, PassiveSkill.CREATOR_CONSTITUTION, PassiveSkill.CREATOR_PRECISION), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                748, 612, 476, 612, 612, 816);
    }
}