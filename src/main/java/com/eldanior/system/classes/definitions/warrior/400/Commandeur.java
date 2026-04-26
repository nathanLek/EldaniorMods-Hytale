package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Commandeur extends ClassModel {
    public Commandeur() {
        super("commandeur", "Commandeur", "Le Commandeur dirige les armees avec une autorite absolue. Son endurance marathonienne et sa resilience mentale en font un leader indestructible.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.MARATHON_RUNNER, PassiveSkill.OVERFLOWING_LIFE, PassiveSkill.TENACITY), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                82, 82, 7, 82, 98, 102);
    }
}