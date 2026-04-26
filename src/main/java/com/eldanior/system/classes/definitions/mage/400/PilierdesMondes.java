package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class PilierdesMondes extends ClassModel {
    public PilierdesMondes() {
        super("pilier_des_mondes", "Pilier des Mondes", "Un pilier qui soutient la realite elle-meme. Sans lui, les mondes s'effondrent.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.GENIUS_MIND, PassiveSkill.HEART_OF_ETERNITY, PassiveSkill.MANA_CITADEL), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                54, 132, 344, 132, 64, 108);
    }
}
