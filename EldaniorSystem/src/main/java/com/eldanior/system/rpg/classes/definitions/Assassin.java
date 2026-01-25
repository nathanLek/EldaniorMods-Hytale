package com.eldanior.system.rpg.classes.definitions;

import com.eldanior.system.rpg.classes.ClassModel;
import com.eldanior.system.rpg.enums.ClassType;
import com.eldanior.system.rpg.enums.Rarity;

public class Assassin extends ClassModel {

    public Assassin() {
        super(
                "assassin",
                "Assassin",
                "Un tueur silencieux qui frappe dans l'ombre.",
                Rarity.COMMON,
                ClassType.ASSASSIN,
                null,
                true,
                "shadow_blade",
                0,
                false,
                3, 2, 0, 2, 6, 3
        );
    }
}