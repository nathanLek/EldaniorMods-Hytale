package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LameAbsolue extends ClassModel {
    public LameAbsolue() {
        super("lame_absolue", "Lame Absolue", "La lame absolue qui ne peut etre paree ni evitee.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.GENESIS_EDGE, PassiveSkill.DEMIGOD_SWIFTNESS, PassiveSkill.CREATOR_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                174, 64, 20, 64, 310, 166);
    }
}
