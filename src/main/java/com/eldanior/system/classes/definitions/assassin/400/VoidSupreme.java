package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class VoidSupreme extends ClassModel {
    public VoidSupreme() {
        super("void_supreme", "Void Supreme", "Le neant supreme. Au-dela des ombres, au-dela de tout.",
                Rarity.LEGENDARY, ClassType.ASSASSIN, List.of(PassiveSkill.CREATOR_EDGE, PassiveSkill.SOUL_CRUSHING_PRESSURE, PassiveSkill.GOD_SLAYER_SWIFTNESS), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                196, 106, 32, 106, 416, 302);
    }
}
