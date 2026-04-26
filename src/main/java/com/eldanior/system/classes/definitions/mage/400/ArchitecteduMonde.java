package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArchitecteduMonde extends ClassModel {
    public ArchitecteduMonde() {
        super("architecte_du_monde", "Architecte du Monde", "L'architecte qui a dessine les fondations du monde.",
                Rarity.LEGENDARY, ClassType.MAGE, List.of(PassiveSkill.COSMIC_MIND, PassiveSkill.GENESIS_STRIKE, PassiveSkill.ETERNAL_LIFE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                106, 208, 500, 174, 106, 132);
    }
}
