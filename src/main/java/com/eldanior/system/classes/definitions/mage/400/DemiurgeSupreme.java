package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DemiurgeSupreme extends ClassModel {
    public DemiurgeSupreme() {
        super("demiurge_supreme", "Demiurge Supreme", "Le demiurge supreme dont le pouvoir facon la realite.",
                Rarity.LEGENDARY, ClassType.MAGE, List.of(PassiveSkill.COSMIC_MIND, PassiveSkill.MANA_INFINITY, PassiveSkill.HEART_OF_GENESIS), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                102, 204, 510, 170, 102, 136);
    }
}
