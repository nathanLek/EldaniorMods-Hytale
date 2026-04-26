package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SpectreSilencieux extends ClassModel {
    public SpectreSilencieux() {
        super("spectre_silencieux", "Spectre Silencieux", "Plus silencieux que le vent, plus mortel que la nuit.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.PHANTOM_DODGE, PassiveSkill.GALE_STEP, PassiveSkill.VOID_STEP), List.of(WeaponMastery.DAGGER), List.of(), 400, false,
                36, 24, 10, 16, 100, 74);
    }
}
