package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class PiegeVivant extends ClassModel {
    public PiegeVivant() {
        super("piege_vivant", "Piege Vivant", "Chaque pas de ses ennemis est un piege qui se referme.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.PRESSURE_POINT, PassiveSkill.RAZOR_SENSES, PassiveSkill.BATTLE_FRENZY), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                16, 10, 6, 12, 22, 16);
    }
}
