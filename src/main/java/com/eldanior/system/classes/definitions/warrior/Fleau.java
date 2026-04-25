// 2. Le Fléau (Légendaire - La Destruction)
package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Fleau extends ClassModel {
    public Fleau() {
        super("fleau", "Fléau", "L'incarnation de la guerre. La ou le Fleau passe, il ne reste que des cendres.",
                Rarity.LEGENDARY, ClassType.WARRIOR, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.DEMIGOD_SWIFTNESS, PassiveSkill.SOUL_CRUSHING_PRESSURE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 250, false,
                280, 120, 40, 120, 120, 80);
    }
}