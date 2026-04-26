package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GrandCaravanier extends ClassModel {
    public GrandCaravanier() {
        super("grand_caravanier", "Grand Caravanier", "Le maitre des caravanes dont les routes commerciales traversent le monde entier.",
                Rarity.RARE, ClassType.MERCHANT, List.of(PassiveSkill.ARTISANAT, PassiveSkill.TREASURE_HUNTER, PassiveSkill.MARATHON_RUNNER), List.of(WeaponMastery.ANY), List.of(), 400, false,
                10, 14, 7, 14, 14, 30);
    }
}
