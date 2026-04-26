package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Demiurge extends ClassModel {
    public Demiurge() {
        super("demiurge", "Demiurge", "Le Demiurge possede le pouvoir de creation. Sa magie donne forme a la matiere et insuffle la vie.",
                Rarity.LEGENDARY, ClassType.MAGE,
                List.of(PassiveSkill.COSMIC_MIND, PassiveSkill.MANA_INFINITY, PassiveSkill.COSMIC_CONSTITUTION),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("demiurge_supreme", "createur_cosmique", "architecte_du_monde"), 400, false,
                60, 120, 300, 100, 60, 80);
    }
}
