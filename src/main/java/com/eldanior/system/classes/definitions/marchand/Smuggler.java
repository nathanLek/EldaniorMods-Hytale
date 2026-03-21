package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class Smuggler extends ClassModel {

    public Smuggler() {
        super(
                "smuggler",
                "Contrebandier",
                "Il sait se faufiler partout avec des marchandises illégales. Rapide et discret.",
                Rarity.RARE,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT),
                List.of("black_market_prince"),
                500,
                false,
                6, 4, 12, 8, 22, 25
        );
    }
}