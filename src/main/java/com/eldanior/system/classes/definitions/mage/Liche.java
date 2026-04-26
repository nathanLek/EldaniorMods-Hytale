package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Liche extends ClassModel {
    public Liche() {
        super("liche", "Liche", "La Liche a sacrifie sa mortalite pour une puissance eternelle. Entre vie et mort, elle commande les tenebres.",
                Rarity.EPIC, ClassType.MAGE,
                List.of(PassiveSkill.SPIRIT_DRAIN, PassiveSkill.HYDRA_BLOOD, PassiveSkill.UNBREAKABLE),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("arche_liche", "liche_eternelle", "seigneur_non_mort"), 400, false,
                15, 50, 110, 60, 20, 50);
    }
}
