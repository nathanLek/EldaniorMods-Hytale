package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class EluDesBatailles extends ClassModel {
    public EluDesBatailles() {
        super("elu_des_batailles", "Elu des Batailles", "L'Elu des Batailles est choisi par le destin pour dominer chaque affrontement. Sa constitution titanesque et ses reflexes surhumains font de lui une legende vivante.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.COSMIC_CONSTITUTION, PassiveSkill.DIVINE_REFLEXES), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                200, 136, 34, 136, 100, 68);
    }
}