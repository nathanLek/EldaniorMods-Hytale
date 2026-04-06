// 1. Le Héros (Légendaire - La Perfection)
package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;

import java.util.List;

public class Heros extends ClassModel {
    public Heros() {
        super("heros", "Héros", "L'élu des prophéties. Ses statistiques sont parfaitement équilibrées et surpuissantes.",
                Rarity.LEGENDARY, ClassType.WARRIOR, List.of(), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 250, false,
                75, 75, 75, 75, 40, 40);
    }
}