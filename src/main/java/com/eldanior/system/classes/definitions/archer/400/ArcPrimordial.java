package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArcPrimordial extends ClassModel {
    public ArcPrimordial() {
        super("arc_primordial", "Arc Primordial", "L'arc le plus ancien du monde. Chaque fleche est chargee de magie primordiale.",
                Rarity.UNIQUE, ClassType.ARCHER, List.of(PassiveSkill.ARCANE_ANNIHILATION, PassiveSkill.MANA_INFINITY, PassiveSkill.COSMIC_MIND), List.of(WeaponMastery.BOW, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                68, 85, 136, 68, 136, 170);
    }
}
