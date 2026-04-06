package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;

import java.util.List;

public class Novice extends ClassModel {

    public Novice() {
        super(
                "novice",
                "Novice",
                "Juste un Novice.",
                Rarity.COMMON,
                ClassType.NOVICE,
                List.of(),
                List.of(WeaponMastery.ANY, WeaponMastery.SHIELD, WeaponMastery.SPEAR, WeaponMastery.STAFF, WeaponMastery.SPELLBOOK, WeaponMastery.SWORD, WeaponMastery.BOW, WeaponMastery.CLUB, WeaponMastery.AXE, WeaponMastery.MACE, WeaponMastery.DAGGER),
                List.of("assassin", "warrior", "mage", "merchant", "archer"),
                20,
                false,
                0, 0, 0, 0, 0, 0
        );
    }
}