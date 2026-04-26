package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ForceOriginelle extends ClassModel {
    public ForceOriginelle() {
        super("force_originelle", "Force Originelle", "La force a l'origine de toute magie. Pure et incommensurable.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.COSMIC_MIND, PassiveSkill.ETERNAL_LIFE, PassiveSkill.ARCANE_ANNIHILATION), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                72, 166, 300, 116, 48, 82);
    }
}
