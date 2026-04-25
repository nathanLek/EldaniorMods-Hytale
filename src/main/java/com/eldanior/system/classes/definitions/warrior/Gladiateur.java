package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Gladiateur extends ClassModel {

    public Gladiateur() {
        super(
                "gladiateur",
                "Gladiateur",
                "Le Gladiateur a forge sa legende dans l'arene. Chaque combat est un spectacle ou seule la victoire compte.",
                Rarity.RARE,
                ClassType.WARRIOR,
                List.of(PassiveSkill.SHARP_BLADE, PassiveSkill.RAZOR_SENSES, PassiveSkill.BATTLE_FRENZY),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(),
                250,
                false,
                50, 30, 4, 30, 50, 30
        );
    }
}