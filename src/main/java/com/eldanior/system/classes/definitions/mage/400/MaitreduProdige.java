package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaitreduProdige extends ClassModel {
    public MaitreduProdige() {
        super("maitre_du_prodige", "Maitre du Prodige", "Le maitre des prodiges dont la magie defie les lois de la nature.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.UNLEASHED_MAGIC, PassiveSkill.OVERFLOWING_LIFE, PassiveSkill.BRILLIANT_MIND), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                12, 58, 84, 42, 28, 42);
    }
}
