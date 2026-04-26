package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class CoureurdAurore extends ClassModel {
    public CoureurdAurore() {
        super("coureur_d_aurore", "Coureur d'Aurore", "Il court avec l'aurore, premier rayon de lumiere et derniere fleche.",
                Rarity.RARE, ClassType.ARCHER, List.of(PassiveSkill.LIGHT_REFLEXES, PassiveSkill.MARATHON_RUNNER, PassiveSkill.LUCKY_STRIKE), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                6, 8, 8, 8, 30, 22);
    }
}
