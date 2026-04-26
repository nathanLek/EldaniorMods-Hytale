package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Extinction extends ClassModel {
    public Extinction() {
        super("extinction", "Extinction", "L'Extinction efface toute trace de vie la ou elle passe. Sa pression ecrasante sur l'ame et sa lame dimensionnelle aneantissent jusqu'a l'esperance.",
                Rarity.LEGENDARY, ClassType.WARRIOR, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.SOUL_CRUSHING_PRESSURE, PassiveSkill.DIMENSIONAL_STEP), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                480, 198, 68, 198, 198, 138);
    }
}