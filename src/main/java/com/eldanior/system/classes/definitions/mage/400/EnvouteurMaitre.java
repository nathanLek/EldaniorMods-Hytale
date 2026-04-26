package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class EnvouteurMaitre extends ClassModel {
    public EnvouteurMaitre() {
        super("envouteur_maitre", "Envouteur Maitre", "Le maitre des envoutements. Ses maledictions sont eternelles.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.SOUL_STEALER, PassiveSkill.CRITICAL_LUCK, PassiveSkill.BRILLIANT_MIND), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                20, 36, 88, 28, 32, 72);
    }
}
