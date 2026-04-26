package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class EnchanteurRoyal extends ClassModel {
    public EnchanteurRoyal() {
        super("enchanteur_royal", "Enchanteur Royal", "L'enchanteur de la cour royale dont les sorts protegent le royaume.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.MANA_FONT, PassiveSkill.OVERFLOWING_LIFE, PassiveSkill.AWAKENED_MIND), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                4, 20, 34, 16, 6, 6);
    }
}
