package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class PisteurSauvage extends ClassModel {
    public PisteurSauvage() {
        super("pisteur_sauvage", "Pisteur Sauvage", "Un pisteur qui ne fait qu'un avec la nature sauvage.",
                Rarity.RARE, ClassType.ARCHER, List.of(PassiveSkill.SURVIVAL_INSTINCT, PassiveSkill.MARATHON_RUNNER, PassiveSkill.WIND_STEP), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                8, 12, 4, 12, 22, 22);
    }
}
