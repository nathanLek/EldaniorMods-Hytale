package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class EruditArcane extends ClassModel {
    public EruditArcane() {
        super("erudit_arcane", "Erudit Arcane", "Un erudit dont le savoir arcanique n'a pas de limites.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.TITAN_CONSTITUTION, PassiveSkill.GENIUS_MIND, PassiveSkill.MARATHON_RUNNER), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                32, 50, 88, 50, 36, 36);
    }
}
