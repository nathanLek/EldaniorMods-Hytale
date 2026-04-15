package com.eldanior.system.skills.skills.passives.Unique.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import java.util.List;

public class FateVision implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.THREAT_AWARENESS) return 3.50f;
        return 1.0f;
    }
    @Override
    public String getRadarMessage(List<RadarTarget> closestTargets, int extraMobs, int extraPlayers) {
        int total = closestTargets.size() + extraMobs + extraPlayers;
        if (total == 0) return null;
        return "<color:gold>" + total + " destin(s) scellé(s)</color>";
    }
    @Override
    public NotificationStyle getRadarStyle() { return NotificationStyle.Warning; }
}