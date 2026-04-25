package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Chasseur extends ClassModel {
    public Chasseur() {
        super("chasseur", "Chasseur de Primes", "Le Chasseur de Primes traque ses cibles sans relache. Aucune proie ne lui echappe.",
                Rarity.RARE, ClassType.ASSASSIN,
                List.of(PassiveSkill.HAWK_EYE, PassiveSkill.RELENTLESS_HUNT, PassiveSkill.MARATHON_RUNNER),
                List.of(WeaponMastery.DAGGER, WeaponMastery.BOW, WeaponMastery.SWORD),
                List.of(), 250, false,
                25, 20, 5, 15, 45, 45);
    }
}
