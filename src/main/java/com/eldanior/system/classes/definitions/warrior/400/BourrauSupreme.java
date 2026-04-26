package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class BourrauSupreme extends ClassModel {
    public BourrauSupreme() {
        super("bourrau_supreme", "Bourreau Supreme", "Le Bourreau Supreme est l'executeur ultime dont la lame du vide tranche les ames. Sa precision fatale et sa chasse mortelle ne laissent aucun survivant.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.FATAL_PRECISION, PassiveSkill.DEATH_HUNT), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                340, 136, 17, 136, 170, 152);
    }
}