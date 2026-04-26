package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class AgentDouble extends ClassModel {
    public AgentDouble() {
        super("agent_double", "Agent Double", "Un maitre de la duplicite qui joue tous les camps a la fois.",
                Rarity.EPIC, ClassType.ASSASSIN, List.of(PassiveSkill.SHADOW_DODGE, PassiveSkill.CRITICAL_LUCK, PassiveSkill.KEEN_SENSES), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                24, 32, 28, 16, 68, 92);
    }
}
