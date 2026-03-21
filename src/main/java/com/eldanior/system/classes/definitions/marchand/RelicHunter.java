package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class RelicHunter extends ClassModel {

    public RelicHunter() {
        super(
                "relic_hunter",
                "Chasseur de Reliques",
                "Son flair pour les trésors anciens défie toute logique.",
                Rarity.RARE,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT),
                List.of("gold_baron"),
                500,
                false,
                10, 6, 10, 8, 12, 35
        );
    }
}