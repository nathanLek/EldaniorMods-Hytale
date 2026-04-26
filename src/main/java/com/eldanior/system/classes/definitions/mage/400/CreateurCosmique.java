package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class CreateurCosmique extends ClassModel {
    public CreateurCosmique() {
        super("createur_cosmique", "Createur Cosmique", "Le createur cosmique qui donne forme aux etoiles.",
                Rarity.LEGENDARY, ClassType.MAGE, List.of(PassiveSkill.CREATOR_MIND, PassiveSkill.ARCANE_ANNIHILATION, PassiveSkill.COSMIC_CONSTITUTION), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                98, 200, 506, 166, 98, 140);
    }
}
