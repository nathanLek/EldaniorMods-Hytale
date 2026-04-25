package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Magus extends ClassModel {
    public Magus() {
        super("magus", "Magus", "Le Magus a atteint le sommet de l'art arcanique. Ses sorts peuvent raser des montagnes et soulever des oceans.",
                Rarity.EPIC, ClassType.MAGE,
                List.of(PassiveSkill.BRILLIANT_MIND, PassiveSkill.ARCANE_DEVASTATION, PassiveSkill.MANA_FORTRESS),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of(), 250, false,
                20, 60, 120, 50, 30, 40);
    }
}
