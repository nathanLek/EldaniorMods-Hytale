package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Trancheur extends ClassModel {
    public Trancheur() {
        super("trancheur", "Trancheur", "Le Trancheur decoupe ses adversaires avec une precision chirurgicale. Sa lame fend l'air si vite que l'ennemi ne voit jamais le coup venir.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.INSTINCTIVE_STRIKE, PassiveSkill.WIND_STEP, PassiveSkill.RAZOR_SENSES), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                42, 26, 6, 17, 28, 10);
    }
}