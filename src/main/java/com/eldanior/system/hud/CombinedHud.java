package com.eldanior.system.hud;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.party.PartyManager;
import com.eldanior.system.quest.QuestHud;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.UUID;

/**
 * HUD combine qui affiche les quetes (gauche) ET le groupe (droite).
 * Resout le probleme de un seul CustomUIHud a la fois.
 */
public class CombinedHud extends CustomUIHud {

    private final UUID ownerUUID;
    private final QuestHud questHud;
    private boolean hasParty;

    public CombinedHud(PlayerRef playerRef, UUID ownerUUID) {
        super(playerRef, "combined_hud");
        this.ownerUUID = ownerUUID;
        this.questHud = new QuestHud(playerRef, ownerUUID);
    }

    public void setCachedData(PlayerLevelData data, Player player) {
        questHud.setCachedData(data, player);
    }

    public QuestHud getQuestHud() { return questHud; }
    public UUID getOwnerUUID() { return ownerUUID; }

    @Override
    protected void build(UICommandBuilder ui) {
        // Quetes a gauche
        ui.append("Quest/QuestHud.ui");
        questHud.renderPublic(ui);

        // Groupe a droite
        hasParty = PartyManager.hasParty(ownerUUID);
        if (hasParty) {
            ui.append("Party/PartyHud.ui");
            renderParty(ui);
        }
    }

    @Override
    public void update(boolean firstUpdate, UICommandBuilder ui) {
        questHud.renderPublic(ui);

        boolean nowHasParty = PartyManager.hasParty(ownerUUID);
        if (nowHasParty) {
            if (!hasParty) {
                // Vient de rejoindre un groupe -> rebuild complet
                hasParty = true;
            }
            renderParty(ui);
        } else {
            if (hasParty) {
                // Vient de quitter le groupe
                ui.set("#PartyPanel.Visible", false);
                hasParty = false;
            }
        }
        super.update(firstUpdate, ui);
    }

    private void renderParty(UICommandBuilder ui) {
        // Deleguer le rendu du party au PartyHud existant (methode statique ou inline)
        com.eldanior.system.party.PartyHud.renderPartyStatic(ui, ownerUUID);
    }
}
