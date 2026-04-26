// 1. Le Sang-Dragon (Unique - Hybride/Bruiser)
package com.eldanior.system.classes.definitions.warrior;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SangDragon extends ClassModel {
    public SangDragon() {
        super("sang_dragon", "Sang-Dragon", "Un guerrier dont les veines brulent d'une magie draconique antique. Choisi par le Dragon Ancestral.",
                Rarity.DIVINE, ClassType.WARRIOR, List.of(PassiveSkill.GENESIS_EDGE, PassiveSkill.GOD_SLAYER_SWIFTNESS, PassiveSkill.GENESIS_STRIKE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of("demi_dragon"), 400, false,
                440, 320, 440, 300, 180, 440);
    }
}