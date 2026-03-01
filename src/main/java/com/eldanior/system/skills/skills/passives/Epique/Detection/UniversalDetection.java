package com.eldanior.system.skills.skills.passives.Epique.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;

public class UniversalDetection implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.THREAT_AWARENESS) {
            return 40.00f; // Améliore la déception des menaces proches
        }
        return 1.0f;
    }

    @Override
    public String getRadarMessage(int mobCount, int playerCount, int closestDistance) {
        StringBuilder msg = new StringBuilder("<color:gold>✦ Vision Absolue : ");

        if (mobCount > 0) msg.append(mobCount).append(" Âme(s) ");
        if (playerCount > 0) msg.append("<color:magenta>").append(playerCount).append(" Éveillé(s)</color> ");

        msg.append("| Cible à ").append(closestDistance).append("m ✦</color>");

        return msg.toString();
    }

    @Override
    public NotificationStyle getRadarStyle() {
        return NotificationStyle.Warning;
    }
}
