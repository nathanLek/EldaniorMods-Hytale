package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GeneralArcher extends ClassModel {
    public GeneralArcher() {
        super("general_archer", "General des Archers", "Le General des Archers commande une pluie de fleches capable d'obscurcir le ciel.",
                Rarity.EPIC, ClassType.ARCHER,
                List.of(PassiveSkill.TITAN_CONSTITUTION, PassiveSkill.LIGHTNING_REFLEXES, PassiveSkill.DESTINY_STRIKE),
                List.of(WeaponMastery.BOW, WeaponMastery.SWORD),
                List.of(), 250, false,
                50, 60, 20, 50, 60, 60);
    }
}
