package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GrandMaitredeGuilde extends ClassModel {
    public GrandMaitredeGuilde() {
        super("grand_maitre_de_guilde", "Grand Maitre de Guilde", "Le grand maitre qui dirige la plus puissante guilde du monde.",
                Rarity.UNIQUE, ClassType.MERCHANT, List.of(PassiveSkill.ARTISANAT, PassiveSkill.TITAN_CONSTITUTION, PassiveSkill.OVERFLOWING_LIFE), List.of(WeaponMastery.ANY), List.of(), 400, false,
                48, 18, 52, 42, 14, 52);
    }
}
