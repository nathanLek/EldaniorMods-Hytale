package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ConquerantUltime extends ClassModel {
    public ConquerantUltime() {
        super("conquerant_ultime", "Conquerant Ultime", "Le Conquerant Ultime ne connait aucune limite dans sa soif de victoire. Sa puissance brute et ses reflexes cosmiques ecrasent toute resistance.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.LIGHTNING_REFLEXES, PassiveSkill.WAR_LEGEND), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                170, 130, 48, 132, 86, 98);
    }
}