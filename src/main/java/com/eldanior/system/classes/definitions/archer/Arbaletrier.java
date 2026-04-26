package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Arbaletrier extends ClassModel {
    public Arbaletrier() {
        super("arbaletrier", "Arbaletrier", "L'Arbaletrier maitrise l'arbalete avec une precision mecanique. Chaque carreau est un arret de mort.",
                Rarity.COMMON, ClassType.ARCHER,
                List.of(PassiveSkill.PRESSURE_POINT, PassiveSkill.INSTINCTIVE_STRIKE, PassiveSkill.STONE_SKIN),
                List.of(WeaponMastery.BOW),
                List.of("arbaletrier_d_elite", "arbalete_lourd", "tireur_de_siege"), 400, false,
                8, 8, 2, 6, 12, 12);
    }
}
