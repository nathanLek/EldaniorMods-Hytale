package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Centurion extends ClassModel {
    public Centurion() {
        super("centurion", "Centurion", "Le Centurion commande avec autorite et mene ses troupes au front. Sa discipline militaire et sa robustesse en font un pilier indestructible.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.STONE_SKIN, PassiveSkill.ROBUST_CONSTITUTION, PassiveSkill.STEEL_RESOLVE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                28, 48, 7, 33, 10, 7);
    }
}