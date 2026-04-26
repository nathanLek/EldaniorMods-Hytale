package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class RangerElite extends ClassModel {
    public RangerElite() {
        super("ranger_elite", "Ranger d'Elite", "Le Ranger d'Elite est un gardien de la nature. Son arc protege les forets et punit les intrus.",
                Rarity.RARE, ClassType.ARCHER,
                List.of(PassiveSkill.MARATHON_RUNNER, PassiveSkill.CRITICAL_LUCK, PassiveSkill.OVERFLOWING_LIFE),
                List.of(WeaponMastery.BOW, WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of("commandant_ranger", "garde_forestier", "sentinelle_d_elite"), 400, false,
                25, 30, 10, 20, 40, 35);
    }
}
