package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class VoyantArcane extends ClassModel {
    public VoyantArcane() {
        super("voyant_arcane", "Voyant Arcane", "Un voyant dont le regard arcanique voit au-dela du temps.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.BRILLIANT_MIND, PassiveSkill.DEADLY_PRECISION, PassiveSkill.PSYCHIC_AWARENESS), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                12, 40, 92, 32, 36, 62);
    }
}
