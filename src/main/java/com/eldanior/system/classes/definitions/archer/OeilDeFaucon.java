package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class OeilDeFaucon extends ClassModel {
    public OeilDeFaucon() {
        super("oeil_de_faucon", "Oeil de Faucon", "L'Oeil de Faucon voit tout. A des kilometres de distance, il peut toucher une cible grande comme une piece.",
                Rarity.UNIQUE, ClassType.ARCHER,
                List.of(PassiveSkill.FATAL_PRECISION, PassiveSkill.VOID_BLADE, PassiveSkill.BERSERKER_SWIFTNESS),
                List.of(WeaponMastery.BOW),
                List.of(), 250, false,
                60, 40, 20, 30, 120, 150);
    }
}
