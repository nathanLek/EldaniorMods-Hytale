package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Invaincu extends ClassModel {
    public Invaincu() {
        super("invaincu", "Invaincu", "L'Invaincu n'a jamais connu la defaite. Son corps indestructible et sa volonte d'acier repoussent chaque assaut, transformant l'impossible en victoire.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.LIGHTNING_REFLEXES, PassiveSkill.TITAN_CONSTITUTION, PassiveSkill.INVINCIBLE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                194, 140, 34, 134, 96, 68);
    }
}