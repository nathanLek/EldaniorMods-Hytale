package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArchangeNoir extends ClassModel {
    public ArchangeNoir() {
        super("archange_noir", "Archange Noir", "Un archange corrompu dont la puissance rivale celle des dieux.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.VOID_STEP, PassiveSkill.LIGHTNING_REFLEXES, PassiveSkill.ABSOLUTE_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                68, 52, 18, 34, 226, 104);
    }
}
