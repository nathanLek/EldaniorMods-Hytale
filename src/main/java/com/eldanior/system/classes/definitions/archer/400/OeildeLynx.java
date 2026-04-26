package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class OeildeLynx extends ClassModel {
    public OeildeLynx() {
        super("oeil_de_lynx", "Oeil de Lynx", "Son regard percant ne manque aucun detail. Chaque tir est parfait.",
                Rarity.EPIC, ClassType.ARCHER, List.of(PassiveSkill.EAGLE_EYE, PassiveSkill.DEADLY_PRECISION, PassiveSkill.LIGHTNING_REFLEXES), List.of(WeaponMastery.BOW), List.of(), 400, false,
                32, 24, 16, 16, 84, 88);
    }
}
