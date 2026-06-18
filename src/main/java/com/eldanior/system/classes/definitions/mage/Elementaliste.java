package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Elementaliste extends ClassModel {
    public Elementaliste() {
        super("elementaliste", "Elementaliste", "L'Elementaliste maitrise les forces primordiales du feu, de la glace et de la foudre. Il dechaîne les elements sur ses ennemis.",
                Rarity.COMMON, ClassType.MAGE,
                List.of(PassiveSkill.AWAKENED_MIND, PassiveSkill.ARCANE_STRIKE, PassiveSkill.MANA_FONT),
                List.of("TEMPETE_ELEMENTAIRE"),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("elementaliste_maitre", "seigneur_des_elements", "catalyseur_arcane"), 400, false,
                2, 6, 26, 6, 6, 4);
    }
}
