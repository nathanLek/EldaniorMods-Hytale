package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class AngeDechu extends ClassModel {
    public AngeDechu() {
        super("ange_dechu", "Ange Dechu", "L'Ange Dechu a renonce a la lumiere pour la vitesse pure. Il se deplace si vite que le temps semble s'arreter.",
                Rarity.EPIC, ClassType.ASSASSIN,
                List.of(PassiveSkill.STORM_STEP, PassiveSkill.LIGHTNING_REFLEXES, PassiveSkill.ACROBATIC_POISE),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of(), 250, false,
                40, 30, 10, 20, 130, 60);
    }
}