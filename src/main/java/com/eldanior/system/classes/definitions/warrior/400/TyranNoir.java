package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TyranNoir extends ClassModel {
    public TyranNoir() {
        super("tyran_noir", "Tyran Noir", "Le Tyran Noir impose sa domination par la terreur absolue. Sa cruaute sans limites et sa puissance ecrasante reduisent les plus braves au silence.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.CRUSHING_PRESSURE, PassiveSkill.STORM_STEP, PassiveSkill.DARK_VISION), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                204, 102, 34, 84, 132, 120);
    }
}