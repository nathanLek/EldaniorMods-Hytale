package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Eclaireur extends ClassModel {
    public Eclaireur() {
        super("eclaireur_archer", "Eclaireur", "L'Eclaireur se deplace vite et frappe de loin. Sa mobilite est son plus grand atout.",
                Rarity.COMMON, ClassType.ARCHER,
                List.of(PassiveSkill.WIND_STEP, PassiveSkill.LIGHT_REFLEXES, PassiveSkill.LUCKY_STRIKE),
                List.of(WeaponMastery.BOW, WeaponMastery.DAGGER),
                List.of("eclaireur_fantome_archer", "ombre_des_vents", "coureur_d_aurore"), 400, false,
                4, 4, 4, 4, 20, 14);
    }
}
