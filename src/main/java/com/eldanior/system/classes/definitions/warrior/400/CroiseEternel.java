package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class CroiseEternel extends ClassModel {
    public CroiseEternel() {
        super("croise_eternel", "Croise Eternel", "Le Croise Eternel mene une guerre sainte qui transcende le temps. Sa devotion sans fin lui confere un corps d'acier et une vie qui jamais ne s'eteint.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.DIAMOND_BODY, PassiveSkill.DRAGON_BLOOD, PassiveSkill.MONSTER_SLAYER_GUARD), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD, WeaponMastery.MACE), List.of(), 400, false,
                136, 170, 68, 170, 68, 68);
    }
}