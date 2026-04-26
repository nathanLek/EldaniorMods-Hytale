package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaitreNegociant extends ClassModel {
    public MaitreNegociant() {
        super("maitre_negociant", "Maitre Negociant", "Le negociant supreme dont les accords font trembler les marches.",
                Rarity.RARE, ClassType.MERCHANT, List.of(PassiveSkill.ARTISANAT, PassiveSkill.GOLDEN_TOUCH, PassiveSkill.CRITICAL_LUCK), List.of(WeaponMastery.ANY), List.of(), 400, false,
                7, 7, 10, 7, 10, 44);
    }
}
