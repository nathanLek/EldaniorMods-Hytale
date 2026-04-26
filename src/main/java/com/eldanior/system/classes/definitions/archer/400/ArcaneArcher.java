package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArcaneArcher extends ClassModel {
    public ArcaneArcher() {
        super("arcane_archer", "Arcane Archer", "L'archer arcanique ultime. Ses fleches sont des tempetes de magie.",
                Rarity.UNIQUE, ClassType.ARCHER, List.of(PassiveSkill.ARCANE_ANNIHILATION, PassiveSkill.GENIUS_MIND, PassiveSkill.MANA_OCEAN), List.of(WeaponMastery.BOW, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                52, 52, 86, 34, 122, 104);
    }
}
