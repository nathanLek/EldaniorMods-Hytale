package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MageVoid extends ClassModel {
    public MageVoid() {
        super("mage_void", "Mage du Vide", "Le Mage du Vide canalise l'energie du neant. Sa magie consume l'existence meme de ses adversaires.",
                Rarity.UNIQUE, ClassType.MAGE,
                List.of(PassiveSkill.VOID_BLADE, PassiveSkill.ARCANE_SUPREMACY, PassiveSkill.DIMENSIONAL_DODGE),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of(), 250, false,
                20, 60, 180, 50, 60, 80);
    }
}
