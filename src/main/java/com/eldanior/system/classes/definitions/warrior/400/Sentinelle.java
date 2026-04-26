package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Sentinelle extends ClassModel {
    public Sentinelle() {
        super("sentinelle", "Sentinelle", "La Sentinelle veille sans relache sur les siens. Ses reflexes defensifs et sa recuperation naturelle font d'elle un gardien infatigable.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.NATURAL_RECOVERY, PassiveSkill.ADAPTIVE_SHIELD, PassiveSkill.IRON_RESOLVE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                20, 38, 4, 30, 11, 7);
    }
}
