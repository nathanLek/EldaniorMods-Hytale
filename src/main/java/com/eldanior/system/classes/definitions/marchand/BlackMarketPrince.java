package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class BlackMarketPrince extends ClassModel {

    public BlackMarketPrince() {
        super(
                "black_market_prince",
                "Prince du Marché Noir",
                "Il contrôle les bas-fonds. Rapide comme l'ombre, et mortel s'il est acculé.",
                Rarity.EPIC,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT),
                List.of(WeaponMastery.ANY),
                List.of("underworld_king"),
                800,
                false,
                5, 4, 10, 8, 22, 20
        );
    }
}