package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MonarqueNoir extends ClassModel {
    public MonarqueNoir() {
        super("monarque_noir", "Monarque Noir", "Le monarque noir dont le trone est fait des ames de ses ennemis.",
                Rarity.LEGENDARY, ClassType.ASSASSIN, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.DEMIGOD_SWIFTNESS, PassiveSkill.CREATOR_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                166, 140, 48, 100, 370, 336);
    }
}
