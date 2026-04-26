package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TraqueurImplacable extends ClassModel {
    public TraqueurImplacable() {
        super("traqueur_implacable", "Traqueur Implacable", "Une fois sa cible marquee, rien ne peut l'arreter.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.RELENTLESS_HUNT, PassiveSkill.HAWK_EYE, PassiveSkill.FATAL_PRECISION), List.of(WeaponMastery.DAGGER, WeaponMastery.BOW), List.of(), 400, false,
                34, 26, 8, 26, 86, 86);
    }
}
