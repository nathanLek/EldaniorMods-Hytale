package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LameSpectrale extends ClassModel {
    public LameSpectrale() {
        super("lame_spectrale", "Lame Spectrale", "Sa lame traverse les armures comme un spectre traverse les murs.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.VOID_BLADE, PassiveSkill.CRUSHING_PRESSURE, PassiveSkill.ABSOLUTE_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                104, 44, 26, 44, 190, 122);
    }
}
