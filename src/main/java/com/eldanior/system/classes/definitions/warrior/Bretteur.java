package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Bretteur extends ClassModel {

    public Bretteur() {
        super(
                "bretteur",
                "Bretteur",
                "Le Bretteur est un virtuose de la lame. Sa vitesse fulgurante et sa precision chirurgicale ne laissent aucune chance.",
                Rarity.RARE,
                ClassType.WARRIOR,
                List.of(PassiveSkill.GALE_STEP, PassiveSkill.THUNDER_REFLEXES, PassiveSkill.RAZOR_SENSES),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(),
                250,
                false,
                40, 30, 10, 30, 70, 40
        );
    }
}