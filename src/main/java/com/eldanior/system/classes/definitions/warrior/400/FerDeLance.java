package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class FerDeLance extends ClassModel {
    public FerDeLance() {
        super("fer_de_lance", "Fer de Lance", "Le Fer de Lance charge en premiere ligne avec une bravoure inegalee. Sa constitution d'acier et son esprit indomptable terrorisent l'ennemi.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.UNYIELDING, PassiveSkill.STEEL_CONSTITUTION, PassiveSkill.REACTIVE_BULWARK), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                52, 128, 16, 112, 34, 17);
    }
}