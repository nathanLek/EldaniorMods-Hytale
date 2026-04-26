package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class VirtuoseDuSabre extends ClassModel {
    public VirtuoseDuSabre() {
        super("virtuose_du_sabre", "Virtuose du Sabre", "Le Virtuose du Sabre a atteint la perfection dans l'art de l'escrime. Chaque mouvement est calcule avec une precision mathematique et une grace mortelle.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.GALE_STEP, PassiveSkill.THUNDER_REFLEXES, PassiveSkill.DEADLY_PRECISION), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                66, 51, 16, 48, 116, 66);
    }
}