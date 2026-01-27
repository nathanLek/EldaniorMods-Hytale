package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.classes.enums.ClassType;
import com.eldanior.system.classes.enums.Rarity;

public class Assassin extends ClassModel {

    public Assassin() {
        super(
                "assassin",
                "Assassin",
                "Un tueur silencieux qui frappe dans l'ombre.",
                Rarity.COMMON,
                ClassType.ASSASSIN,
                null,
                20,
                false,
                3, 3, 2, 0, 2, 6
        );
    }
}