package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GrandTemplier extends ClassModel {
    public GrandTemplier() {
        super("grand_templier", "Grand Templier", "Le Grand Templier est le chef supreme de l'ordre sacre. Sa volonte d'acier et sa vitalite inepuisable inspirent ses freres d'armes au combat.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.STEEL_RESOLVE, PassiveSkill.BURSTING_LIFE, PassiveSkill.ATHLETICISM), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                85, 85, 7, 85, 100, 100);
    }
}