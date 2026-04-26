package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DemiNeant extends ClassModel {
    public DemiNeant() {
        super("demi_neant", "Demi-Neant", "A mi-chemin entre l'existence et le neant. Un etre transcendant les limites de la realite.",
                Rarity.DIVINE, ClassType.ASSASSIN, List.of(PassiveSkill.CREATOR_EDGE, PassiveSkill.CREATOR_SWIFTNESS, PassiveSkill.CREATOR_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                306, 204, 68, 170, 680, 595);
    }
}
