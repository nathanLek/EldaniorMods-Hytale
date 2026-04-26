package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ChasseurDePrimes extends ClassModel {
    public ChasseurDePrimes() {
        super("chasseur_de_primes", "Chasseur de Primes", "Le Chasseur de Primes traque ses cibles avec une determination implacable. Aucune proie ne lui echappe une fois qu'il a accepte le contrat.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.PRESSURE_POINT, PassiveSkill.RELENTLESS_HUNT, PassiveSkill.TRACKER), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                32, 20, 7, 16, 34, 26);
    }
}