package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class PaladinSacre extends ClassModel {
    public PaladinSacre() {
        super("paladin_sacre", "Paladin Sacre", "Le Paladin Sacre est beni par les divinites de la lumiere. Sa foi indefectible lui confere un pouvoir de guerison et de protection miraculeux.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.STEEL_RESOLVE, PassiveSkill.BURSTING_LIFE, PassiveSkill.VITAL_RECOVERY), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD, WeaponMastery.MACE), List.of(), 400, false,
                66, 85, 34, 68, 34, 50);
    }
}