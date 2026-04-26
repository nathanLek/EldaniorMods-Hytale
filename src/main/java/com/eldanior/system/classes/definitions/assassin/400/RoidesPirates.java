package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class RoidesPirates extends ClassModel {
    public RoidesPirates() {
        super("roi_des_pirates", "Roi des Pirates", "Le seigneur inconteste des mers. Sa flotte est une legende.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.RAZOR_SENSES, PassiveSkill.LIGHTNING_REFLEXES), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                50, 36, 8, 28, 76, 72);
    }
}
