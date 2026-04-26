package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaitredesPoisons extends ClassModel {
    public MaitredesPoisons() {
        super("maitre_des_poisons", "Maitre des Poisons", "Un alchimiste de la mort dont les concoctions sont legendaires.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.HAUNTING_THRUST, PassiveSkill.KEEN_SENSES, PassiveSkill.SPIRIT_DRAIN), List.of(WeaponMastery.DAGGER), List.of(), 400, false,
                10, 7, 10, 4, 28, 28);
    }
}
