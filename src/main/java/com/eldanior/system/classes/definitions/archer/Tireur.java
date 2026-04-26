package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Tireur extends ClassModel {
    public Tireur() {
        super("tireur", "Tireur", "Le Tireur excelle dans le tir de precision. Chaque fleche trouve sa cible avec une exactitude mortelle.",
                Rarity.COMMON, ClassType.ARCHER,
                List.of(PassiveSkill.EAGLE_EYE, PassiveSkill.LIGHT_REFLEXES, PassiveSkill.KEEN_SENSES),
                List.of(WeaponMastery.BOW, WeaponMastery.DAGGER),
                List.of("tireur_d_elite", "franc_tireur_royal", "arc_precis"), 400, false,
                6, 6, 4, 4, 16, 14);
    }
}
