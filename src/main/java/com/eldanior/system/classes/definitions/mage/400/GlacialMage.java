package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GlacialMage extends ClassModel {
    public GlacialMage() {
        super("glacial_mage", "Glacial Mage", "Un mage de glace dont les sorts gelent jusqu'a l'ame.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.AWAKENED_MIND, PassiveSkill.EXPANDED_MIND, PassiveSkill.FORTIFIED_SKIN), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                2, 12, 42, 8, 12, 8);
    }
}
