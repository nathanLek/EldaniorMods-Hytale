package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SouveraindesOmbres extends ClassModel {
    public SouveraindesOmbres() {
        super("souverain_des_ombres", "Souverain des Ombres", "Le monarque absolu du royaume des ombres. Sa volonte plie les tenebres.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.VOID_STEP, PassiveSkill.SHADOW_DODGE, PassiveSkill.FATAL_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                86, 52, 18, 52, 174, 140);
    }
}
