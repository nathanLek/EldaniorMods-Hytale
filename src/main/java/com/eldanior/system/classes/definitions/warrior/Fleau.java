// 2. Le Fléau (Légendaire - La Destruction)
package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;

import java.util.List;

public class Fleau extends ClassModel {
    public Fleau() {
        super("fleau", "Fléau", "L'incarnation de la guerre. Là où il passe, il ne reste que des cendres.",
                Rarity.LEGENDARY, ClassType.WARRIOR, List.of(), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 250, false,
                140, 60, 20, 60, 60, 40);
    }
}