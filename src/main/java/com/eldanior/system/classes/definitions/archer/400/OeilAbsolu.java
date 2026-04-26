package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class OeilAbsolu extends ClassModel {
    public OeilAbsolu() {
        super("oeil_absolu", "Oeil Absolu", "L'oeil qui voit tout. Aucune cible n'est hors de portee.",
                Rarity.UNIQUE, ClassType.ARCHER, List.of(PassiveSkill.ABSOLUTE_PRECISION, PassiveSkill.DEADLY_PRECISION, PassiveSkill.EAGLE_EYE), List.of(WeaponMastery.BOW), List.of(), 400, false,
                72, 48, 24, 32, 134, 170);
    }
}
