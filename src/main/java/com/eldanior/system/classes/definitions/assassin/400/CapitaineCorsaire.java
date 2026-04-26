package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class CapitaineCorsaire extends ClassModel {
    public CapitaineCorsaire() {
        super("capitaine_corsaire", "Capitaine Corsaire", "Le capitaine des mers sombres, terreur des navires marchands.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.CRITICAL_LUCK, PassiveSkill.BATTLE_FRENZY), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                52, 34, 8, 26, 78, 70);
    }
}
