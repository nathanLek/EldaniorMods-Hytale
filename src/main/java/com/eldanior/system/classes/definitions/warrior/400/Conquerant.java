package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Conquerant extends ClassModel {
    public Conquerant() {
        super("conquerant", "Conquerant", "Le Conquerant soumet les nations entieres par la force de son bras. Sa lame cramoisie et sa precision fatale ne laissent aucune chance a ses adversaires.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.FATAL_PRECISION, PassiveSkill.TITAN_CONSTITUTION), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                196, 132, 32, 132, 102, 66);
    }
}