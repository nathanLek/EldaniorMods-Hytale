package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Corsaire extends ClassModel {
    public Corsaire() {
        super("corsaire", "Corsaire", "Le Corsaire est un pirate d'elite. Il combine combat rapproche et ruse pour piller sans merci.",
                Rarity.RARE, ClassType.ASSASSIN,
                List.of(PassiveSkill.CRITICAL_LUCK, PassiveSkill.SHARP_BLADE, PassiveSkill.BATTLE_FRENZY),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of(), 250, false,
                30, 20, 5, 15, 45, 40);
    }
}
