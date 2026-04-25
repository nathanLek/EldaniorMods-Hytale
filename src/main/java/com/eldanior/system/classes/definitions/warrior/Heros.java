// 1. Le Héros (Légendaire - La Perfection)
package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Heros extends ClassModel {
    public Heros() {
        super("heros", "Héros", "L'elu des propheties. Le Heros possede des statistiques parfaitement equilibrees et surpuissantes.",
                Rarity.LEGENDARY, ClassType.WARRIOR, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.COSMIC_CONSTITUTION, PassiveSkill.ABSOLUTE_PRECISION), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 250, false,
                150, 150, 150, 150, 80, 80);
    }
}