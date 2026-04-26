package com.eldanior.system.classes.definitions.marchand;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ContrebandierRoyal extends ClassModel {
    public ContrebandierRoyal() {
        super("contrebandier_royal", "Contrebandier Royal", "Le roi des contrebandiers. Ses reseaux s'etendent dans tous les royaumes.",
                Rarity.EPIC, ClassType.MERCHANT, List.of(PassiveSkill.ARTISANAT, PassiveSkill.STORM_STEP, PassiveSkill.CRITICAL_LUCK), List.of(WeaponMastery.ANY), List.of(), 400, false,
                20, 14, 42, 28, 76, 86);
    }
}
