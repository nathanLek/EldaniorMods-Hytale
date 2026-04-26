package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Brute extends ClassModel {

    public Brute() {
        super(
                "brute",
                "Brute",
                "La Brute ne connait qu'un langage : la force. Chaque coup porte la puissance d'un tremblement de terre.",
                Rarity.COMMON,
                ClassType.WARRIOR,
                List.of(PassiveSkill.DEEP_SLASH, PassiveSkill.STONE_SKIN, PassiveSkill.TIRELESS_BREATH),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of("ecraseur", "boucher_de_guerre", "colosse_de_fer"),
                400,
                false,
                30, 20, 2, 8, 8, 4
        );
    }
}