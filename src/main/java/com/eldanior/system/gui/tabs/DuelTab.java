package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.duel.DuelManager;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;

public class DuelTab {

    public static final int MAX_INVITE_SLOTS = 8;
    public static final int MAX_HISTORY_SLOTS = 10;
    private static final List<String> cachedPlayerNames = new ArrayList<>();

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return;

        UUID myUUID = getPlayerUUID(ref, store);
        boolean inDuel = myUUID != null && DuelManager.isInDuel(myUUID);

        // Stats
        ui.set("#DuelStatTotal.Text", String.valueOf(data.getDuelTotal()));
        ui.set("#DuelStatWins.Text", String.valueOf(data.getDuelWins()));
        ui.set("#DuelStatLosses.Text", String.valueOf(data.getDuelLosses()));
        ui.set("#DuelStatStreak.Text", String.valueOf(data.getDuelStreak()));
        ui.set("#DuelStatRecord.Text", String.valueOf(data.getDuelBestStreak()));
        ui.set("#DuelInProgress.Visible", inDuel);
        ui.set("#DuelInviteSection.Visible", !inDuel);

        // Historique
        List<String> history = data.getDuelHistory();
        for (int i = 0; i < MAX_HISTORY_SLOTS; i++) {
            if (i < history.size()) {
                String[] parts = history.get(i).split("\\|");
                if (parts.length >= 4) {
                    boolean won = "V".equals(parts[0]);
                    ui.set("#DuelHist" + i + ".Visible", true);
                    ui.set("#DuelHistResult" + i + ".Text", won ? "VICTOIRE" : "DEFAITE");
                    ui.set("#DuelHistName" + i + ".Text", parts[1]);
                    ui.set("#DuelHistHP" + i + ".Text", parts[2] + " vs " + parts[3]);
                } else {
                    ui.set("#DuelHist" + i + ".Visible", false);
                }
            } else {
                ui.set("#DuelHist" + i + ".Visible", false);
            }
        }

        // Joueurs en ligne pour defier
        if (!inDuel) {
            cachedPlayerNames.clear();
            for (PlayerRef pRef : Universe.get().getPlayers()) {
                if (cachedPlayerNames.size() >= MAX_INVITE_SLOTS) break;
                UUID pUUID = extractUUID(pRef);
                if (pUUID == null || pUUID.equals(myUUID)) continue;
                if (DuelManager.isInDuel(pUUID)) continue;
                cachedPlayerNames.add(pRef.getUsername());
            }

            for (int i = 0; i < MAX_INVITE_SLOTS; i++) {
                if (i < cachedPlayerNames.size()) {
                    ui.set("#DuelPlayer" + i + ".Visible", true);
                    ui.set("#DuelPName" + i + ".Text", cachedPlayerNames.get(i));
                } else {
                    ui.set("#DuelPlayer" + i + ".Visible", false);
                }
            }
        }
    }

    public static boolean handleChallenge(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        if (idx < 0 || idx >= cachedPlayerNames.size()) return false;

        UUID myUUID = getPlayerUUID(ref, store);
        if (myUUID == null || DuelManager.isInDuel(myUUID)) return false;

        String targetName = cachedPlayerNames.get(idx);
        PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) return false;

        UUID targetUUID = extractUUID(targetRef);
        if (targetUUID == null || DuelManager.isInDuel(targetUUID)) return false;

        DuelManager.sendChallenge(myUUID, targetUUID);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.sendMessage(Message.raw("§6Defi envoye a " + targetName + " !"));
        }
        targetRef.sendMessage(Message.raw("§6§l" + (player != null ? player.getDisplayName() : "?") + " vous defie en duel !"));
        targetRef.sendMessage(Message.raw("§7Tapez §f/es duel accept §7ou §f/es duel decline"));

        return true;
    }

    private static UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        return extractUUID(pRef);
    }

    private static UUID extractUUID(PlayerRef playerRef) {
        if (playerRef == null) return null;
        try { return UUIDExtractor.getUUID(playerRef); } catch (Exception e) { return null; }
    }
}
