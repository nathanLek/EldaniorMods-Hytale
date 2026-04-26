package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GardeForestier extends ClassModel {
    public GardeForestier() {
        super("garde_forestier", "Garde Forestier", "Le gardien supreme de la foret dont l'arc ne rate jamais.",
                Rarity.EPIC, ClassType.ARCHER, List.of(PassiveSkill.OVERFLOWING_LIFE, PassiveSkill.RAZOR_SENSES, PassiveSkill.CRITICAL_LUCK), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                40, 50, 16, 36, 68, 62);
    }
}
