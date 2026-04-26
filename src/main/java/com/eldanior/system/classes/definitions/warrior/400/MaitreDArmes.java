package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaitreDArmes extends ClassModel {
    public MaitreDArmes() {
        super("maitre_d_armes", "Maitre d'Armes", "Le Maitre d'Armes connait chaque technique de combat existante. Son savoir martial est si vaste qu'il peut contrer n'importe quel style adverse.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.KEEN_SENSES, PassiveSkill.SWORD_MASTERY, PassiveSkill.PRESSURE_POINT), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                28, 20, 7, 14, 28, 17);
    }
}