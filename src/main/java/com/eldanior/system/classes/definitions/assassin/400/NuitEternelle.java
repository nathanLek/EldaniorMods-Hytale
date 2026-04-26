package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class NuitEternelle extends ClassModel {
    public NuitEternelle() {
        super("nuit_eternelle", "Nuit Eternelle", "La nuit sans fin qui engloutira le monde.",
                Rarity.LEGENDARY, ClassType.ASSASSIN, List.of(PassiveSkill.GENESIS_EDGE, PassiveSkill.ABSOLUTE_PRECISION, PassiveSkill.CREATOR_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                200, 98, 36, 98, 420, 310);
    }
}
