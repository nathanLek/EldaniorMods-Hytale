package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DemiMarchand extends ClassModel {
    public DemiMarchand() {
        super("demi_marchand", "Demi-Marchand", "Mi-mortel mi-divin, le commerce est sa religion et l'or son sang.",
                Rarity.DIVINE, ClassType.MERCHANT, List.of(PassiveSkill.ARTISANAT, PassiveSkill.CREATOR_PRECISION, PassiveSkill.CREATOR_CONSTITUTION), List.of(WeaponMastery.ANY), List.of(), 400, false,
                340, 340, 510, 340, 510, 2040);
    }
}
