package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArbaletrierdElite extends ClassModel {
    public ArbaletrierdElite() {
        super("arbaletrier_d_elite", "Arbaletrier d'Elite", "Un arbaletrier d'elite dont les carreaux percent les armures les plus epaisses.",
                Rarity.RARE, ClassType.ARCHER, List.of(PassiveSkill.PRESSURE_POINT, PassiveSkill.DEADLY_PRECISION, PassiveSkill.RAZOR_SENSES), List.of(WeaponMastery.BOW), List.of(), 400, false,
                14, 14, 4, 10, 20, 20);
    }
}
