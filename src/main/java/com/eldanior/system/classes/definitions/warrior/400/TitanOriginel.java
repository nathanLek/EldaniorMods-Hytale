package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TitanOriginel extends ClassModel {
    public TitanOriginel() {
        super("titan_originel", "Titan Originel", "Le Titan Originel est le premier ne de sa race, une force primordiale pure. Sa constitution divine et sa vie eternelle defient les lois de l'existence.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.GOD_CONSTITUTION, PassiveSkill.ETERNAL_LIFE, PassiveSkill.UNMOVABLE_MOUNTAIN), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                340, 510, 0, 238, 68, 34);
    }
}