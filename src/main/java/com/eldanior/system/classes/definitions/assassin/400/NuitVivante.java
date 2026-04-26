package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class NuitVivante extends ClassModel {
    public NuitVivante() {
        super("nuit_vivante", "Nuit Vivante", "L'incarnation de la nuit elle-meme. Ou elle passe, les lumieres s'eteignent.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.DARK_VISION, PassiveSkill.SHADOW_DODGE, PassiveSkill.DIMENSIONAL_STEP), List.of(WeaponMastery.DAGGER), List.of(), 400, false,
                32, 28, 8, 20, 96, 80);
    }
}
