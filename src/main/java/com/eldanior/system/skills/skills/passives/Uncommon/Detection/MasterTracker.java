package com.eldanior.system.skills.skills.passives.Uncommon.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class MasterTracker implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.TRACKING_EVIDENCE) {
            return 1.50f; // +50% visibilité des traces
        }
        return 1.0f;
    }
}