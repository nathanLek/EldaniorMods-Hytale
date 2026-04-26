package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LegendeVivante extends ClassModel {
    public LegendeVivante() {
        super("legende_vivante", "Legende Vivante", "La Legende Vivante transcende le mythe pour incarner la perfection guerriere. Chaque aspect de son etre a ete forge par des epreuves impossibles.",
                Rarity.LEGENDARY, ClassType.WARRIOR, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.GOD_CONSTITUTION, PassiveSkill.ABSOLUTE_PRECISION), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                250, 250, 250, 250, 140, 132);
    }
}