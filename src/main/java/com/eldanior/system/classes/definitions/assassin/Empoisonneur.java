package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Empoisonneur extends ClassModel {
    public Empoisonneur() {
        super("empoisonneur", "Empoisonneur", "L'Empoisonneur maitrise l'art des toxines mortelles. Une simple eraflure de sa lame peut etre fatale.",
                Rarity.COMMON, ClassType.ASSASSIN,
                List.of(PassiveSkill.HAUNTING_THRUST, PassiveSkill.KEEN_SENSES, PassiveSkill.LUCKY_STRIKE),
                List.of(WeaponMastery.DAGGER),
                List.of(), 120, false,
                6, 4, 6, 2, 16, 16);
    }
}
