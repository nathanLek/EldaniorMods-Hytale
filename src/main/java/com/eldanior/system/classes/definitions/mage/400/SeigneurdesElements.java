package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SeigneurdesElements extends ClassModel {
    public SeigneurdesElements() {
        super("seigneur_des_elements_mage", "Seigneur des Elements", "Le seigneur absolu des elements. La terre, le feu, l'eau et l'air obeissent.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.ARCANE_ANNIHILATION, PassiveSkill.GENIUS_MIND, PassiveSkill.MANA_OCEAN), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                44, 86, 208, 70, 86, 52);
    }
}
