package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class AvatarArcanique extends ClassModel {
    public AvatarArcanique() {
        super("avatar_arcanique", "Avatar Arcanique", "L'Avatar Arcanique est la magie incarnee. Chaque souffle, chaque geste est une manifestation de puissance pure.",
                Rarity.DIVINE, ClassType.MAGE,
                List.of(PassiveSkill.INFINITE_MIND, PassiveSkill.ARCANE_GENESIS, PassiveSkill.GENESIS_STRIKE),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of(), 250, false,
                80, 200, 500, 150, 100, 200);
    }
}
