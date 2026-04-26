package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GolemVivant extends ClassModel {
    public GolemVivant() {
        super("golem_vivant", "Golem Vivant", "Le Golem Vivant est une creation impossible de chair et de roche. Son corps indestructible regenere sans cesse, absorbant tout dommage sans broncher.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.STEEL_BODY, PassiveSkill.TROLL_BLOOD, PassiveSkill.UNMOVABLE_MOUNTAIN), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                96, 240, 16, 234, 32, 52);
    }
}