package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class RoiDuCommerce extends ClassModel {

    public RoiDuCommerce() {
        super(
                "roi_commerce",
                "Roi du Commerce",
                "Le Roi du Commerce controle l'economie du monde entier. Chaque piece d'or frappee porte son empreinte invisible.",
                Rarity.DIVINE,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.CREATOR_CONSTITUTION, PassiveSkill.CREATOR_STRIKE),
                List.of(WeaponMastery.ANY),
                List.of("demi_commercant"),
                400,
                false,
                250, 250, 350, 250, 250, 1500
        );
    }
}