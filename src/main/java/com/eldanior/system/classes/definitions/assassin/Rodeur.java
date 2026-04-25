package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Rodeur extends ClassModel {
    public Rodeur() {
        super("rodeur", "Rodeur", "Le Rodeur hante les forets et les ruelles sombres. Il connait chaque recoin ou se cacher pour frapper.",
                Rarity.COMMON, ClassType.ASSASSIN,
                List.of(PassiveSkill.EAGLE_EYE, PassiveSkill.WIND_STEP, PassiveSkill.INSTINCTIVE_STRIKE),
                List.of(WeaponMastery.DAGGER, WeaponMastery.BOW),
                List.of(), 120, false,
                6, 6, 4, 4, 16, 12);
    }
}
