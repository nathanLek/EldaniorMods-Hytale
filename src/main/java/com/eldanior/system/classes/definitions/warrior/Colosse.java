// 2. Le Colosse (Épique - Tank)
package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;

import java.util.List;

public class Colosse extends ClassModel {
    public Colosse() {
        super("colosse", "Colosse", "Une montagne de muscles impossible à faire vaciller.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 250, false,
                30, 70, 5, 70, 10, 15);
    }
}