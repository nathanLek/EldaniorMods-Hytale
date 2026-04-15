package com.eldanior.system.skills.skills.passives.Epique.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import java.util.List;

public class WarProphecy implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.THREAT_AWARENESS) return 2.80f;
        return 1.0f;
    }
    @Override
    public String getRadarMessage(List<RadarTarget> closestTargets, int extraMobs, int extraPlayers) {
        int total = closestTargets.size() + extraMobs + extraPlayers;
        if (total == 0) return null;
        return "<color:red>" + total + " menace(s) - Prophétie active</color>";
    }
    @Override
    public NotificationStyle getRadarStyle() { return NotificationStyle.Warning; }
}