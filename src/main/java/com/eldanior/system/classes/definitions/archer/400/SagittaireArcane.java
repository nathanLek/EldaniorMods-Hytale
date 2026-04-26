package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SagittaireArcane extends ClassModel {
    public SagittaireArcane() {
        super("sagittaire_arcane", "Sagittaire Arcane", "Un archer-mage dont les fleches sont des sorts a part entiere.",
                Rarity.EPIC, ClassType.ARCHER, List.of(PassiveSkill.EXPANDED_MIND, PassiveSkill.ARCANE_STRIKE, PassiveSkill.MANA_STREAM), List.of(WeaponMastery.BOW, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                24, 24, 64, 16, 68, 68);
    }
}
