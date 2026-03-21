package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;

import java.util.List;

public class Mercenaire extends ClassModel {

    public Mercenaire() {
        super(
                "mercenaire",
                "Mercenaire",
                "Un combattant a gages opportuniste, pariant sur sa vitesse et sa chance.",
                Rarity.COMMON,
                ClassType.WARRIOR,
                List.of(),
                List.of(),
                250,
                false,
                10, 6, 2, 5, 10, 8
        );
    }
}