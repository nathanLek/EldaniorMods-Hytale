package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Espion extends ClassModel {
    public Espion() {
        super("espion", "Espion", "L'Espion est un maitre du deguisement et de l'infiltration. Il obtient toujours l'information qu'il cherche.",
                Rarity.RARE, ClassType.ASSASSIN,
                List.of(PassiveSkill.DANGER_SENSE, PassiveSkill.GOLDEN_TOUCH, PassiveSkill.PHANTOM_DODGE),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of(), 250, false,
                15, 20, 15, 10, 40, 55);
    }
}
