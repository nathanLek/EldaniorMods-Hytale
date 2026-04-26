package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SeigneurduFroid extends ClassModel {
    public SeigneurduFroid() {
        super("seigneur_du_froid", "Seigneur du Froid", "Le seigneur du froid eternel. La ou il marche, l'hiver s'installe.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.MANA_BARRIER, PassiveSkill.STEEL_CONSTITUTION, PassiveSkill.MANA_FONT), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                4, 16, 38, 12, 8, 6);
    }
}
