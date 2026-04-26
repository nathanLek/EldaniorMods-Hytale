package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Inquisiteur extends ClassModel {
    public Inquisiteur() {
        super("inquisiteur", "Inquisiteur", "L'Inquisiteur traque les heretiques avec une ferveur fanatique. Son zele implacable et sa determination sans faille brisent les volontes les plus fortes.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.STEEL_RESOLVE, PassiveSkill.RELENTLESS_HUNT, PassiveSkill.BLOOD_HUNT), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                84, 80, 7, 84, 96, 98);
    }
}