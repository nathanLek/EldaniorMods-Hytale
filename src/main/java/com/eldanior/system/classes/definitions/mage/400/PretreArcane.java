package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class PretreArcane extends ClassModel {
    public PretreArcane() {
        super("pretre_arcane", "Pretre Arcane", "Un pretre qui canalise la magie arcanique pour guerir.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.MANA_FONT, PassiveSkill.ROBUST_CONSTITUTION, PassiveSkill.ENRICHED_BLOOD), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                2, 22, 36, 12, 4, 8);
    }
}
