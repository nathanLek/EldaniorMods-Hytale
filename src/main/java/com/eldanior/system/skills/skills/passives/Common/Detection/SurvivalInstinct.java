package com.eldanior.system.skills.skills.passives.Common.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;

import java.util.List;

public class SurvivalInstinct implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.THREAT_AWARENESS) {
            return 1.50f; // Améliore la déception des menaces proches
        }
        return 1.0f;
    }

    @Override
    public String getRadarMessage(List<RadarTarget> closestTargets, int extraMobs, int extraPlayers) {
        int total = closestTargets.size() + extraMobs + extraPlayers;
        return "<color:red>" + total + " présence(s) à proximité</color>";
    }

    @Override
    public NotificationStyle getRadarStyle() {
        return NotificationStyle.Warning;
    }
}