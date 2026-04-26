package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArtisteduCombat extends ClassModel {
    public ArtisteduCombat() {
        super("artiste_du_combat", "Artiste du Combat", "Le combat est son art, et chaque coup est un chef-d'oeuvre.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.ACROBATIC_POISE, PassiveSkill.GRAVITY_DEFIANCE, PassiveSkill.CRITICAL_LUCK), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                8, 10, 4, 8, 40, 12);
    }
}
