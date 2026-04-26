package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ChevalierMystique extends ClassModel {
    public ChevalierMystique() {
        super("chevalier_mystique", "Chevalier Mystique", "Le Chevalier Mystique est un guerrier illumine par les arcanes. Sa maitrise duale de l'epee et de la magie en fait un adversaire impredictible.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.EXPANDED_MIND, PassiveSkill.SPELLBLADE, PassiveSkill.MANA_FORTRESS), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                64, 66, 98, 52, 48, 34);
    }
}