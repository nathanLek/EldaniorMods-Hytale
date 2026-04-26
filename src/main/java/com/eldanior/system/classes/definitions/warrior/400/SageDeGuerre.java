package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SageDeGuerre extends ClassModel {
    public SageDeGuerre() {
        super("sage_de_guerre", "Sage de Guerre", "Le Sage de Guerre unit la sagesse ancienne a la puissance brute. Son esprit brillant et ses runes de protection en font un guerrier-mage sans egal.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.GENIUS_MIND, PassiveSkill.MANA_FORTRESS, PassiveSkill.MYTHRIL_FORTIFICATION), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                134, 264, 202, 264, 36, 34);
    }
}
