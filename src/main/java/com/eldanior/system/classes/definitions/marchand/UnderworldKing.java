package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import java.util.List;

public class UnderworldKing extends ClassModel {

    public UnderworldKing() {
        super(
                "underworld_king",
                "Roi des Bas-Fonds",
                "Le maître absolu du syndicat criminel. Insaisissable, il dicte sa loi dans les ténèbres.",
                Rarity.LEGENDARY,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT),
                null,
                800,
                false,
                100, 80, 150, 120, 350, 300
        );
    }
}