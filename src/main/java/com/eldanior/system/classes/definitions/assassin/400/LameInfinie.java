package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LameInfinie extends ClassModel {
    public LameInfinie() {
        super("lame_infinie", "Lame Infinie", "Sa lame n'a ni debut ni fin. Elle tranche a travers le temps lui-meme.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.BERSERKER_SWIFTNESS, PassiveSkill.ABSOLUTE_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                170, 68, 18, 68, 306, 170);
    }
}
