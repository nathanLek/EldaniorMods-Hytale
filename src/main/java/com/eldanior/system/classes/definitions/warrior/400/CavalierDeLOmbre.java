package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class CavalierDeLOmbre extends ClassModel {
    public CavalierDeLOmbre() {
        super("cavalier_de_l_ombre", "Cavalier de l'Ombre", "Le Cavalier de l'Ombre chevauche entre les dimensions, surgissant de nulle part pour frapper avec une brutalite terrifiante avant de disparaitre.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.BLOOD_HUNT, PassiveSkill.VOID_STEP, PassiveSkill.CRUSHING_PRESSURE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                196, 98, 32, 82, 134, 116);
    }
}