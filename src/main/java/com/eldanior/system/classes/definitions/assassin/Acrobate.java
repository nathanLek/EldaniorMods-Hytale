package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Acrobate extends ClassModel {
    public Acrobate() {
        super("acrobate", "Acrobate", "L'Acrobate transforme le combat en une danse mortelle. Sa souplesse et son agilite defient les lois de la physique.",
                Rarity.COMMON, ClassType.ASSASSIN,
                List.of(PassiveSkill.WIND_STEP, PassiveSkill.LIGHT_REFLEXES, PassiveSkill.ELDANIOR_SUPPLENESS),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of(), 120, false,
                6, 6, 2, 4, 24, 8);
    }
}
