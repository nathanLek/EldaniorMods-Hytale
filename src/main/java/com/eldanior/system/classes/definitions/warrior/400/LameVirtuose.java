package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LameVirtuose extends ClassModel {
    public LameVirtuose() {
        super("lame_virtuose", "Lame Virtuose", "La Lame Virtuose manie l'epee avec une grace incomparable. Chaque mouvement est une oeuvre d'art letale, alliant precision et elegance mortelle.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.DUELIST_SWIFTNESS, PassiveSkill.RAZOR_SENSES, PassiveSkill.VITAL_PRESSURE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                26, 20, 7, 13, 30, 16);
    }
}