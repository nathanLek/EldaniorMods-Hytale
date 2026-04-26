package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaitreElementaire extends ClassModel {
    public MaitreElementaire() {
        super("maitre_elementaire", "Maitre Elementaire", "Le Maitre Elementaire commande tous les elements a la perfection. Feu, glace, foudre et terre obeissent a sa volonte.",
                Rarity.EPIC, ClassType.MAGE,
                List.of(PassiveSkill.ARCANE_DEVASTATION, PassiveSkill.BRILLIANT_MIND, PassiveSkill.LIGHTNING_REFLEXES),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("seigneur_des_elements_mage", "elementaire_supreme", "force_naturelle"), 400, false,
                25, 50, 120, 40, 50, 30);
    }
}
