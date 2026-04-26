package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Ravageur extends ClassModel {

    public Ravageur() {
        super(
                "ravageur",
                "Ravageur",
                "Le Ravageur est une force de destruction pure. Sa puissance d'attaque est inegalee sur le champ de bataille.",
                Rarity.RARE,
                ClassType.WARRIOR,
                List.of(PassiveSkill.SHARP_BLADE, PassiveSkill.RELENTLESS_HUNT, PassiveSkill.BATTLE_FRENZY),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of("destructeur", "fleau_de_guerre", "annihilateur"),
                400,
                false,
                90, 40, 4, 40, 30, 16
        );
    }
}