package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Ascendant extends ClassModel {
    public Ascendant() {
        super("ascendant", "Ascendant", "L'Ascendant transcende les limites mortelles pour atteindre un etat superieur. Sa constitution divine et sa vie eternelle le placent au-dela de la mort.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.CREATOR_CONSTITUTION, PassiveSkill.ETERNAL_LIFE, PassiveSkill.CELESTIAL_IMMORTALITY), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                334, 500, 0, 236, 70, 36);
    }
}