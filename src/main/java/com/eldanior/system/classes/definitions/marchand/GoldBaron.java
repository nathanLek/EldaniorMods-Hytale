package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class GoldBaron extends ClassModel {

    public GoldBaron() {
        super(
                "gold_baron",
                "Baron de l'Or",
                "Le Baron de l'Or possede une fortune inimaginable. Chaque piece qu'il touche se multiplie comme par magie.",
                Rarity.EPIC,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.GOLDEN_TOUCH, PassiveSkill.CRITICAL_LUCK),
                List.of(WeaponMastery.ANY),
                List.of("prosperity_avatar"),
                800,
                false,
                12, 12, 16, 12, 20, 90
        );
    }
}