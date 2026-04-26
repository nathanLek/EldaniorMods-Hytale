package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class PhantomeEternel extends ClassModel {
    public PhantomeEternel() {
        super("phantome_eternel", "Phantome Eternel", "Un spectre de combat qui hante les champs de bataille pour l'eternite.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.DIMENSIONAL_STEP, PassiveSkill.STORM_STEP, PassiveSkill.FATAL_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                100, 42, 24, 42, 186, 118);
    }
}
