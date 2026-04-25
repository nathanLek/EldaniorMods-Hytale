package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class Banquier extends ClassModel {

    public Banquier() {
        super(
                "banquier",
                "Banquier",
                "Le Banquier gere des fortunes colossales. Son sens des affaires et sa prudence le rendent incontournable.",
                Rarity.RARE,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.GOLDEN_TOUCH, PassiveSkill.STEEL_CONSTITUTION),
                List.of(WeaponMastery.ANY),
                List.of(),
                250,
                false,
                10, 20, 20, 20, 10, 60
        );
    }
}