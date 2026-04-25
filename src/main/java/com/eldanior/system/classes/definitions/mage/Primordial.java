package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Primordial extends ClassModel {
    public Primordial() {
        super("primordial", "Primordial", "Le Primordial puise dans les forces qui existaient avant la creation du monde. Sa magie est aussi ancienne que l'univers.",
                Rarity.UNIQUE, ClassType.MAGE,
                List.of(PassiveSkill.GENIUS_MIND, PassiveSkill.ETERNAL_LIFE, PassiveSkill.MANA_OCEAN),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of(), 250, false,
                40, 100, 180, 70, 30, 50);
    }
}
