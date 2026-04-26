package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArchiMagus extends ClassModel {
    public ArchiMagus() {
        super("archi_magus", "Archi-Magus", "L'archi-magus dont le savoir remonte a l'aube de la magie.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.BRILLIANT_MIND, PassiveSkill.ARCANE_DEVASTATION, PassiveSkill.MANA_OCEAN), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                32, 100, 204, 82, 48, 68);
    }
}
