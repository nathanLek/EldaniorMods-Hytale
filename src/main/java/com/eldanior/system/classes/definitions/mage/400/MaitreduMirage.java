package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaitreduMirage extends ClassModel {
    public MaitreduMirage() {
        super("maitre_du_mirage", "Maitre du Mirage", "Ses mirages sont si parfaits que la realite elle-meme doute.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.DIMENSIONAL_DODGE, PassiveSkill.GALE_STEP, PassiveSkill.EXPANDED_MIND), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                12, 32, 76, 24, 68, 62);
    }
}
