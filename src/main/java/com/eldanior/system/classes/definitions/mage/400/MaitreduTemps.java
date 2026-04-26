package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaitreduTemps extends ClassModel {
    public MaitreduTemps() {
        super("maitre_du_temps", "Maitre du Temps", "Le maitre absolu du temps. Il accelere, ralentit ou arrete le temps.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.VOID_STEP, PassiveSkill.MANA_INFINITY, PassiveSkill.DIMENSIONAL_DODGE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                26, 70, 174, 52, 122, 86);
    }
}
