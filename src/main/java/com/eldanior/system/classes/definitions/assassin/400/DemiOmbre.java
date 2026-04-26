package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DemiOmbre extends ClassModel {
    public DemiOmbre() {
        super("demi_ombre", "Demi-Ombre", "Mi-dieu mi-ombre. Sa puissance eclipserait le soleil s'il le voulait.",
                Rarity.DIVINE, ClassType.ASSASSIN, List.of(PassiveSkill.CREATOR_PRECISION, PassiveSkill.CREATOR_SWIFTNESS, PassiveSkill.GOD_SLAYER_SWIFTNESS), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                340, 255, 85, 204, 765, 680);
    }
}
