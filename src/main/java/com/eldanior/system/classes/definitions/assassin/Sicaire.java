package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Sicaire extends ClassModel {
    public Sicaire() {
        super("sicaire", "Sicaire", "Le Sicaire est un tueur a gages professionnel. Chaque contrat est execute avec une precision chirurgicale.",
                Rarity.COMMON, ClassType.ASSASSIN,
                List.of(PassiveSkill.PRESSURE_POINT, PassiveSkill.KEEN_SENSES, PassiveSkill.DUELIST_SWIFTNESS),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of(), 120, false,
                10, 4, 2, 2, 18, 12);
    }
}
