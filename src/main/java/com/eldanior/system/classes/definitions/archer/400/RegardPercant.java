package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class RegardPercant extends ClassModel {
    public RegardPercant() {
        super("regard_percant", "Regard Percant", "Un regard qui perce toute defense, toute illusion, toute verite.",
                Rarity.UNIQUE, ClassType.ARCHER, List.of(PassiveSkill.FATAL_PRECISION, PassiveSkill.CRIMSON_BLADE, PassiveSkill.CREATOR_PRECISION), List.of(WeaponMastery.BOW), List.of(), 400, false,
                96, 72, 32, 54, 196, 260);
    }
}
