package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class RempartVivant extends ClassModel {
    public RempartVivant() {
        super("rempart_vivant", "Rempart Vivant", "Le Rempart Vivant est une forteresse de chair et d'os. Sa constitution inhumaine lui permet d'absorber des degats qui tueraient tout autre guerrier.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.STONE_SKIN, PassiveSkill.STEEL_CONSTITUTION, PassiveSkill.HARDENING), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                19, 42, 3, 32, 9, 7);
    }
}
