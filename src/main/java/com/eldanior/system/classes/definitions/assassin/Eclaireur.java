package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Eclaireur extends ClassModel {
    public Eclaireur() {
        super("eclaireur", "Eclaireur", "L'Eclaireur est les yeux de l'armee. Sa vitesse et sa perception lui permettent de reperer tout danger.",
                Rarity.COMMON, ClassType.ASSASSIN,
                List.of(PassiveSkill.EAGLE_EYE, PassiveSkill.SURVIVAL_INSTINCT, PassiveSkill.WIND_STEP),
                List.of(WeaponMastery.DAGGER, WeaponMastery.BOW),
                List.of("eclaireur_d_elite", "oeil_percant", "veilleur_ombre"), 400, false,
                4, 6, 4, 4, 20, 10);
    }
}
