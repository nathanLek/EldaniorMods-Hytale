package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class OmbredesVents extends ClassModel {
    public OmbredesVents() {
        super("ombre_des_vents", "Ombre des Vents", "Plus rapide que le vent. Ses fleches arrivent avant le son.",
                Rarity.RARE, ClassType.ARCHER, List.of(PassiveSkill.WIND_STEP, PassiveSkill.RAZOR_SENSES, PassiveSkill.CRITICAL_LUCK), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                8, 6, 6, 6, 32, 26);
    }
}
