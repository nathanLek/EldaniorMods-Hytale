package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Forteresse extends ClassModel {
    public Forteresse() {
        super("forteresse", "Forteresse", "La Forteresse est un bastion vivant impenetrable. Derriere ses defenses colossales, ses allies sont en securite absolue contre toute menace.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.UNBREAKABLE, PassiveSkill.LIVING_FORTRESS, PassiveSkill.BURSTING_LIFE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                98, 236, 17, 240, 36, 48);
    }
}