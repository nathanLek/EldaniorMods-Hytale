package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArcheLiche extends ClassModel {
    public ArcheLiche() {
        super("arche_liche", "Arche-Liche", "La liche supreme qui defie la mort depuis des millenaires.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.SPIRIT_DRAIN, PassiveSkill.ETERNAL_LIFE, PassiveSkill.ARCANE_ANNIHILATION), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                26, 86, 190, 104, 34, 86);
    }
}
