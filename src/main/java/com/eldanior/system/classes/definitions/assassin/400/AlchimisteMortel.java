package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class AlchimisteMortel extends ClassModel {
    public AlchimisteMortel() {
        super("alchimiste_mortel", "Alchimiste Mortel", "Ses poisons sont si raffines qu'une goutte suffit a terrasser un geant.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.HAUNTING_THRUST, PassiveSkill.RAZOR_SENSES, PassiveSkill.DEADLY_PRECISION), List.of(WeaponMastery.DAGGER), List.of(), 400, false,
                8, 8, 12, 4, 26, 26);
    }
}
