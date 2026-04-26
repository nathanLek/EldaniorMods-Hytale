package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArcheDruide extends ClassModel {
    public ArcheDruide() {
        super("arche_druide", "Arche Druide", "Le druide supreme, gardien de l'equilibre naturel.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.VITAL_RECOVERY, PassiveSkill.TROLL_BLOOD, PassiveSkill.TITAN_CONSTITUTION), List.of(WeaponMastery.STAFF), List.of(), 400, false,
                14, 70, 78, 52, 26, 34);
    }
}
