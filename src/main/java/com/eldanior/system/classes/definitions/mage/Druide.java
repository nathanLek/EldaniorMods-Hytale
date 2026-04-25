package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Druide extends ClassModel {
    public Druide() {
        super("druide", "Druide", "Le Druide est un gardien de la nature. Il puise sa force dans les elements vivants et les esprits de la foret.",
                Rarity.RARE, ClassType.MAGE,
                List.of(PassiveSkill.VITAL_RECOVERY, PassiveSkill.ENRICHED_BLOOD, PassiveSkill.MANA_STREAM),
                List.of(WeaponMastery.STAFF),
                List.of(), 250, false,
                8, 40, 45, 30, 15, 20);
    }
}
