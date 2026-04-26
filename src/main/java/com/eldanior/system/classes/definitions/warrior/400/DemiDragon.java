package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DemiDragon extends ClassModel {
    public DemiDragon() {
        super("demi_dragon", "Demi-Dragon", "Le Demi-Dragon a fusionne avec l'essence draconique pour devenir une force de la nature. Sa vitesse divine et sa frappe de genese reduisent les mondes en cendres.",
                Rarity.DIVINE, ClassType.WARRIOR, List.of(PassiveSkill.GENESIS_EDGE, PassiveSkill.GOD_SLAYER_SWIFTNESS, PassiveSkill.GENESIS_STRIKE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                748, 544, 748, 510, 306, 748);
    }
}