package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;

import java.util.List;

public class Assassin extends ClassModel {

    public Assassin() {
        super(
                "assassin",
                "Assassin",
                "Un tueur silencieux qui frappe dans l'ombre.",
                Rarity.COMMON,
                ClassType.ASSASSIN,
                List.of(),
                List.of(),
                120,
                false,
                1000, 300, 200, 0, 2000, 3000
        );
    }
}