package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ForceNaturelle extends ClassModel {
    public ForceNaturelle() {
        super("force_naturelle", "Force Naturelle", "Une force de la nature elle-meme. Indomptable et primordiale.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.ARCANE_ANNIHILATION, PassiveSkill.TITAN_CONSTITUTION, PassiveSkill.MANA_FORTRESS), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                48, 90, 200, 74, 82, 48);
    }
}
