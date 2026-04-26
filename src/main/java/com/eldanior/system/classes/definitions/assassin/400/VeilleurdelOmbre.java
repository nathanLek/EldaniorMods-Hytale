package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class VeilleurdelOmbre extends ClassModel {
    public VeilleurdelOmbre() {
        super("veilleur_ombre", "Veilleur de l'Ombre", "La sentinelle invisible qui veille depuis les tenebres.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.DARK_VISION, PassiveSkill.LIGHT_REFLEXES, PassiveSkill.PHANTOM_DODGE), List.of(WeaponMastery.DAGGER, WeaponMastery.BOW), List.of(), 400, false,
                6, 10, 6, 8, 30, 18);
    }
}
