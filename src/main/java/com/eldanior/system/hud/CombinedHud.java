package com.eldanior.system.hud;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.duel.DuelManager;
import com.eldanior.system.party.PartyManager;
import com.eldanior.system.quest.QuestHud;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.util.UUID;

/**
 * HUD combine qui affiche les quetes (gauche), le groupe (droite) et le duel (centre haut).
 * Resout le probleme de un seul CustomUIHud a la fois.
 */
public class CombinedHud extends CustomUIHud {

    private final UUID ownerUUID;
    private final QuestHud questHud;
    private boolean hasParty;
    private boolean hasDuel;

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

        // Duel au centre haut
        hasDuel = DuelManager.isInDuel(ownerUUID);
        if (hasDuel) {
            ui.append("Duel/DuelHud.ui");
            renderDuel(ui);
        }
    }

    @Override
    public void update(boolean firstUpdate, UICommandBuilder ui) {
        questHud.renderPublic(ui);

        // === Groupe ===
        boolean nowHasParty = PartyManager.hasParty(ownerUUID);
        if (nowHasParty) {
            if (!hasParty) {
                // Vient de rejoindre un groupe -> append le .ui et afficher
                ui.append("Party/PartyHud.ui");
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

        // === Duel ===
        boolean nowInDuel = DuelManager.isInDuel(ownerUUID);
        if (nowInDuel) {
            if (!hasDuel) {
                // Duel vient de commencer -> append le .ui
                ui.append("Duel/DuelHud.ui");
                hasDuel = true;
            }
            renderDuel(ui);
        } else {
            if (hasDuel) {
                // Duel termine -> cacher le panneau
                ui.set("#DuelPanel.Visible", false);
                hasDuel = false;
            }
        }

        super.update(firstUpdate, ui);
    }

    private void renderParty(UICommandBuilder ui) {
        // Deleguer le rendu du party au PartyHud existant (methode statique ou inline)
        com.eldanior.system.party.PartyHud.renderPartyStatic(ui, ownerUUID);
    }

    private void renderDuel(UICommandBuilder ui) {
        DuelManager.ActiveDuel duel = DuelManager.getDuel(ownerUUID);
        if (duel == null) {
            ui.set("#DuelPanel.Visible", false);
            return;
        }

        ui.set("#DuelPanel.Visible", true);

        // Nom de l'adversaire
        UUID opponentUUID = duel.getOpponent(ownerUUID);
        PlayerRef opponentRef = Universe.get().getPlayer(opponentUUID);
        String opponentName = opponentRef != null ? opponentRef.getUsername() : "?";
        ui.set("#DuelOpponentName.Text", "VS " + opponentName);

        // Temps ecoule depuis le debut du duel
        long elapsed = System.currentTimeMillis() - duel.getStartTime();
        long seconds = elapsed / 1000;
        long minutes = seconds / 60;
        long secs = seconds % 60;
        ui.set("#DuelTimer.Text", minutes + ":" + String.format("%02d", secs));

        // HP de l'adversaire (ratio 0.0 - 1.0)
        float opponentHP = opponentRef != null ? DuelManager.getPlayerHPRatio(opponentRef) : 0f;
        ui.set("#DuelOpponentHP.Value", opponentHP);
    }
}
