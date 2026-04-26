package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Samourai extends ClassModel {

    public Samourai() {
        super(
                "samourai",
                "Samourai",
                "Le Samourai suit le code du Bushido. Sa discipline et son honneur se traduisent par des coups d'une precision mortelle.",
                Rarity.RARE,
                ClassType.WARRIOR,
                List.of(PassiveSkill.RELENTLESS_HUNT, PassiveSkill.THUNDER_REFLEXES, PassiveSkill.GALE_STEP),
                List.of(WeaponMastery.SWORD, WeaponMastery.DAGGER),
                List.of("kensei", "shogun", "ronin_legendaire"),
                400,
                false,
                45, 25, 10, 20, 60, 40
        );
    }
}