package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;
import com.eldanior.system.quest.dialogue.QuestCondition;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestTab {

    public static final int MAX_QUEST_SLOTS = 15;

    // Cache PAR JOUEUR au lieu d'un cache static global
    private static final Map<UUID, List<String>> playerDisplayCaches = new ConcurrentHashMap<>();

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID myUUID = getPlayerUUID(ref, store);
        if (myUUID == null) return;

        // Determiner si le joueur est PK
        boolean isPK = false;
        PlayerLevelData pkCheck = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (pkCheck != null) isPK = pkCheck.isPK();

        // Quete active
        PlayerQuest active = QuestManager.getActiveQuest(myUUID);
        if (active != null) {
            QuestModel model = QuestManager.getQuest(active.getQuestId());
            if (model != null) {
                ui.set("#QActivePanel.Visible", true);
                ui.set("#QActiveName.Text", model.getName());
                ui.set("#QActiveType.Text", model.getType().getDisplayName() + " - " + model.getDifficulty().getDisplayName());
                ui.set("#QActiveCategory.Text", model.getCategory().getDisplayName());
                ui.set("#QActiveReward.Text", model.getRewardText());

                if (model instanceof NpcDialogueQuest nq && nq.getCompletionCondition() != null) {
                    PlayerLevelData pData = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
                    Player pPlayer = store.getComponent(ref, Player.getComponentType());
                    renderActiveObjectiveBoxes(ui, nq.getCompletionCondition(), pData, pPlayer);
                } else {
                    // Quete simple : 1 seul objectif
                    ui.set("#QActiveObj1Box.Visible", true);
                    ui.set("#QActiveObj1.Text", model.getObjectiveText());
                    String prog = active.isCompleted() ? "OK" : active.getProgress() + "/" + model.getTargetAmount();
                    ui.set("#QActiveObj1Count.Text", prog);
                    ui.set("#QActiveObj1.Style.TextColor", active.isCompleted() ? "#4CAF50" : "#ddeeff");
                    ui.set("#QActiveObj1Count.Style.TextColor", active.isCompleted() ? "#4CAF50" : "#ffffff");
                    ui.set("#QActiveObj2Box.Visible", false);
                    ui.set("#QActiveObj3Box.Visible", false);
                }
            }
        } else {
            ui.set("#QActivePanel.Visible", false);
        }

        // Construire le cache d'affichage pour ce joueur
        List<String> displayCache = new ArrayList<>();
        List<PlayerQuest> myQuests = QuestManager.getPlayerQuests(myUUID);
        long now = System.currentTimeMillis();
        int slotIdx = 0;

        // === SECTION 1 : QUETES EN COURS ===
        int enCoursCount = 0;
        for (PlayerQuest pq : myQuests) {
            if (slotIdx >= MAX_QUEST_SLOTS) break;
            if (pq.isActive() || pq.isCompleted()) continue;

            QuestModel model = QuestManager.getQuest(pq.getQuestId());
            if (model == null) continue;
            if (model instanceof NpcDialogueQuest nq && nq.isInfoOnly()) continue;

            fillQuestSlot(ui, slotIdx, model, pq, true);
            displayCache.add(pq.getQuestId());
            slotIdx++;
            enCoursCount++;
        }
        ui.set("#QSectionEnCours.Visible", enCoursCount > 0);
        ui.set("#QSectionEnCours.Text", "QUETES EN COURS (" + enCoursCount + ")");

        // === SECTION 2 : QUETES JOURNALIERES ===
        int journalCount = 0;

        // Disponibles
        List<QuestModel> available = QuestManager.getAvailableQuests(myUUID, isPK);
        for (QuestModel quest : available) {
            if (slotIdx >= MAX_QUEST_SLOTS) break;
            if (!quest.isDaily()) continue;
            fillQuestSlot(ui, slotIdx, quest, null, false);
            displayCache.add(quest.getId());
            slotIdx++;
            journalCount++;
        }

        // En cooldown (timer)
        for (QuestModel quest : QuestManager.getAllQuests()) {
            if (slotIdx >= MAX_QUEST_SLOTS) break;
            if (!quest.isDaily() || quest.getCooldownMinutes() <= 0) continue;
            if (!QuestManager.getTodaysDailyIds().contains(quest.getId())) continue;
            long cdEnd = QuestManager.getCooldownEnd(myUUID, quest.getId());
            if (cdEnd <= now) continue;
            if (displayCache.contains(quest.getId())) continue;

            fillQuestSlot(ui, slotIdx, quest, null, false);
            ui.set("#QSlotBtnAccept" + slotIdx + ".Visible", false);
            long remaining = cdEnd - now;
            long hrs = remaining / 3600000;
            long mins = (remaining % 3600000) / 60000;
            ui.set("#QSlotProg" + slotIdx + ".Text", hrs + "h " + mins + "m");
            ui.set("#QSlotProg" + slotIdx + ".Style.TextColor", "#cc6644");
            displayCache.add(quest.getId());
            slotIdx++;
            journalCount++;
        }
        ui.set("#QSectionJournalieres.Visible", journalCount > 0);
        ui.set("#QSectionJournalieres.Text", "QUETES JOURNALIERES (" + journalCount + ")");

        // === SECTION 3 : QUETES TERMINEES ===
        int termineeCount = 0;
        for (PlayerQuest pq : myQuests) {
            if (slotIdx >= MAX_QUEST_SLOTS) break;
            if (!pq.isCompleted()) continue;

            QuestModel model = QuestManager.getQuest(pq.getQuestId());
            if (model == null) continue;
            if (model instanceof NpcDialogueQuest nq && nq.isInfoOnly()) continue;

            fillQuestSlot(ui, slotIdx, model, pq, true);
            displayCache.add(pq.getQuestId());
            slotIdx++;
            termineeCount++;
        }
        ui.set("#QSectionTerminees.Visible", termineeCount > 0);
        ui.set("#QSectionTerminees.Text", "QUETES TERMINEES (" + termineeCount + ")");

        // Cacher les slots restants
        for (int i = slotIdx; i < MAX_QUEST_SLOTS; i++) {
            ui.set("#QSlot" + i + ".Visible", false);
        }

        // Sauver le cache pour ce joueur
        playerDisplayCaches.put(myUUID, displayCache);
    }

    private static void fillQuestSlot(UICommandBuilder ui, int i, QuestModel model, PlayerQuest pq, boolean owned) {
        ui.set("#QSlot" + i + ".Visible", true);
        ui.set("#QSlotName" + i + ".Text", model.getName());
        ui.set("#QSlotCat" + i + ".Text", model.getCategory().getDisplayName());
        ui.set("#QSlotDiff" + i + ".Text", model.getDifficulty().getDisplayName());
        ui.set("#QSlotProg" + i + ".Text", "");

        // Objectifs dans des containers individuels
        if (model instanceof NpcDialogueQuest nq && nq.getCompletionCondition() != null) {
            fillSlotMultiObjectives(ui, i, nq.getCompletionCondition(), pq);
        } else {
            // Quete simple : 1 container
            ui.set("#QSlotObjABox" + i + ".Visible", true);
            ui.set("#QSlotObjA" + i + ".Text", model.getObjectiveText());
            if (pq != null && pq.isCompleted()) {
                ui.set("#QSlotObjAC" + i + ".Text", "OK");
                ui.set("#QSlotObjAC" + i + ".Style.TextColor", "#4CAF50");
                ui.set("#QSlotObjA" + i + ".Style.TextColor", "#4CAF50");
            } else if (pq != null) {
                ui.set("#QSlotObjAC" + i + ".Text", pq.getProgress() + "/" + model.getTargetAmount());
                ui.set("#QSlotObjAC" + i + ".Style.TextColor", "#ffffff");
                ui.set("#QSlotObjA" + i + ".Style.TextColor", "#ddeeff");
            } else {
                ui.set("#QSlotObjAC" + i + ".Text", "");
                ui.set("#QSlotObjA" + i + ".Style.TextColor", "#ddeeff");
            }
            ui.set("#QSlotObjBBox" + i + ".Visible", false);
            ui.set("#QSlotObjCBox" + i + ".Visible", false);
        }

        // Recompenses dans des containers individuels
        fillSlotRewards(ui, i, model);

        // Reset boutons
        ui.set("#QSlotBtnAbandon" + i + ".Visible", false);
        ui.set("#QSlotBtnAccept" + i + ".Visible", false);
        ui.set("#QSlotBtnActivate" + i + ".Visible", false);
        ui.set("#QSlotBtnClaim" + i + ".Visible", false);

        if (pq != null && pq.isCompleted()) {
            ui.set("#QSlotBtnClaim" + i + ".Visible", true);
        } else if (owned) {
            ui.set("#QSlotBtnActivate" + i + ".Visible", true);
            ui.set("#QSlotBtnAbandon" + i + ".Visible", true);
        } else {
            ui.set("#QSlotBtnAccept" + i + ".Visible", true);
        }
    }

    private static void fillSlotMultiObjectives(UICommandBuilder ui, int slot, QuestCondition cond, PlayerQuest pq) {
        String[] boxes = {"A", "B", "C"};
        List<String[]> objectives = new ArrayList<>();
        int num = 1;

        if (cond.getRequiredLevel() > 0) {
            objectives.add(new String[]{num + ". Lvl " + cond.getRequiredLevel(), "?", "0"});
            num++;
        }
        if (cond.getRequiredQuestCompleted() != null) {
            objectives.add(new String[]{num + ". Indice", "0/1", "0"});
            num++;
        }
        if (cond.getRequiredItemId() != null) {
            String name = cond.getRequiredItemId().replaceAll("([a-z])([A-Z])", "$1 $2").replace("_", " ");
            if (name.contains(":")) name = name.substring(name.indexOf(':') + 1);
            objectives.add(new String[]{num + ". " + name, "0/1", "0"});
        }

        for (int j = 0; j < 3; j++) {
            String id = boxes[j];
            if (j < objectives.size()) {
                ui.set("#QSlotObj" + id + "Box" + slot + ".Visible", true);
                ui.set("#QSlotObj" + id + slot + ".Text", objectives.get(j)[0]);
                ui.set("#QSlotObj" + id + "C" + slot + ".Text", objectives.get(j)[1]);
                ui.set("#QSlotObj" + id + slot + ".Style.TextColor", "#ddeeff");
                ui.set("#QSlotObj" + id + "C" + slot + ".Style.TextColor", "#ffffff");
            } else {
                ui.set("#QSlotObj" + id + "Box" + slot + ".Visible", false);
            }
        }
    }

    private static void fillSlotRewards(UICommandBuilder ui, int i, QuestModel model) {
        String[] boxes = {"A", "B", "C"};
        int rewIdx = 0;

        if (model.getRewardXP() > 0 && rewIdx < 3) {
            ui.set("#QSlotRew" + boxes[rewIdx] + "Box" + i + ".Visible", true);
            ui.set("#QSlotRew" + boxes[rewIdx] + i + ".Text", "+" + model.getRewardXP() + " XP");
            rewIdx++;
        }
        if (model.getRewardGold() > 0 && rewIdx < 3) {
            ui.set("#QSlotRew" + boxes[rewIdx] + "Box" + i + ".Visible", true);
            ui.set("#QSlotRew" + boxes[rewIdx] + i + ".Text", "+" + model.getRewardGold() + " Or");
            rewIdx++;
        }
        if (model.getRewardTitleId() != null && rewIdx < 3) {
            ui.set("#QSlotRew" + boxes[rewIdx] + "Box" + i + ".Visible", true);
            ui.set("#QSlotRew" + boxes[rewIdx] + i + ".Text", "Titre");
            rewIdx++;
        }
        for (int r = rewIdx; r < 3; r++) {
            ui.set("#QSlotRew" + boxes[r] + "Box" + i + ".Visible", false);
        }
    }

    private static List<String> getCache(UUID uuid) {
        return playerDisplayCaches.getOrDefault(uuid, List.of());
    }

    public static boolean handleAccept(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx = parseIdx(slotIndex);
        UUID uuid = getPlayerUUID(ref, store);
        if (uuid == null) return false;
        List<String> cache = getCache(uuid);
        if (idx < 0 || idx >= cache.size()) return false;

        boolean ok = QuestManager.acceptQuest(uuid, cache.get(idx));
        if (ok) saveQuestData(uuid, ref, store);
        return ok;
    }

    public static boolean handleActivate(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx = parseIdx(slotIndex);
        UUID uuid = getPlayerUUID(ref, store);
        if (uuid == null) return false;
        List<String> cache = getCache(uuid);
        if (idx < 0 || idx >= cache.size()) return false;

        boolean ok = QuestManager.activateQuest(uuid, cache.get(idx));
        if (ok) saveQuestData(uuid, ref, store);
        return ok;
    }

    public static boolean handleClaim(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx = parseIdx(slotIndex);
        UUID uuid = getPlayerUUID(ref, store);
        if (uuid == null) return false;
        List<String> cache = getCache(uuid);
        if (idx < 0 || idx >= cache.size()) return false;

        String questId = cache.get(idx);
        QuestModel model = QuestManager.getQuest(questId);
        if (model == null) return false;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return false;

        int oldLevel = data.getLevel();
        data.addExperience(model.getRewardXP());
        data.addMoney(model.getRewardGold());
        if (model.getRewardTitleId() != null) data.addTitle(model.getRewardTitleId());

        if (model.isDaily() && model.getCooldownMinutes() > 0) {
            QuestManager.setCooldown(uuid, questId, model.getCooldownMinutes());
        }

        // Level up ?
        if (data.getLevel() > oldLevel) {
            int gained = (data.getLevel() - oldLevel) * 3;
            data.setAttributePoints(data.getAttributePoints() + gained);
        }

        QuestManager.getPlayerQuests(uuid).removeIf(pq -> pq.getQuestId().equals(questId) && pq.isCompleted());

        data.setQuestData(QuestManager.serializePlayerQuests(uuid));
        data.setCooldownData(QuestManager.serializeCooldowns(uuid));
        store.putComponent(ref, type, data);

        // Mettre à jour les stats
        com.eldanior.system.Leveling.utils.StatCalculator.updatePlayerStats(ref, store, data);

        PlayerRef pRefClaim = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRefClaim != null) {
            // Grand titre de completion
            com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pRefClaim,
                    "QUETE TERMINEE", model.getName(), true);
            // Notification des recompenses
            com.eldanior.system.Leveling.utils.NotificationHelper.sendSuccess(pRefClaim,
                    "<color:green>+" + model.getRewardXP() + " XP</color> <color:gold>+" + model.getRewardGold() + " Or</color>");
            if (model.getRewardTitleId() != null) {
                com.eldanior.system.Leveling.utils.NotificationHelper.sendSuccess(pRefClaim,
                        "<color:yellow>Titre debloque : " + model.getRewardTitleId() + "</color>");
            }
            if (data.getLevel() > oldLevel) {
                com.eldanior.system.Leveling.utils.NotificationHelper.showLevelUpTitle(pRefClaim, data.getLevel());
            }
        }
        return true;
    }

    public static boolean handleAbandonActive(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID uuid = getPlayerUUID(ref, store);
        if (uuid == null) return false;

        PlayerQuest active = QuestManager.getActiveQuest(uuid);
        if (active == null) return false;

        QuestManager.abandonQuest(uuid, active.getQuestId());
        saveQuestData(uuid, ref, store);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) player.getPlayerRef().sendMessage(Message.raw("Quete abandonnee."));
        return true;
    }

    public static boolean handleAbandon(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx = parseIdx(slotIndex);
        UUID uuid = getPlayerUUID(ref, store);
        if (uuid == null) return false;
        List<String> cache = getCache(uuid);
        if (idx < 0 || idx >= cache.size()) return false;

        QuestManager.abandonQuest(uuid, cache.get(idx));
        saveQuestData(uuid, ref, store);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) player.getPlayerRef().sendMessage(Message.raw("Quete abandonnee."));
        return true;
    }

    private static void renderActiveObjectiveBoxes(UICommandBuilder ui, QuestCondition cond,
                                                      PlayerLevelData data, Player player) {
        List<String[]> objectives = new ArrayList<>(); // [label, counter, done]
        int num = 1;

        if (cond.getRequiredLevel() > 0) {
            int current = data != null ? data.getLevel() : 0;
            boolean done = current >= cond.getRequiredLevel();
            objectives.add(new String[]{num + ". Lvl " + cond.getRequiredLevel(), done ? "OK" : current + "/" + cond.getRequiredLevel(), done ? "1" : "0"});
            num++;
        }
        if (cond.getRequiredQuestCompleted() != null) {
            boolean done = data != null && data.getQuestData() != null
                    && data.getQuestData().contains(cond.getRequiredQuestCompleted() + ":")
                    && data.getQuestData().contains(":COMPLETED");
            objectives.add(new String[]{num + ". Indice", done ? "OK" : "0/1", done ? "1" : "0"});
            num++;
        }
        if (cond.getRequiredItemId() != null) {
            boolean done = hasItemInFullInventory(player, cond.getRequiredItemId());
            String name = cond.getRequiredItemId().replaceAll("([a-z])([A-Z])", "$1 $2").replace("_", " ");
            if (name.contains(":")) name = name.substring(name.indexOf(':') + 1);
            objectives.add(new String[]{num + ". " + name, done ? "OK" : "0/1", done ? "1" : "0"});
        }

        for (int i = 1; i <= 3; i++) {
            if (i <= objectives.size()) {
                String[] obj = objectives.get(i - 1);
                boolean done = "1".equals(obj[2]);
                ui.set("#QActiveObj" + i + "Box.Visible", true);
                ui.set("#QActiveObj" + i + ".Text", obj[0]);
                ui.set("#QActiveObj" + i + "Count.Text", obj[1]);
                ui.set("#QActiveObj" + i + ".Style.TextColor", done ? "#4CAF50" : "#ddeeff");
                ui.set("#QActiveObj" + i + "Count.Style.TextColor", done ? "#4CAF50" : "#ffffff");
            } else {
                ui.set("#QActiveObj" + i + "Box.Visible", false);
            }
        }
    }

    /**
     * Parcourt l'inventaire complet (hotbar + storage + backpack) pour trouver un item.
     */
    private static boolean hasItemInFullInventory(Player player, String itemId) {
        if (player == null || itemId == null) return false;
        var inv = player.getInventory();
        for (short i = 0; i < 9; i++) {
            ItemStack item = inv.getHotbar().getItemStack(i);
            if (item != null && !item.isEmpty() && item.getItemId().equalsIgnoreCase(itemId)) return true;
        }
        for (short i = 0; i < 27; i++) {
            ItemStack item = inv.getStorage().getItemStack(i);
            if (item != null && !item.isEmpty() && item.getItemId().equalsIgnoreCase(itemId)) return true;
        }
        for (short i = 0; i < 8; i++) {
            ItemStack item = inv.getBackpack().getItemStack(i);
            if (item != null && !item.isEmpty() && item.getItemId().equalsIgnoreCase(itemId)) return true;
        }
        return false;
    }

    /** Sauvegarde les quetes + cooldowns du joueur dans PlayerLevelData */
    private static void saveQuestData(UUID uuid, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data != null) {
            data.setQuestData(QuestManager.serializePlayerQuests(uuid));
            data.setCooldownData(QuestManager.serializeCooldowns(uuid));
            store.putComponent(ref, type, data);
        }
    }

    private static int parseIdx(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }

    private static UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        try { return UUIDExtractor.getUUID(pRef); }
        catch (Exception e) { return null; }
    }

    /** Nettoyage quand un joueur se deconnecte */
    public static void cleanupPlayer(UUID uuid) {
        playerDisplayCaches.remove(uuid);
    }
}
