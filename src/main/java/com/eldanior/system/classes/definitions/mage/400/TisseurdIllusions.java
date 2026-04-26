package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TisseurdIllusions extends ClassModel {
    public TisseurdIllusions() {
        super("tisseur_d_illusions", "Tisseur d'Illusions", "Il tisse des illusions comme d'autres tissent la soie.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.PHANTOM_DODGE, PassiveSkill.SHADOW_DODGE, PassiveSkill.GENIUS_MIND), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                8, 36, 74, 28, 66, 58);
    }
}
