package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class UnderworldKing extends ClassModel {

    public UnderworldKing() {
        super(
                "underworld_king",
                "Roi des Bas-Fonds",
                "Le Roi des Bas-Fonds regne sur un empire criminel. Chaque transaction illegale dans le monde passe par ses mains.",
                Rarity.LEGENDARY,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.REALITY_DODGE, PassiveSkill.DEMIGOD_SWIFTNESS),
                List.of(WeaponMastery.ANY),
                null,
                800,
                false,
                200, 160, 300, 240, 700, 600
        );
    }
}