package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TireurElementaire extends ClassModel {
    public TireurElementaire() {
        super("tireur_elementaire", "Tireur Elementaire", "Le Tireur Elementaire lance des fleches de feu, de glace et de foudre. Chaque tir est une catastrophe naturelle.",
                Rarity.EPIC, ClassType.ARCHER,
                List.of(PassiveSkill.ARCANE_DEVASTATION, PassiveSkill.BRILLIANT_MIND, PassiveSkill.STORM_STEP),
                List.of(WeaponMastery.BOW, WeaponMastery.SPELLBOOK),
                List.of("arcane_archer", "sagittaire_elemental", "fleche_cosmique"), 400, false,
                30, 30, 50, 20, 70, 60);
    }
}
