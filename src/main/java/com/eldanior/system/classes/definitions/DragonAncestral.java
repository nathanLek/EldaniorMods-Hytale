package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DragonAncestral extends ClassModel {

    public DragonAncestral() {
        super(
                "dragon",
                "Dragon Ancestral",
                "Dragon Ancestral Divin",
                Rarity.DIVINE,
                ClassType.DRAGON,
                List.of(PassiveSkill.VOL, PassiveSkill.MANA_HEART),
                List.of(WeaponMastery.ANY, WeaponMastery.SHIELD, WeaponMastery.SPEAR, WeaponMastery.STAFF, WeaponMastery.SPELLBOOK, WeaponMastery.SWORD, WeaponMastery.BOW, WeaponMastery.CLUB, WeaponMastery.AXE, WeaponMastery.MACE, WeaponMastery.DAGGER),
                List.of(),
                999,
                true,
                1000, 1000, 2000, 500, 500, 1000
        );
    }
}