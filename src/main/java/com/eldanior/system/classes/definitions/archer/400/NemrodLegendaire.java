package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class NemrodLegendaire extends ClassModel {
    public NemrodLegendaire() {
        super("nemrod_legendaire", "Nemrod Legendaire", "Le chasseur mythique dont les exploits resonnent a travers les ages.",
                Rarity.EPIC, ClassType.ARCHER, List.of(PassiveSkill.RELENTLESS_HUNT, PassiveSkill.STORM_STEP, PassiveSkill.STEEL_NERVES), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                32, 36, 8, 28, 90, 80);
    }
}
