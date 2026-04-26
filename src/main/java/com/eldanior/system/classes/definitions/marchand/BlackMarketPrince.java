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
                "Le Prince du Marche Noir controle le commerce souterrain. Son reseau d'informateurs s'etend dans tout le continent.",
                Rarity.EPIC,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.GOLDEN_TOUCH, PassiveSkill.PHANTOM_DODGE),
                List.of(WeaponMastery.ANY),
                List.of("roi_du_marche_noir"),
                800,
                false,
                10, 8, 20, 16, 44, 40
        );
    }
}