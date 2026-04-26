package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class BanquierImperial extends ClassModel {
    public BanquierImperial() {
        super("banquier_imperial", "Banquier Imperial", "Le banquier de l'empire dont les coffres sont plus vastes que les oceans.",
                Rarity.EPIC, ClassType.MERCHANT, List.of(PassiveSkill.ARTISANAT, PassiveSkill.GOLDEN_TOUCH, PassiveSkill.TITAN_CONSTITUTION), List.of(WeaponMastery.ANY), List.of(), 400, false,
                18, 34, 34, 34, 18, 104);
    }
}
