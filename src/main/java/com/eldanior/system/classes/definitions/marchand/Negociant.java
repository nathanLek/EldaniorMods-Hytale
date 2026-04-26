package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class Negociant extends ClassModel {

    public Negociant() {
        super(
                "negociant",
                "Negociant",
                "Le Negociant est un commercial ne. Il transforme chaque echange en profit et chaque rencontre en opportunite.",
                Rarity.COMMON,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.FORTUNE_COINS, PassiveSkill.LUCKY_STRIKE),
                List.of(WeaponMastery.ANY),
                List.of("maitre_negociant"),
                400,
                false,
                4, 4, 6, 4, 6, 26
        );
    }
}
