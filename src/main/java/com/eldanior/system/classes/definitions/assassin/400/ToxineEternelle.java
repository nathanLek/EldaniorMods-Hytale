package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ToxineEternelle extends ClassModel {
    public ToxineEternelle() {
        super("toxine_eternelle", "Toxine Eternelle", "Un poison qui ne guerit jamais. Eternel comme son createur.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.HYDRA_BLOOD, PassiveSkill.SPIRIT_DRAIN, PassiveSkill.GOD_SLAYER_SWIFTNESS), List.of(WeaponMastery.DAGGER), List.of(), 400, false,
                104, 96, 64, 64, 208, 234);
    }
}
