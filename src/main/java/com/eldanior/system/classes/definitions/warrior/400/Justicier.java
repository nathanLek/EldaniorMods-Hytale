package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Justicier extends ClassModel {
    public Justicier() {
        super("justicier", "Justicier", "Le Justicier traque le mal sous toutes ses formes. Anime par un sens de la justice implacable, sa lame ne connait aucune pitie pour les malfaiteurs.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.VITAL_RECOVERY, PassiveSkill.STEEL_RESOLVE, PassiveSkill.RELENTLESS_HUNT), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD, WeaponMastery.MACE), List.of(), 400, false,
                70, 80, 34, 64, 36, 50);
    }
}