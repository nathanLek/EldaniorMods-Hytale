package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Merchant extends ClassModel {

    public Merchant() {
        super(
                "merchant",
                "Marchand",
                "Un negociateur hors pair.",
                Rarity.COMMON,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT),
                List.of("master_artisan", "relic_hunter", "smuggler"),
                120,
                false,
                2, 1, 2, 2, 2, 10
        );
    }
}