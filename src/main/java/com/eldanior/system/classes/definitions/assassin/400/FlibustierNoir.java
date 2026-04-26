package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class FlibustierNoir extends ClassModel {
    public FlibustierNoir() {
        super("flibustier_noir", "Flibustier Noir", "Un pirate sans pitie dont le pavillon noir glace le sang.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.SHARP_BLADE, PassiveSkill.WAR_FRENZY, PassiveSkill.BLOOD_HUNT), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                54, 32, 10, 24, 74, 66);
    }
}
