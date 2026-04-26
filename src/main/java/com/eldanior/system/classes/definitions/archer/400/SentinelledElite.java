package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SentinelledElite extends ClassModel {
    public SentinelledElite() {
        super("sentinelle_d_elite", "Sentinelle d'Elite", "La sentinelle ultime. Rien ne passe sans qu'elle le sache.",
                Rarity.EPIC, ClassType.ARCHER, List.of(PassiveSkill.MARATHON_RUNNER, PassiveSkill.LIGHTNING_REFLEXES, PassiveSkill.EAGLE_EYE), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                42, 54, 20, 32, 72, 58);
    }
}
