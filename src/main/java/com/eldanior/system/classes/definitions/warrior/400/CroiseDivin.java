package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class CroiseDivin extends ClassModel {
    public CroiseDivin() {
        super("croise_divin", "Croise Divin", "Le Croise Divin porte la volonte des dieux sur le champ de bataille. Son epee sacree et sa vie debordante ecrasent les forces du mal.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.OVERFLOWING_LIFE, PassiveSkill.IRON_FORTIFICATION, PassiveSkill.ENRICHED_BLOOD), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD, WeaponMastery.MACE), List.of(), 400, false,
                68, 82, 32, 66, 32, 52);
    }
}