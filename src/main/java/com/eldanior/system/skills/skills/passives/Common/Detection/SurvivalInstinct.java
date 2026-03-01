package com.eldanior.system.skills.skills.passives.Common.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;

public class SurvivalInstinct implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.THREAT_AWARENESS) {
            return 1.50f; // Améliore la déception des menaces proches
        }
        return 1.0f;
    }

    @Override
    public String getRadarMessage(int mobCount, int playerCount, int closestDistance) {
        return "<color:red>" + mobCount + " présence(s) à proximité</color>";
    }

    @Override
    public NotificationStyle getRadarStyle() {
        return NotificationStyle.Warning;
    }
}