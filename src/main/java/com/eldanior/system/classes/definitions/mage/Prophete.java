package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Prophete extends ClassModel {
    public Prophete() {
        super("prophete", "Prophete", "Le Prophete voit tous les futurs possibles. Sa connaissance ultime du destin en fait un etre quasi omniscient.",
                Rarity.LEGENDARY, ClassType.MAGE,
                List.of(PassiveSkill.ABSOLUTE_PRECISION, PassiveSkill.COSMIC_MIND, PassiveSkill.REALITY_DODGE),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("prophete_absolu", "oracle_divin", "voix_de_l_eternel"), 400, false,
                40, 100, 280, 80, 80, 120);
    }
}
