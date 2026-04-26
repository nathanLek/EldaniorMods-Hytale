package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class OmbreErrante extends ClassModel {
    public OmbreErrante() {
        super("ombre_errante", "Ombre Errante", "Un spectre errant entre les ombres, insaisissable et mortel.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.PHANTOM_DODGE, PassiveSkill.GALE_STEP, PassiveSkill.THUNDER_REFLEXES), List.of(WeaponMastery.DAGGER, WeaponMastery.BOW), List.of(), 400, false,
                8, 10, 8, 8, 26, 18);
    }
}
