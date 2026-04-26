package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TrancheurNoir extends ClassModel {
    public TrancheurNoir() {
        super("trancheur_noir", "Trancheur Noir", "Le trancheur des tenebres dont les coups fendent la realite.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.SHARP_BLADE, PassiveSkill.BLOOD_HUNT, PassiveSkill.DEADLY_PRECISION), List.of(WeaponMastery.DAGGER), List.of(), 400, false,
                62, 14, 8, 14, 90, 72);
    }
}
