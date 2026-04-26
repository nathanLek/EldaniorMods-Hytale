package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Shogun extends ClassModel {
    public Shogun() {
        super("shogun", "Shogun", "Le Shogun commande avec la puissance d'un seigneur de guerre. Ses reflexes foudroyants et sa determination indomptable ecrasent toute opposition.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.THUNDER_REFLEXES, PassiveSkill.GALE_STEP, PassiveSkill.STEEL_RESOLVE), List.of(WeaponMastery.SWORD, WeaponMastery.DAGGER), List.of(), 400, false,
                74, 44, 16, 34, 96, 66);
    }
}