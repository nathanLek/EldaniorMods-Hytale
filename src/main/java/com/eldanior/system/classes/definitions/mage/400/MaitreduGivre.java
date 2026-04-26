package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaitreduGivre extends ClassModel {
    public MaitreduGivre() {
        super("maitre_du_givre", "Maitre du Givre", "Le maitre du froid absolu dont le gel paralyse tout.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.MANA_BARRIER, PassiveSkill.BRILLIANT_MIND, PassiveSkill.STONE_SKIN), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                4, 14, 40, 10, 10, 7);
    }
}
