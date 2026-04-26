package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArcdesEtoiles extends ClassModel {
    public ArcdesEtoiles() {
        super("arc_des_etoiles", "Arc des Etoiles", "Son arc tire des fleches faites de lumiere stellaire.",
                Rarity.EPIC, ClassType.ARCHER, List.of(PassiveSkill.SPELLBLADE, PassiveSkill.GENIUS_MIND, PassiveSkill.STORM_STEP), List.of(WeaponMastery.BOW, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                28, 28, 56, 20, 72, 66);
    }
}
