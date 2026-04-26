package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Mercenaire extends ClassModel {

    public Mercenaire() {
        super(
                "mercenaire",
                "Mercenaire",
                "Le Mercenaire combat pour l'or et la gloire. Sa polyvalence et son instinct en font un adversaire imprevisible.",
                Rarity.COMMON,
                ClassType.WARRIOR,
                List.of(PassiveSkill.KEEN_SENSES, PassiveSkill.LUCKY_STRIKE, PassiveSkill.PRESSURE_POINT),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of("capitaine_mercenaire", "chasseur_de_primes", "loup_de_guerre"),
                400,
                false,
                20, 12, 4, 10, 20, 16
        );
    }
}