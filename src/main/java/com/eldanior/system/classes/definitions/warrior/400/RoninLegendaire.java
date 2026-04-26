package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class RoninLegendaire extends ClassModel {
    public RoninLegendaire() {
        super("ronin_legendaire", "Ronin Legendaire", "Le Ronin Legendaire erre sans maitre, guide par son propre code d'honneur. Sa liberte lui a confere une maitrise du combat sans egale.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.RELENTLESS_HUNT, PassiveSkill.STORM_STEP, PassiveSkill.DEADLY_PRECISION), List.of(WeaponMastery.SWORD, WeaponMastery.DAGGER), List.of(), 400, false,
                72, 40, 17, 32, 102, 66);
    }
}
