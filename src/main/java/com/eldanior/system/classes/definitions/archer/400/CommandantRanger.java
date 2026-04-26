package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class CommandantRanger extends ClassModel {
    public CommandantRanger() {
        super("commandant_ranger", "Commandant Ranger", "Le commandant des rangers, meneur d'hommes et tireur hors pair.",
                Rarity.EPIC, ClassType.ARCHER, List.of(PassiveSkill.MARATHON_RUNNER, PassiveSkill.TITAN_CONSTITUTION, PassiveSkill.DEADLY_PRECISION), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                44, 52, 18, 34, 70, 60);
    }
}
