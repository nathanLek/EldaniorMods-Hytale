package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class RelicHunter extends ClassModel {

    public RelicHunter() {
        super(
                "relic_hunter",
                "Chasseur de Reliques",
                "Le Chasseur de Reliques parcourt le monde a la recherche d'artefacts oublies. Chaque decouverte est une fortune.",
                Rarity.RARE,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.TREASURE_HUNTER, PassiveSkill.EAGLE_EYE),
                List.of(WeaponMastery.ANY),
                List.of("gold_baron"),
                500,
                false,
                20, 12, 20, 16, 24, 70
        );
    }
}