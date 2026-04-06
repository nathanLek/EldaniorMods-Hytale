package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Merchant extends ClassModel {

    public Merchant() {
        super(
                "merchant",
                "Marchand",
                "Un negociateur hors pair.",
                Rarity.COMMON,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT),
                List.of(WeaponMastery.ANY, WeaponMastery.SHIELD, WeaponMastery.SPEAR, WeaponMastery.STAFF, WeaponMastery.SPELLBOOK, WeaponMastery.SWORD, WeaponMastery.BOW, WeaponMastery.CLUB, WeaponMastery.AXE, WeaponMastery.MACE, WeaponMastery.DAGGER, WeaponMastery.RIFLE, WeaponMastery.GUN),
                List.of("master_artisan", "relic_hunter", "smuggler"),
                120,
                false,
                2, 1, 2, 2, 2, 10
        );
    }
}