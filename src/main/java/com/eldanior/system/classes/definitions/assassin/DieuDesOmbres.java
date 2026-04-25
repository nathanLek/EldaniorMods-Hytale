package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DieuDesOmbres extends ClassModel {
    public DieuDesOmbres() {
        super("dieu_ombres", "Dieu des Ombres", "Le Dieu des Ombres est la nuit eternelle. Toute lumiere s'eteint en sa presence et seule la mort demeure.",
                Rarity.DIVINE, ClassType.ASSASSIN,
                List.of(PassiveSkill.CREATOR_PRECISION, PassiveSkill.CREATOR_SWIFTNESS, PassiveSkill.CREATOR_BLOOD),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of(), 250, false,
                200, 150, 50, 120, 450, 400);
    }
}
