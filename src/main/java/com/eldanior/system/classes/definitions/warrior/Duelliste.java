package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Duelliste extends ClassModel {

    public Duelliste() {
        super(
                "duelliste",
                "Duelliste",
                "Le Duelliste est un maitre du combat singulier. Sa technique impeccable et sa precision en font un adversaire redoutable en face a face.",
                Rarity.COMMON,
                ClassType.WARRIOR,
                List.of(PassiveSkill.DUELIST_SWIFTNESS, PassiveSkill.KEEN_SENSES, PassiveSkill.PRESSURE_POINT),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(),
                120,
                false,
                16, 12, 4, 8, 18, 10
        );
    }
}