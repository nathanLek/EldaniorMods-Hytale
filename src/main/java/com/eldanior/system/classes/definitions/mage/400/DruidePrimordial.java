package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DruidePrimordial extends ClassModel {
    public DruidePrimordial() {
        super("druide_primordial", "Druide Primordial", "Un druide primordial en contact avec les forces originelles de la nature.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.VITAL_RECOVERY, PassiveSkill.BURSTING_LIFE, PassiveSkill.GENIUS_MIND), List.of(WeaponMastery.STAFF), List.of(), 400, false,
                16, 72, 76, 48, 28, 32);
    }
}
