package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DemolisseurFurtif extends ClassModel {
    public DemolisseurFurtif() {
        super("demolisseur_furtif", "Demolisseur Furtif", "Il seme le chaos sans etre vu. Ses sabotages sont des oeuvres d'art.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.SHADOW_DODGE, PassiveSkill.KEEN_SENSES, PassiveSkill.SHARP_BLADE), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                12, 12, 8, 10, 26, 18);
    }
}
