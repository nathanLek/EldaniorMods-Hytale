package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ChuteFinale extends ClassModel {
    public ChuteFinale() {
        super("chute_finale", "Chute Finale", "Sa chute fut si profonde qu'il a touche le fond de l'abime.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.STORM_STEP, PassiveSkill.GRAVITY_DEFIANCE), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                72, 48, 16, 32, 220, 100);
    }
}
