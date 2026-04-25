package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Sorcier extends ClassModel {
    public Sorcier() {
        super("sorcier", "Sorcier", "Le Sorcier manipule les forces obscures avec une maitrise terrifiante. Ses maledictions sont legendaires.",
                Rarity.RARE, ClassType.MAGE,
                List.of(PassiveSkill.SOUL_STEALER, PassiveSkill.RAZOR_SENSES, PassiveSkill.SPELLBLADE),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of(), 250, false,
                10, 20, 55, 15, 20, 40);
    }
}
