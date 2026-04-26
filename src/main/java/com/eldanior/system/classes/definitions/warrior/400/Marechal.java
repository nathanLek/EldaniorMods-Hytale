package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Marechal extends ClassModel {
    public Marechal() {
        super("marechal", "Marechal", "Le Marechal est le sommet de la hierarchie militaire. Sa presence inspire une discipline absolue et sa constitution titanesque le rend presque invincible.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.STEEL_BODY, PassiveSkill.TITAN_CONSTITUTION, PassiveSkill.UNYIELDING), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                50, 136, 17, 119, 34, 17);
    }
}