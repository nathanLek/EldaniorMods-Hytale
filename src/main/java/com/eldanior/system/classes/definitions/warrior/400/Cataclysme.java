package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Cataclysme extends ClassModel {
    public Cataclysme() {
        super("cataclysme", "Cataclysme", "Le Cataclysme dechaine des forces capables de remodeler les continents. Sa lame abyssale et sa rapidite surnaturelle sement le chaos absolu.",
                Rarity.LEGENDARY, ClassType.WARRIOR, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.DEMIGOD_SWIFTNESS, PassiveSkill.ANNIHILATING_PRESSURE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                472, 200, 66, 200, 200, 140);
    }
}