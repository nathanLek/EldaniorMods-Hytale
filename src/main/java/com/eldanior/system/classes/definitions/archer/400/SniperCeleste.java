package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SniperCeleste extends ClassModel {
    public SniperCeleste() {
        super("sniper_celeste", "Sniper Celeste", "Un tireur dont les fleches tombent du ciel comme la foudre divine.",
                Rarity.UNIQUE, ClassType.ARCHER, List.of(PassiveSkill.FATAL_PRECISION, PassiveSkill.VOID_BLADE, PassiveSkill.CRIMSON_BLADE), List.of(WeaponMastery.BOW), List.of(), 400, false,
                68, 52, 26, 34, 138, 174);
    }
}
