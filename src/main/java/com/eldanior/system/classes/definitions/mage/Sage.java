package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Sage extends ClassModel {
    public Sage() {
        super("sage", "Sage", "Le Sage est un erudit dont la sagesse eclaire le monde. Son equilibre parfait entre corps et esprit force le respect.",
                Rarity.RARE, ClassType.MAGE,
                List.of(PassiveSkill.STEEL_CONSTITUTION, PassiveSkill.EXPANDED_MIND, PassiveSkill.MARATHON_RUNNER),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of(), 250, false,
                20, 30, 50, 30, 20, 20);
    }
}
