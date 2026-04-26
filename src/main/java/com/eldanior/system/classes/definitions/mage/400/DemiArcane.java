package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DemiArcane extends ClassModel {
    public DemiArcane() {
        super("demi_arcane", "Demi-Arcane", "A mi-chemin entre le mortel et la pure magie. Un etre de pouvoir brut.",
                Rarity.DIVINE, ClassType.MAGE, List.of(PassiveSkill.INFINITE_MIND, PassiveSkill.ARCANE_GENESIS, PassiveSkill.CREATOR_CONSTITUTION), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                136, 340, 850, 255, 170, 340);
    }
}
