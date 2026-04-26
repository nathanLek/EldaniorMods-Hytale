package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Chronoturge extends ClassModel {
    public Chronoturge() {
        super("chronoturge", "Chronoturge", "Le Chronoturge manipule le flux du temps. Il peut ralentir ses ennemis et accelerer ses propres actions.",
                Rarity.EPIC, ClassType.MAGE,
                List.of(PassiveSkill.STORM_STEP, PassiveSkill.MANA_RIVER, PassiveSkill.SHADOW_DODGE),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("maitre_du_temps", "chrono_seigneur", "tisseur_temporel"), 400, false,
                15, 40, 100, 30, 70, 50);
    }
}
