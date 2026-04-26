package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class OmbreEternelle extends ClassModel {
    public OmbreEternelle() {
        super("ombre_eternelle", "Ombre Eternelle", "Une ombre qui existera pour toujours. Immortelle et omnipresente.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.DIMENSIONAL_STEP, PassiveSkill.STORM_STEP, PassiveSkill.ABSOLUTE_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                82, 50, 16, 50, 170, 136);
    }
}
