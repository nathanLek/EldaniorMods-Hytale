package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class Smuggler extends ClassModel {

    public Smuggler() {
        super(
                "smuggler",
                "Contrebandier",
                "Le Contrebandier connait les routes secretes et les marches noirs. Rien ne lui echappe quand il s'agit de profit.",
                Rarity.RARE,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.WIND_STEP, PassiveSkill.LUCKY_STRIKE),
                List.of(WeaponMastery.ANY),
                List.of("contrebandier_royal"),
                500,
                false,
                12, 8, 24, 16, 44, 50
        );
    }
}