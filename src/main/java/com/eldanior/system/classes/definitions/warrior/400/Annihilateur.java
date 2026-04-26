package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Annihilateur extends ClassModel {
    public Annihilateur() {
        super("annihilateur", "Annihilateur", "L'Annihilateur ne connait qu'un seul objectif : l'aneantissement total. Sa fureur destructrice et ses coups titanesques effacent toute trace de resistance.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.BATTLE_FRENZY, PassiveSkill.SEISMIC_STRIKE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                155, 64, 7, 64, 48, 28);
    }
}