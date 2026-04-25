package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Chasseur extends ClassModel {
    public Chasseur() {
        super("chasseur_archer", "Chasseur", "Le Chasseur traque ses proies dans les forets les plus denses. Son arc est le prolongement de son bras.",
                Rarity.COMMON, ClassType.ARCHER,
                List.of(PassiveSkill.EAGLE_EYE, PassiveSkill.SURVIVAL_INSTINCT, PassiveSkill.WIND_STEP),
                List.of(WeaponMastery.BOW, WeaponMastery.DAGGER),
                List.of(), 120, false,
                6, 8, 2, 6, 14, 12);
    }
}
