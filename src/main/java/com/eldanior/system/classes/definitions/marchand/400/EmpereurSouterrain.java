package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class EmpereurSouterrain extends ClassModel {
    public EmpereurSouterrain() {
        super("empereur_souterrain", "Empereur Souterrain", "Le souverain des profondeurs dont l'empire s'etend sous la surface.",
                Rarity.LEGENDARY, ClassType.MERCHANT, List.of(PassiveSkill.ARTISANAT, PassiveSkill.GOD_SLAYER_SWIFTNESS, PassiveSkill.ABSOLUTE_PRECISION), List.of(WeaponMastery.ANY), List.of(), 400, false,
                340, 272, 510, 408, 1190, 1020);
    }
}
