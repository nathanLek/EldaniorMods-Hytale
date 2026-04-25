package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class AvantGarde extends ClassModel {

    public AvantGarde() {
        super(
                "avant_garde",
                "Avant-Garde",
                "L'Avant-Garde est une muraille vivante. Il avance en premiere ligne et ne recule jamais.",
                Rarity.RARE,
                ClassType.WARRIOR,
                List.of(PassiveSkill.IRON_BODY, PassiveSkill.STEEL_CONSTITUTION, PassiveSkill.UNYIELDING),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(),
                250,
                false,
                30, 80, 10, 70, 20, 10
        );
    }
}