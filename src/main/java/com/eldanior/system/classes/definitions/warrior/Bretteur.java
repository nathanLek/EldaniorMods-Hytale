package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;

import java.util.List;

public class Bretteur extends ClassModel {

    public Bretteur() {
        super(
                "bretteur",
                "Bretteur",
                "Un combattant agile qui mise sur la vitesse d'execution et les parades fluides.",
                Rarity.RARE,
                ClassType.WARRIOR,
                List.of(),
                List.of(), // Évoluera au niveau 250 en (Ex: Maître d'Armes, Danseur de Lames)
                250,
                false,
                20, 15, 5, 15, 35, 20 // Vitesse (Agl) et Précision (Lck) dominantes
        );
    }
}