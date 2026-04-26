package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class PredateurSupreme extends ClassModel {
    public PredateurSupreme() {
        super("predateur_supreme", "Predateur Supreme", "Au sommet de la chaine alimentaire. Aucune proie ne peut lui echapper.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.BLOOD_HUNT, PassiveSkill.RAZOR_SENSES, PassiveSkill.FATAL_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.BOW, WeaponMastery.SWORD), List.of(), 400, false,
                42, 32, 10, 24, 76, 76);
    }
}
