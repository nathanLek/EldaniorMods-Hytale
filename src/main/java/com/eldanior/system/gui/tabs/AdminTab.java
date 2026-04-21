package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.NobilityManager;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.church.ChurchManager;
import com.eldanior.system.titles.church.ChurchRank;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Field;
import java.util.*;

public class AdminTab {

    public static final int MAX_PLAYER_SLOTS = 8;
    private static final List<String> cachedPlayerNames = new ArrayList<>();

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        // Populate online players
        cachedPlayerNames.clear();
        UUID myUUID = getPlayerUUID(ref, store);

        List<PlayerRef> allPlayers = Universe.get().getPlayers();
        for (PlayerRef pRef : allPlayers) {
            if (cachedPlayerNames.size() >= MAX_PLAYER_SLOTS) break;
            cachedPlayerNames.add(pRef.getUsername());
        }

        // Player rows for Leveling
        for (int i = 0; i < MAX_PLAYER_SLOTS; i++) {
            if (i < cachedPlayerNames.size()) {
                ui.set("#AdminPlayer" + i + ".Visible", true);
                ui.set("#AdminPName" + i + ".Text", cachedPlayerNames.get(i));
            } else {
                ui.set("#AdminPlayer" + i + ".Visible", false);
            }
        }

        // Player rows for Nobility
        for (int i = 0; i < MAX_PLAYER_SLOTS; i++) {
            if (i < cachedPlayerNames.size()) {
                ui.set("#NobPlayer" + i + ".Visible", true);
                ui.set("#NobPName" + i + ".Text", cachedPlayerNames.get(i));
            } else {
                ui.set("#NobPlayer" + i + ".Visible", false);
            }
        }

        // Player rows for Church
        for (int i = 0; i < MAX_PLAYER_SLOTS; i++) {
            if (i < cachedPlayerNames.size()) {
                ui.set("#ChurchPlayer" + i + ".Visible", true);
                ui.set("#ChurchPName" + i + ".Text", cachedPlayerNames.get(i));
            } else {
                ui.set("#ChurchPlayer" + i + ".Visible", false);
            }
        }
    }

    // === HANDLERS ===

    public static boolean handleResetLevel(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef targetRef = getTargetRef(slotIndex);
        if (targetRef == null) return false;
        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
            data.setLevel(1);
            data.setExperience(0);
            data.setStrength(1); data.setVitality(1); data.setIntelligence(1);
            data.setEndurance(1); data.setAgility(1); data.setLuck(1);
            data.setAttributePoints(0);
            data.setPlayerClass("Novice"); data.setPlayerClassId("novice");
            data.forgetAllSkills(); data.resetTitles();
            data.setNobilityRank("ROTURIER"); data.setNobleFamilyId(""); data.setStatus(""); data.setDignity(0);
            data.setChurchRank("LAIQUE"); data.setFaith(0);
            data.setGuildId(""); data.setGuildRole("");
            data.setPlayerKills(0); data.setPlayerDeaths(0); data.setKillStreak(0); data.setBestKillStreak(0);
            data.setChestsDiscovered(0);
            targetRef.sendMessage(Message.raw("§cVotre personnage a ete reinitialise (Niveau 1)."));
        });
    }

    public static boolean handleAddXP(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef targetRef = getTargetRef(slotIndex);
        if (targetRef == null) return false;
        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
            int oldLvl = data.getLevel();
            data.addExperience(10000);
            if (data.getLevel() > oldLvl) {
                targetRef.sendMessage(Message.raw("§aLevel Up ! Niveau " + data.getLevel()));
            }
            targetRef.sendMessage(Message.raw("§a+10,000 XP !"));
        });
    }

    public static boolean handleNobilityPromote(String slotIndex, String rank, Ref<EntityStore> ref, Store<EntityStore> store) {
        NobilityRank newRank = NobilityRank.fromString(rank);
        if (newRank == null) return false;

        PlayerRef targetRef = getTargetRef(slotIndex);
        if (targetRef == null) return false;
        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
            data.setNobilityRank(newRank.name());
            data.setDignity(newRank.getBaseDignity());
            NobilityManager.recordKingPromotion(newRank);
            targetRef.sendMessage(Message.raw("§eVous etes maintenant " + newRank.getFormattedName()));
        });
    }

    public static boolean handleChurchPromote(String slotIndex, String rank, Ref<EntityStore> ref, Store<EntityStore> store) {
        ChurchRank newRank = ChurchRank.fromString(rank);
        if (newRank == null) return false;

        PlayerRef targetRef = getTargetRef(slotIndex);
        if (targetRef == null) return false;
        return executeOnTarget(targetRef, (tRef, tStore, data) -> {
            data.setChurchRank(newRank.name());
            data.setFaith(newRank.getBaseFaith());
            ChurchManager.recordPopePromotion(newRank);
            targetRef.sendMessage(Message.raw("§eVous etes maintenant " + newRank.getFormattedName()));
        });
    }

    // === UTILS ===

    private static PlayerRef getTargetRef(String slotIndex) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return null; }
        if (idx < 0 || idx >= cachedPlayerNames.size()) return null;
        String name = cachedPlayerNames.get(idx);
        return Universe.get().getPlayerByUsername(name, com.hypixel.hytale.server.core.NameMatching.EXACT_IGNORE_CASE);
    }

    @FunctionalInterface
    private interface TargetAction {
        void apply(Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData data);
    }

    private static boolean executeOnTarget(PlayerRef targetRef, TargetAction action) {
        try {
            var tEntityRef = targetRef.getReference();
            if (tEntityRef == null) return false;
            var tStore = tEntityRef.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = tStore.getComponent(tEntityRef, type);
            if (data == null) data = new PlayerLevelData();

            action.apply(tEntityRef, tStore, data);

            tStore.putComponent(tEntityRef, type, data);
            StatCalculator.updatePlayerStats(tEntityRef, tStore, data);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        return extractUUID(pRef);
    }

    private static UUID extractUUID(PlayerRef playerRef) {
        if (playerRef == null) return null;
        try {
            Field f = PlayerRef.class.getDeclaredField("uuid");
            f.setAccessible(true);
            return (UUID) f.get(playerRef);
        } catch (Exception e) { return null; }
    }
}
