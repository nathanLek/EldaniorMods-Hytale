// 1. Le Titan (Divin - Indestructible)
package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;

import java.util.List;

public class Titan extends ClassModel {
    public Titan() {
        super("titan", "Titan", "Un être primordial dont la peau est plus dure que le diamant. Il est la terre elle-même.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 250, false,
                100, 150, 0, 70, 20, 10);
    }
}