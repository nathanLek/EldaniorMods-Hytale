package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class NinjaLegendaire extends ClassModel {
    public NinjaLegendaire() {
        super("ninja_legendaire", "Ninja Legendaire", "Un shinobi dont le nom est murmure avec crainte dans tout le continent.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.STORM_STEP, PassiveSkill.THUNDER_REFLEXES, PassiveSkill.DEADLY_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                44, 26, 18, 18, 112, 52);
    }
}
