package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SeigneurArcanique extends ClassModel {
    public SeigneurArcanique() {
        super("seigneur_arcanique", "Seigneur Arcanique", "Le seigneur arcanique dont la magie est la loi.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.COSMIC_MIND, PassiveSkill.ARCANE_SUPREMACY, PassiveSkill.ETERNAL_LIFE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                48, 140, 336, 140, 72, 96);
    }
}
