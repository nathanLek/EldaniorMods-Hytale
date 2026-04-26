package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SeigneurPoison extends ClassModel {
    public SeigneurPoison() {
        super("seigneur_poison", "Seigneur du Poison", "Le Seigneur du Poison commande la mort lente. Ses toxines peuvent anihiler des armees entieres.",
                Rarity.UNIQUE, ClassType.ASSASSIN,
                List.of(PassiveSkill.DEATH_HUNT, PassiveSkill.PHOENIX_BLOOD, PassiveSkill.FATAL_PRECISION),
                List.of(WeaponMastery.DAGGER),
                List.of("poison_primordial", "seigneur_des_venins", "toxine_eternelle"), 400, false,
                60, 60, 40, 40, 120, 140);
    }
}
