package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArcMystique extends ClassModel {
    public ArcMystique() {
        super("arc_mystique", "Arc Mystique", "L'Arc Mystique imprègne ses fleches de magie pure. Chaque tir est une explosion d'energie arcanique.",
                Rarity.RARE, ClassType.ARCHER,
                List.of(PassiveSkill.SPELLBLADE, PassiveSkill.EXPANDED_MIND, PassiveSkill.MANA_STREAM),
                List.of(WeaponMastery.BOW, WeaponMastery.SPELLBOOK),
                List.of(), 250, false,
                15, 15, 35, 10, 40, 40);
    }
}
