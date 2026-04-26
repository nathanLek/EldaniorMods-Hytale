package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MarechaldesArcs extends ClassModel {
    public MarechaldesArcs() {
        super("marechal_des_arcs", "Marechal des Arcs", "Le plus haut grade des archers. Sa tactique est infaillible.",
                Rarity.UNIQUE, ClassType.ARCHER, List.of(PassiveSkill.TITAN_CONSTITUTION, PassiveSkill.ABSOLUTE_PRECISION, PassiveSkill.CRIMSON_BLADE), List.of(WeaponMastery.BOW, WeaponMastery.SWORD), List.of(), 400, false,
                86, 104, 34, 86, 104, 104);
    }
}
