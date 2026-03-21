package com.eldanior.system.skills.skills.passives.Epique.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;

import java.util.List;

public class UniversalDetection implements IPassiveCombatSkill {

    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.THREAT_AWARENESS) {
            return 4.00f;
        }
        return 1.0f;
    }

    @Override
    public String getRadarMessage(List<RadarTarget> closestTargets, int extraMobs, int extraPlayers) {
        // Le \n permet de sauter une ligne
        StringBuilder msg = new StringBuilder("<color:gold>Detection Universel :</color>\n");

        // On affiche le détail des 3 plus proches
        for (RadarTarget target : closestTargets) {
            msg.append("<color:white>- ").append(target.name()).append(" - ").append(target.distance()).append("m</color>\n");
        }

        // On affiche les totaux supplémentaires s'il y en a
        if (extraMobs > 0) {
            msg.append("<color:green>+ ").append(extraMobs).append(" présence(s) sont à proximité</color>\n");
        }
        if (extraPlayers > 0) {
            msg.append("<color:red>+ ").append(extraPlayers).append(" joueur(s) sont à proximité");
        }

        msg.append("</color>");
        return msg.toString();
    }

    @Override
    public NotificationStyle getRadarStyle() {
        return NotificationStyle.Warning;
    }
}