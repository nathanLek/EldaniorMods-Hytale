package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MarteauDivin extends ClassModel {
    public MarteauDivin() {
        super("marteau_divin", "Marteau Divin", "Le Marteau Divin frappe avec la colere des dieux. Chaque coup de son arme sacree ebranle les fondations du monde et purifie les impurs.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.MONSTER_SLAYER_GUARD, PassiveSkill.SEISMIC_STRIKE, PassiveSkill.DIVINE_BULWARK), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD, WeaponMastery.MACE), List.of(), 400, false,
                140, 164, 66, 168, 70, 66);
    }
}