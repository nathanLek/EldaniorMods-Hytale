package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArcRunique extends ClassModel {
    public ArcRunique() {
        super("arc_runique", "Arc Runique", "Un arc grave de runes anciennes qui amplifient chaque fleche.",
                Rarity.EPIC, ClassType.ARCHER, List.of(PassiveSkill.SPELLBLADE, PassiveSkill.BRILLIANT_MIND, PassiveSkill.MANA_FORTRESS), List.of(WeaponMastery.BOW, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                26, 26, 60, 18, 70, 70);
    }
}
