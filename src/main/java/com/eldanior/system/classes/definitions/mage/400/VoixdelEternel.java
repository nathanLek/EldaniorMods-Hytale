package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class VoixdelEternel extends ClassModel {
    public VoixdelEternel() {
        super("voix_de_l_eternel", "Voix de l'Eternel", "La voix qui murmure les secrets de l'eternite.",
                Rarity.LEGENDARY, ClassType.MAGE, List.of(PassiveSkill.ABSOLUTE_PRECISION, PassiveSkill.CREATOR_MIND, PassiveSkill.HEART_OF_GENESIS), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                64, 174, 480, 140, 132, 208);
    }
}
