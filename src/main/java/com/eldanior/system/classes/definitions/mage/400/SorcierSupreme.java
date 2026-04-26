package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SorcierSupreme extends ClassModel {
    public SorcierSupreme() {
        super("sorcier_supreme", "Sorcier Supreme", "Le sorcier supreme dont la magie noire n'a pas de limites.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.SOUL_STEALER, PassiveSkill.DEADLY_PRECISION, PassiveSkill.ARCANE_DEVASTATION), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                18, 34, 96, 26, 34, 70);
    }
}
