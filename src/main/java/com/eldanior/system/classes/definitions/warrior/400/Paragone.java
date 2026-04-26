package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Paragone extends ClassModel {
    public Paragone() {
        super("paragone", "Paragone", "Le Paragone est le modele ultime de ce qu'un guerrier peut devenir. Son equilibre parfait entre toutes les vertus martiales le rend supreme en toute situation.",
                Rarity.LEGENDARY, ClassType.WARRIOR, List.of(PassiveSkill.GENESIS_EDGE, PassiveSkill.COSMIC_CONSTITUTION, PassiveSkill.WAR_PROPHECY), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                252, 248, 252, 248, 138, 134);
    }
}