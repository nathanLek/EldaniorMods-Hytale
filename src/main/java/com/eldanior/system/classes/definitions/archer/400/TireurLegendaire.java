package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TireurLegendaire extends ClassModel {
    public TireurLegendaire() {
        super("tireur_legendaire", "Tireur Legendaire", "Un tireur dont la legende est contee dans tous les royaumes.",
                Rarity.EPIC, ClassType.ARCHER, List.of(PassiveSkill.HAWK_EYE, PassiveSkill.FATAL_PRECISION, PassiveSkill.CRIMSON_BLADE), List.of(WeaponMastery.BOW), List.of(), 400, false,
                34, 26, 18, 18, 86, 86);
    }
}
