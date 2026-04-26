package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class RodeurVeteran extends ClassModel {
    public RodeurVeteran() {
        super("rodeur_veteran", "Rodeur Veteran", "Un pisteur aguerri par des annees d'embuscades. Ses proies ne lui echappent jamais.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.EAGLE_EYE, PassiveSkill.WIND_STEP, PassiveSkill.RELENTLESS_HUNT), List.of(WeaponMastery.DAGGER, WeaponMastery.BOW), List.of(), 400, false,
                10, 10, 7, 7, 28, 20);
    }
}
