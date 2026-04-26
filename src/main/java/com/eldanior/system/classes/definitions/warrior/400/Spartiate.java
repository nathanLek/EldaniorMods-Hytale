package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Spartiate extends ClassModel {
    public Spartiate() {
        super("spartiate", "Spartiate", "Le Spartiate incarne la discipline guerriere poussee a son paroxysme. Ne pour le combat, il ne connait ni peur ni recul face a l'ennemi.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.SHARP_BLADE, PassiveSkill.STEEL_NERVES, PassiveSkill.IRON_RESOLVE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                80, 54, 7, 52, 80, 48);
    }
}