package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Traqueur extends ClassModel {
    public Traqueur() {
        super("traqueur", "Traqueur", "Le Traqueur est un predateur patient. Il suit sa proie pendant des jours avant de porter le coup fatal.",
                Rarity.RARE, ClassType.ASSASSIN,
                List.of(PassiveSkill.MASTER_TRACKER, PassiveSkill.HAWK_EYE, PassiveSkill.RAZOR_SENSES),
                List.of(WeaponMastery.DAGGER, WeaponMastery.BOW),
                List.of("traqueur_implacable", "chasseur_d_ombres", "pisteur_absolu"), 400, false,
                20, 15, 5, 15, 50, 50);
    }
}
