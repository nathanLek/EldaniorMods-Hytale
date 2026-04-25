package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Berserker extends ClassModel {

    public Berserker() {
        super(
                "berserker",
                "Berserker",
                "Le Berserker entre dans une rage incontrôlable au combat. Il sacrifie toute defense pour une puissance d'attaque devastatrice.",
                Rarity.COMMON,
                ClassType.WARRIOR,
                List.of(PassiveSkill.DEEP_SLASH, PassiveSkill.INSTINCTIVE_STRIKE, PassiveSkill.TIRELESS_BREATH),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(),
                120,
                false,
                28, 10, 2, 8, 12, 6
        );
    }
}