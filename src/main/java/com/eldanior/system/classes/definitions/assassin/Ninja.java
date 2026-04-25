package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Ninja extends ClassModel {
    public Ninja() {
        super("ninja", "Ninja", "Le Ninja est un guerrier de l'ombre venu d'Orient. Sa discipline et ses techniques secretes sont legendaires.",
                Rarity.RARE, ClassType.ASSASSIN,
                List.of(PassiveSkill.GALE_STEP, PassiveSkill.THUNDER_REFLEXES, PassiveSkill.CATLIKE_POISE),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of(), 250, false,
                25, 15, 10, 10, 65, 30);
    }
}
