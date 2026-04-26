package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ColosseDeFer extends ClassModel {
    public ColosseDeFer() {
        super("colosse_de_fer", "Colosse de Fer", "Le Colosse de Fer allie une force brute immense a une resistance surhumaine. Tel un golem d'acier, il avance inexorablement vers ses ennemis terrifies.",
                Rarity.RARE, ClassType.WARRIOR, List.of(PassiveSkill.DEEP_SLASH, PassiveSkill.FORTIFIED_SKIN, PassiveSkill.TIRELESS_BREATH), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                48, 36, 3, 14, 12, 7);
    }
}