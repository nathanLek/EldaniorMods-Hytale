package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class CatalyseurArcane extends ClassModel {
    public CatalyseurArcane() {
        super("catalyseur_arcane", "Catalyseur Arcane", "Son corps est un catalyseur de magie pure qui amplifie tout sort.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.EXPANDED_MIND, PassiveSkill.ARCANE_STRIKE, PassiveSkill.MANAWELL), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                2, 8, 46, 8, 12, 6);
    }
}
