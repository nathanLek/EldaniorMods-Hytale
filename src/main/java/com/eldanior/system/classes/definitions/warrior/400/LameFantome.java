package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LameFantome extends ClassModel {
    public LameFantome() {
        super("lame_fantome", "Lame Fantome", "La Lame Fantome se deplace comme un spectre sur le champ de bataille. Invisible et foudroyante, elle frappe avant que l'ennemi ne realise sa presence.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.STORM_STEP, PassiveSkill.LIGHTNING_REFLEXES, PassiveSkill.RAZOR_SENSES), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                68, 50, 17, 50, 119, 68);
    }
}