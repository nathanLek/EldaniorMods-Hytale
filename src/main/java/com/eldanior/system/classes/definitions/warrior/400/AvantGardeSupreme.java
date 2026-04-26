package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class AvantGardeSupreme extends ClassModel {
    public AvantGardeSupreme() {
        super("avant_garde_supreme", "Avant-Garde Supreme", "L'Avant-Garde Supreme est le fer de lance ultime de toute armee. Sa resilience legendaire et sa volonte indomptable brisent toute offensive ennemie.",
                Rarity.EPIC, ClassType.WARRIOR, List.of(PassiveSkill.IRON_BODY, PassiveSkill.TITAN_CONSTITUTION, PassiveSkill.UNBREAKABLE), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD), List.of(), 400, false,
                48, 132, 16, 116, 32, 16);
    }
}