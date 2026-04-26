package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MageVoidAbsolu extends ClassModel {
    public MageVoidAbsolu() {
        super("mage_void_absolu", "Mage Void Absolu", "Le mage du vide absolu. Rien n'existe la ou il regarde.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.VOID_BLADE, PassiveSkill.ARCANE_ANNIHILATION, PassiveSkill.MANA_INFINITY), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                34, 102, 306, 86, 102, 136);
    }
}
