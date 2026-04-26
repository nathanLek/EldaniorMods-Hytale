package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TireurdElite extends ClassModel {
    public TireurdElite() {
        super("tireur_d_elite", "Tireur d'Elite", "Un tireur d'elite dont chaque fleche atteint sa cible avec une precision mortelle.",
                Rarity.RARE, ClassType.ARCHER, List.of(PassiveSkill.EAGLE_EYE, PassiveSkill.RAZOR_SENSES, PassiveSkill.CRITICAL_LUCK), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                10, 10, 7, 7, 28, 24);
    }
}
