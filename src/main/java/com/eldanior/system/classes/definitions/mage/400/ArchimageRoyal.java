package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArchimageRoyal extends ClassModel {
    public ArchimageRoyal() {
        super("archimage_royal", "Archimage Royal", "L'archimage de la cour royale. Sa sagesse guide les rois.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.EXPANDED_MIND, PassiveSkill.MANA_CITADEL, PassiveSkill.ARCANE_STRIKE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                20, 54, 100, 36, 32, 32);
    }
}
