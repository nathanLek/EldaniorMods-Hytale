package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class CommandeurdelElite extends ClassModel {
    public CommandeurdelElite() {
        super("commandeur_de_l_elite", "Commandeur de l'Elite", "Le commandeur de l'elite. Ses ordres sont la victoire.",
                Rarity.UNIQUE, ClassType.ARCHER, List.of(PassiveSkill.TITAN_CONSTITUTION, PassiveSkill.DEADLY_PRECISION, PassiveSkill.DIVINE_REFLEXES), List.of(WeaponMastery.BOW, WeaponMastery.SWORD), List.of(), 400, false,
                84, 102, 32, 88, 102, 100);
    }
}
