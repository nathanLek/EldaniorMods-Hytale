package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ChasseurVeteran extends ClassModel {
    public ChasseurVeteran() {
        super("chasseur_veteran_archer", "Chasseur Veteran", "Un chasseur aguerri par des decennies de traque dans les terres sauvages.",
                Rarity.RARE, ClassType.ARCHER, List.of(PassiveSkill.EAGLE_EYE, PassiveSkill.RELENTLESS_HUNT, PassiveSkill.RAZOR_SENSES), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                10, 14, 4, 10, 24, 20);
    }
}
