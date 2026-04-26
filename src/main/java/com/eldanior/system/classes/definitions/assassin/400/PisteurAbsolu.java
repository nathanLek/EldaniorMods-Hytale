package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class PisteurAbsolu extends ClassModel {
    public PisteurAbsolu() {
        super("pisteur_absolu", "Pisteur Absolu", "Le pisteur ultime dont les sens surpassent ceux de toute creature.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.HAWK_EYE, PassiveSkill.EAGLE_EYE, PassiveSkill.STORM_STEP), List.of(WeaponMastery.DAGGER, WeaponMastery.BOW), List.of(), 400, false,
                32, 28, 8, 28, 82, 88);
    }
}
