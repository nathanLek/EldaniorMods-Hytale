package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Warrior extends ClassModel {

    public Warrior() {
        super(
                "warrior",
                "Guerrier",
                "Ne pour le champ de bataille, le Guerrier excelle au corps a corps. Sa force brute et sa resilience en font un adversaire redoutable que peu osent defier.",
                Rarity.COMMON,
                ClassType.WARRIOR,
                List.of(PassiveSkill.INSTINCTIVE_STRIKE, PassiveSkill.IRON_RESOLVE),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of("templier", "epeiste", "fantassin", "brute", "mercenaire", "avant_garde", "ravageur", "bretteur", "lame_mage", "champion", "colosse", "executeur", "gardien_runique", "titan", "heros", "fleau", "sang_dragon", "DivineApotre", "protecteur", "berserker", "duelliste", "paladin", "gladiateur", "samourai", "warlord", "chevalier_noir", "croise"),
                120,
                false,
                10, 10, 4, 4, 6, 2
        );
    }
}
