package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GrandEnchanteur extends ClassModel {
    public GrandEnchanteur() {
        super("grand_enchanteur", "Grand Enchanteur", "Le plus grand enchanteur vivant. Ses enchantements defient la realite.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.MANA_FONT, PassiveSkill.ROBUST_CONSTITUTION, PassiveSkill.EXPANDED_MIND), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                4, 18, 38, 14, 7, 7);
    }
}
