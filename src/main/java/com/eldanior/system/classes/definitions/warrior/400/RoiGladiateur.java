package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class RoiGladiateur extends ClassModel {
    public RoiGladiateur() {
        super("roi_gladiateur", "Roi Gladiateur", "Le Roi Gladiateur regne en maitre absolu dans l'arene. Sa lame tranchante et ses sens aiguises font de chaque combat une execution royale.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.SHARP_BLADE, PassiveSkill.BATTLE_FRENZY, PassiveSkill.DEADLY_PRECISION), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                82, 52, 7, 48, 82, 52);
    }
}