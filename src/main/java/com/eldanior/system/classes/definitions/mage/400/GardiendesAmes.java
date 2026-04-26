package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GardiendesAmes extends ClassModel {
    public GardiendesAmes() {
        super("gardien_des_ames", "Gardien des Ames", "Le gardien qui protege et soigne les ames blessees.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.NATURAL_RECOVERY, PassiveSkill.STEEL_CONSTITUTION, PassiveSkill.MANA_STREAM), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                4, 26, 32, 16, 2, 6);
    }
}
