package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArcAncien extends ClassModel {
    public ArcAncien() {
        super("arc_ancien", "Arc Ancien", "L'Arc Ancien canalise une magie primordiale. Ses fleches percent les dimensions et frappent l'ame.",
                Rarity.UNIQUE, ClassType.ARCHER,
                List.of(PassiveSkill.ARCANE_ANNIHILATION, PassiveSkill.GENIUS_MIND, PassiveSkill.MANA_OCEAN),
                List.of(WeaponMastery.BOW, WeaponMastery.SPELLBOOK),
                List.of(), 250, false,
                40, 50, 80, 40, 80, 100);
    }
}
