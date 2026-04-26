package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class LoupDeGuerre extends ClassModel {
    public LoupDeGuerre() {
        super("loup_de_guerre", "Loup de Guerre", "Le Loup de Guerre est un predateur ne pour le champ de bataille. Son instinct de chasseur et sa ferocite en font un adversaire terrifiant.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.LUCKY_STRIKE, PassiveSkill.PREDATORY_STRIKE, PassiveSkill.COMBAT_VIGOR), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                34, 19, 6, 17, 32, 28);
    }
}