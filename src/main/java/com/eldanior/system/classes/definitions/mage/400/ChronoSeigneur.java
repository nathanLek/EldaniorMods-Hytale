package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ChronoSeigneur extends ClassModel {
    public ChronoSeigneur() {
        super("chrono_seigneur", "Chrono-Seigneur", "Le seigneur du temps dont le pouvoir transcende les epoques.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.STORM_STEP, PassiveSkill.COSMIC_MIND, PassiveSkill.SHADOW_DODGE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                24, 66, 170, 48, 118, 90);
    }
}
