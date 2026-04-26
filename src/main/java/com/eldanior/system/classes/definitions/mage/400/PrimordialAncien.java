package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class PrimordialAncien extends ClassModel {
    public PrimordialAncien() {
        super("primordial_ancien", "Primordial Ancien", "Le plus ancien des primordinaux. Son existence est un mythe.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.GENIUS_MIND, PassiveSkill.HEART_OF_ETERNITY, PassiveSkill.MANA_INFINITY), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                68, 170, 306, 120, 52, 86);
    }
}
