package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaitreEspion extends ClassModel {
    public MaitreEspion() {
        super("maitre_espion", "Maitre Espion", "Le roi des espions. Il connait tous les secrets du monde.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.PHANTOM_DODGE, PassiveSkill.GOLDEN_TOUCH, PassiveSkill.RAZOR_SENSES), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                26, 34, 26, 18, 70, 96);
    }
}
