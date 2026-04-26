package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class FaucheurEternel extends ClassModel {
    public FaucheurEternel() {
        super("faucheur_eternel", "Faucheur Eternel", "La mort incarnee qui moissonne sans fin depuis l'aube des temps.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.BLOOD_HUNT, PassiveSkill.FATAL_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                138, 52, 8, 52, 156, 104);
    }
}
