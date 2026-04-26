package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Legionnaire extends ClassModel {
    public Legionnaire() {
        super("legionnaire", "Legionnaire", "Le Legionnaire est un soldat d'elite forge dans le feu des batailles. Sa determination et sa constitution font de lui le dernier homme debout.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.IRON_RESOLVE, PassiveSkill.OVERFLOWING_LIFE, PassiveSkill.PERSEVERANCE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                27, 52, 6, 34, 10, 7);
    }
}