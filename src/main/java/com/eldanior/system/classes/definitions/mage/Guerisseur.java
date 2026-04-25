package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Guerisseur extends ClassModel {
    public Guerisseur() {
        super("guerisseur", "Guerisseur", "Le Guerisseur canalise la magie curative pour soigner les blessures les plus graves. Sa presence est un don precieux.",
                Rarity.COMMON, ClassType.MAGE,
                List.of(PassiveSkill.NATURAL_RECOVERY, PassiveSkill.MANA_FONT, PassiveSkill.ROBUST_CONSTITUTION),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of(), 120, false,
                2, 14, 20, 8, 2, 4);
    }
}
