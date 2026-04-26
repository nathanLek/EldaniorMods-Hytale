package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class InfiltreurSupreme extends ClassModel {
    public InfiltreurSupreme() {
        super("infiltreur_supreme", "Infiltreur Supreme", "Aucune forteresse ne peut le contenir. Il entre et sort comme un fantome.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.DIMENSIONAL_STEP, PassiveSkill.DARK_VISION, PassiveSkill.STEEL_NERVES), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                28, 30, 24, 20, 72, 88);
    }
}
