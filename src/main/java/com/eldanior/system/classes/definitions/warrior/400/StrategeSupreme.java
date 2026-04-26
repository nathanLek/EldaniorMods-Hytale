package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class StrategeSupreme extends ClassModel {
    public StrategeSupreme() {
        super("stratege_supreme", "Stratege Supreme", "Le Stratege Supreme anticipe chaque mouvement ennemi avec une precision surnaturelle. Son intelligence tactique et sa lame redoutable ne laissent aucune faille.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.CRIMSON_BLADE, PassiveSkill.TITAN_CONSTITUTION, PassiveSkill.COMBAT_INTUITION), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                164, 132, 52, 134, 82, 102);
    }
}