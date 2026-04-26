package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArcaneSupreme extends ClassModel {
    public ArcaneSupreme() {
        super("arcane_supreme", "Arcane Supreme", "La maitrise arcanique absolue. Magie et tir ne font qu'un.",
                Rarity.UNIQUE, ClassType.ARCHER, List.of(PassiveSkill.ARCANE_ANNIHILATION, PassiveSkill.BRILLIANT_MIND, PassiveSkill.FATAL_PRECISION), List.of(WeaponMastery.BOW, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                64, 88, 140, 64, 132, 164);
    }
}
