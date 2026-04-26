package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Veteran extends ClassModel {
    public Veteran() {
        super("veteran", "Veteran", "Le Veteran a survecu a d'innombrables batailles. Son experience lui confere une resistance et une endurance que seul le temps de guerre peut forger.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.FORTIFIED_SKIN, PassiveSkill.STEEL_CONSTITUTION, PassiveSkill.IRON_RESOLVE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                26, 50, 7, 34, 10, 7);
    }
}