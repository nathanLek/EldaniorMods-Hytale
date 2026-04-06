package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;

import java.util.List;

public class Ravageur extends ClassModel {

    public Ravageur() {
        super(
                "ravageur",
                "Ravageur",
                "Un guerrier redoutable maniant des armes lourdes pour briser les lignes ennemies.",
                Rarity.RARE,
                ClassType.WARRIOR,
                List.of(),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(), // Évoluera au niveau 250 en (Ex: Berserker, Seigneur de Guerre)
                250,
                false,
                45, 20, 2, 20, 15, 8 // Force massive, bonne résistance
        );
    }
}