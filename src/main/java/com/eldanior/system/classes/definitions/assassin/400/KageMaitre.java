package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class KageMaitre extends ClassModel {
    public KageMaitre() {
        super("kage_maitre", "Kage Maitre", "Le maitre des ombres. Nul ne connait son vrai visage.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.SHADOW_DODGE, PassiveSkill.GALE_STEP, PassiveSkill.VOID_BLADE), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                40, 28, 16, 16, 108, 54);
    }
}
