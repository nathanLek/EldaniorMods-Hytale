package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Fleurettiste extends ClassModel {
    public Fleurettiste() {
        super("fleurettiste", "Fleurettiste", "Le Fleurettiste excelle dans l'art de la touche precise. Sa lame fine frappe comme l'eclair aux points vitaux, laissant l'ennemi sans defense.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.PRESSURE_POINT, PassiveSkill.WIND_STEP, PassiveSkill.LIGHT_REFLEXES), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                26, 19, 6, 13, 32, 16);
    }
}