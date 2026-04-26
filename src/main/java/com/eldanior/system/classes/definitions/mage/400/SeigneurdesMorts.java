package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SeigneurdesMorts extends ClassModel {
    public SeigneurdesMorts() {
        super("seigneur_des_morts", "Seigneur des Morts", "Le seigneur qui commande aux morts. Son armee est infinie.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.SPIRITUAL_SIPHON, PassiveSkill.EXPANDED_MIND, PassiveSkill.HYDRA_BLOOD), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                6, 12, 38, 8, 6, 12);
    }
}
