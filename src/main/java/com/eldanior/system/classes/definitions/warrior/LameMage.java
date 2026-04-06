package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;

import java.util.List;

public class LameMage extends ClassModel {

    public LameMage() {
        super(
                "lame_mage",
                "Lame-Mage",
                "Un initié capable de lier l'essence magique a son acier pour des frappes elementaires.",
                Rarity.RARE,
                ClassType.WARRIOR,
                List.of(),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(), // Évoluera au niveau 250 en (Ex: Chevalier Runique, Mage-Guerrier)
                250,
                false,
                20, 20, 30, 15, 15, 10 // Intelligence élevée pour un guerrier
        );
    }
}