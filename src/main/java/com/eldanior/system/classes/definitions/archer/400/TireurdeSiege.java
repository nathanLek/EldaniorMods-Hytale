package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TireurdeSiege extends ClassModel {
    public TireurdeSiege() {
        super("tireur_de_siege", "Tireur de Siege", "Un specialiste du tir a longue portee capable de toucher depuis l'horizon.",
                Rarity.RARE, ClassType.ARCHER, List.of(PassiveSkill.PRESSURE_POINT, PassiveSkill.HAWK_EYE, PassiveSkill.KEEN_SENSES), List.of(WeaponMastery.BOW), List.of(), 400, false,
                12, 16, 4, 8, 22, 16);
    }
}
