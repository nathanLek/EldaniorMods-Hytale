package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TisseurdeRunes extends ClassModel {
    public TisseurdeRunes() {
        super("tisseur_de_runes", "Tisseur de Runes", "Un tisserand de magie qui grave les runes dans l'air.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.MANA_BARRIER, PassiveSkill.BRILLIANT_MIND, PassiveSkill.STEEL_CONSTITUTION), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                2, 16, 36, 12, 8, 8);
    }
}
