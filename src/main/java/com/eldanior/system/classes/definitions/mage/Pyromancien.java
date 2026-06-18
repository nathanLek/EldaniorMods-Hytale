package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Pyromancien extends ClassModel {
    public Pyromancien() {
        super("pyromancien", "Pyromancien", "Le Pyromancien maitrise l'art du feu destructeur. Ses flammes consument tout sur leur passage.",
                Rarity.COMMON, ClassType.MAGE,
                List.of(PassiveSkill.ARCANE_STRIKE, PassiveSkill.INSTINCTIVE_STRIKE, PassiveSkill.KEEN_SENSES),
                List.of("INFERNO"),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("maitre_des_flammes", "infernal_mage", "brasier_vivant"), 400, false,
                4, 4, 26, 4, 6, 6);
    }
}
