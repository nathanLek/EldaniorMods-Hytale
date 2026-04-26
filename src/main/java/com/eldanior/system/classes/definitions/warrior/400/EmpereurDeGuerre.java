package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class EmpereurDeGuerre extends ClassModel {
    public EmpereurDeGuerre() {
        super("empereur_de_guerre", "Empereur de Guerre", "L'Empereur de Guerre domine le champ de bataille avec une autorite absolue. Sa lame legendaire et sa constitution cosmique inspirent terreur et respect.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.VOID_BLADE, PassiveSkill.COSMIC_CONSTITUTION, PassiveSkill.LIGHTNING_REFLEXES), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                168, 136, 50, 136, 85, 100);
    }
}