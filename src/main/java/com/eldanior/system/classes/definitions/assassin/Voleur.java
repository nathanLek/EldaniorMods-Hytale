package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Voleur extends ClassModel {
    public Voleur() {
        super("voleur", "Voleur", "Le Voleur excelle dans le larcin et la discretion. Ses doigts agiles derobent aussi bien des bourses que des vies.",
                Rarity.COMMON, ClassType.ASSASSIN,
                List.of(PassiveSkill.WIND_STEP, PassiveSkill.KEEN_SENSES, PassiveSkill.LUCKY_STRIKE),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of("voleur_maitre", "prince_du_larcin", "main_d_argent"), 400, false,
                8, 4, 2, 2, 20, 14);
    }
}
