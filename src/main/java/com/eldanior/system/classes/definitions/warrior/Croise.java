package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Croise extends ClassModel {

    public Croise() {
        super(
                "croise",
                "Croise",
                "Le Croise est un guerrier beni par la lumiere divine. Sa foi le rend quasiment invulnerable et ses coups portent le jugement celeste.",
                Rarity.EPIC,
                ClassType.WARRIOR,
                List.of(PassiveSkill.STEEL_BODY, PassiveSkill.BURSTING_LIFE, PassiveSkill.MONSTER_SLAYER_GUARD),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD, WeaponMastery.MACE),
                List.of(),
                250,
                false,
                80, 100, 40, 100, 40, 40
        );
    }
}