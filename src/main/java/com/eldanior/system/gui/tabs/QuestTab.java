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
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestTab {

    public static final int MAX_QUEST_SLOTS = 15;

    // Etat de navigation par joueur : null = ecran categories, sinon = id de categorie
    private static final Map<UUID, String> viewingCategory = new ConcurrentHashMap<>();

    // Cache d'affichage par joueur (slot index -> questId ou bountyUUID)
    private static final Map<UUID, List<String>> playerDisplayCaches = new ConcurrentHashMap<>();

    // ==================== NAVIGATION ====================

    public static void selectCategory(UUID uuid, String category) {
        if (category != null) {
            viewingCategory.put(uuid, category);
        } else {
            viewingCategory.remove(uuid);
        }
    }

    public static void goBack(UUID uuid) {
        viewingCategory.remove(uuid);
    }

    // ==================== POPULATE ====================

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID myUUID = getPlayerUUID(ref, store);
        if (myUUID == null) return;

        String category = viewingCategory.get(myUUID);

        if (category == null) {
            renderCategoryScreen(ui, ref, store, myUUID);
        } else {
            renderListScreen(ui, ref, store, myUUID, category);
        }
    }

    // ==================== ECRAN 1 : CATEGORIES ====================

    private static void renderCategoryScreen(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store, UUID myUUID) {
        ui.set("#QuestCategoryPanel.Visible", true);
        ui.set("#QuestListPanel.Visible", false);

        // Cacher tous les slots
        for (int i = 0; i < MAX_QUEST_SLOTS; i++) {
            ui.set("#QSlot" + i + ".Visible", false);
        }

        boolean isPK = false;
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (data != null) isPK = data.isPK();

        // Compteurs
        int dailyCount = QuestManager.countByCategory(myUUID, QuestCategory.JOURNALIERE, isPK);
        int sideCount = QuestManager.countByCategory(myUUID, QuestCategory.SECONDAIRE, isPK);
        int mainCount = QuestManager.countByCategory(myUUID, QuestCategory.PRINCIPAL, isPK);
        int inProgressCount = QuestManager.getInProgressQuests(myUUID).size()
                + com.eldanior.system.boards.GuildBoard.missionsOf(myUUID).size();   // missions prises au tableau de guilde
        int claimableCount = QuestManager.getClaimableQuests(myUUID).size();

        // Bandeaux
        ui.set("#QBannerEnCoursCount.Text", String.valueOf(inProgressCount));

        // Bandeau a reclamer (TextButton direct)
        if (claimableCount > 0) {
            ui.set("#QBannerAClaim.Text", "A RECLAMER (" + claimableCount + ")");
            ui.set("#QBannerAClaim.Background", "#1c1708");
        } else {
            ui.set("#QBannerAClaim.Text", "A RECLAMER (0)");
            ui.set("#QBannerAClaim.Background", "#16130c");
        }

        // Blasons — compteurs
        ui.set("#QBlasonDailyCount.Text", dailyCount + " quetes");

        // Secondaires et Principales : afficher "Verrouillee" si aucune quete disponible
        if (sideCount > 0) {
            ui.set("#QBlasonSideCount.Text", sideCount + " quetes");
            ui.set("#QBlasonSide.Visible", true);
        } else {
            ui.set("#QBlasonSideCount.Text", "Verrouillee");
            ui.set("#QBlasonSideCount.Style.TextColor", "#4a3c18");
            ui.set("#QBlasonSide.Visible", false);
        }
        if (mainCount > 0) {
            ui.set("#QBlasonMainCount.Text", mainCount + " quetes");
            ui.set("#QBlasonMain.Visible", true);
        } else {
            ui.set("#QBlasonMainCount.Text", "Verrouillee");
            ui.set("#QBlasonMainCount.Style.TextColor", "#4a3c18");
            ui.set("#QBlasonMain.Visible", false);
        }

        playerDisplayCaches.put(myUUID, new ArrayList<>());
    }

    // ==================== ECRAN 2 : LISTING ====================

    private static void renderListScreen(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store, UUID myUUID, String category) {
        ui.set("#QuestCategoryPanel.Visible", false);
        ui.set("#QuestListPanel.Visible", true);

        boolean isPK = false;
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (data != null) isPK = data.isPK();
        Player player = store.getComponent(ref, Player.getComponentType());

        // Header
        String catTitle;
        String catColor;
        switch (category) {
            case "daily" -> { catTitle = "JOURNALIERES"; catColor = "#4CAF50"; }
            case "side" -> { catTitle = "SECONDAIRES"; catColor = "#C8A2C8"; }
            case "main" -> { catTitle = "PRINCIPALES"; catColor = "#FFD700"; }
            case "progress" -> { catTitle = "QUETES EN COURS"; catColor = "#caa15a"; }
            case "done" -> { catTitle = "A RECLAMER"; catColor = "#FFD700"; }
            default -> { catTitle = "QUETES"; catColor = "#FFD700"; }
        }
        ui.set("#QListCatTitle.Text", catTitle);
        ui.set("#QListCatTitle.Style.TextColor", catColor);

        List<String> displayCache = new ArrayList<>();
        int slotIdx = 0;

        if ("progress".equals(category)) {
            // Vue agregee : toutes les quetes en cours
            slotIdx = renderInProgressList(ui, ref, store, myUUID, slotIdx, displayCache, player);
            ui.set("#QListCount.Text", displayCache.size() + " QUETES");
            ui.set("#QListSection.Visible", false);
        } else if ("done".equals(category)) {
            // Quetes terminees a reclamer
            slotIdx = renderClaimableList(ui, ref, store, myUUID, slotIdx, displayCache, player);
            ui.set("#QListCount.Text", displayCache.size() + " QUETES");
            ui.set("#QListSection.Visible", false);
        } else {
            // Listing standard par categorie
            QuestCategory questCat = switch (category) {
                case "daily" -> QuestCategory.JOURNALIERE;
                case "side" -> QuestCategory.SECONDAIRE;
                case "main" -> QuestCategory.PRINCIPAL;
                default -> QuestCategory.JOURNALIERE;
            };
            slotIdx = renderCategoryList(ui, ref, store, myUUID, questCat, isPK, slotIdx, displayCache, player, category);
            ui.set("#QListCount.Text", displayCache.size() + " QUETES");
            ui.set("#QListSection.Visible", false);
        }

        // Cacher les slots restants
        for (int i = slotIdx; i < MAX_QUEST_SLOTS; i++) {
            ui.set("#QSlot" + i + ".Visible", false);
        }

        playerDisplayCaches.put(myUUID, displayCache);
    }

    // ==================== RENDERERS ====================

    private static int renderCategoryList(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store,
                                          UUID myUUID, QuestCategory category, boolean isPK,
                                          int slotIdx, List<String> displayCache, Player player, String viewCategory) {
        List<PlayerQuest> myQuests = QuestManager.getPlayerQuests(myUUID);
        long now = System.currentTimeMillis();

        // Quetes possedees de cette categorie (en cours, pas completees)
        for (PlayerQuest pq : myQuests) {
            if (slotIdx >= MAX_QUEST_SLOTS) break;
            if (pq.isCompleted()) continue;
            QuestModel model = QuestManager.getQuest(pq.getQuestId());
            if (model == null) continue;
            if (model instanceof NpcDialogueQuest nq && nq.isInfoOnly()) continue;
            if (model.getCategory() != category) continue;

            fillQuestSlot(ui, slotIdx, model, pq, true, viewCategory, player);
            displayCache.add(pq.getQuestId());
            slotIdx++;
        }

        // Quetes disponibles de cette categorie
        // Les principales et secondaires ne s'obtiennent que via NPC — ne pas les afficher comme "disponibles"
        if (category == QuestCategory.JOURNALIERE) {
            List<QuestModel> available = QuestManager.getAvailableQuests(myUUID, isPK);
            for (QuestModel quest : available) {
                if (slotIdx >= MAX_QUEST_SLOTS) break;
                if (quest.getCategory() != category) continue;
                if (displayCache.contains(quest.getId())) continue;

                fillQuestSlot(ui, slotIdx, quest, null, false, viewCategory, player);
                displayCache.add(quest.getId());
                slotIdx++;
            }
        }

        // Quetes en cooldown (journalieres uniquement)
        if (category == QuestCategory.JOURNALIERE) {
            for (QuestModel quest : QuestManager.getAllQuests()) {
                if (slotIdx >= MAX_QUEST_SLOTS) break;
                if (!quest.isDaily() || quest.getCooldownMinutes() <= 0) continue;
                if (!QuestManager.getTodaysDailyIds().contains(quest.getId())) continue;
                long cdEnd = QuestManager.getCooldownEnd(myUUID, quest.getId());
                if (cdEnd <= now) continue;
                if (displayCache.contains(quest.getId())) continue;

                fillQuestSlot(ui, slotIdx, quest, null, false, viewCategory, player);
                ui.set("#QSlotBtnAccept" + slotIdx + ".Visible", false);
                long remaining = cdEnd - now;
                long hrs = remaining / 3600000;
                long mins = (remaining % 3600000) / 60000;
                ui.set("#QSlotProg" + slotIdx + ".Text", hrs + "h " + mins + "m");
                ui.set("#QSlotProg" + slotIdx + ".Style.TextColor", "#cc6644");
                displayCache.add(quest.getId());
                slotIdx++;
            }
        }

        return slotIdx;
    }

    private static int renderInProgressList(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store,
                                            UUID myUUID, int slotIdx, List<String> displayCache, Player player) {
        List<PlayerQuest> inProgress = QuestManager.getInProgressQuests(myUUID);
        for (PlayerQuest pq : inProgress) {
            if (slotIdx >= MAX_QUEST_SLOTS) break;
            QuestModel model = QuestManager.getQuest(pq.getQuestId());
            if (model == null) continue;

            fillQuestSlot(ui, slotIdx, model, pq, true, "progress", player);
            // Marquer visuellement la quête active
            if (pq.isActive()) {
                ui.set("#QSlotProg" + slotIdx + ".Text", "ACTIVE");
                ui.set("#QSlotProg" + slotIdx + ".Style.TextColor", "#4CAF50");
            }
            displayCache.add(pq.getQuestId());
            slotIdx++;
        }
        return renderGuildMissions(ui, myUUID, slotIdx, displayCache);
    }

    /** Missions prises au tableau de guilde (boards/GuildBoard) : objectif, avancement, recompenses ; abandon possible ici. */
    private static int renderGuildMissions(UICommandBuilder ui, UUID myUUID, int slotIdx, List<String> displayCache) {
        for (com.eldanior.system.boards.GuildBoard.MissionView m : com.eldanior.system.boards.GuildBoard.missionsOf(myUUID)) {
            if (slotIdx >= MAX_QUEST_SLOTS) break;
            ui.set("#QSlot" + slotIdx + ".Visible", true);
            ui.set("#QSlot" + slotIdx + ".Background", "#0d1610");
            ui.set("#QSlotCat" + slotIdx + ".Text", "MISSION DE GUILDE");
            ui.set("#QSlotCat" + slotIdx + ".Style.TextColor", "#caa15a");
            ui.set("#QSlotDiff" + slotIdx + ".Text", m.done() ? "TERMINEE" : "");
            ui.set("#QSlotDiff" + slotIdx + ".Style.TextColor", "#4CAF50");
            ui.set("#QSlotName" + slotIdx + ".Text", m.title());
            ui.set("#QSlotName" + slotIdx + ".Style.TextColor", "#ffffff");
            ui.set("#QSlotProg" + slotIdx + ".Text", m.done() ? "A RECLAMER AU TABLEAU DE GUILDE" : "");
            ui.set("#QSlotProg" + slotIdx + ".Style.TextColor", "#4CAF50");

            ui.set("#QSlotObjABox" + slotIdx + ".Visible", true);
            ui.set("#QSlotObjA" + slotIdx + ".Text", m.objective());
            ui.set("#QSlotObjA" + slotIdx + ".Style.TextColor", m.done() ? "#4CAF50" : "#c8c8c8");
            ui.set("#QSlotObjAC" + slotIdx + ".Text", m.done() ? "OK" : m.progress() + "/" + m.count());
            ui.set("#QSlotObjAC" + slotIdx + ".Style.TextColor", m.done() ? "#4CAF50" : "#caa15a");
            ui.set("#QSlotObjBBox" + slotIdx + ".Visible", false);
            ui.set("#QSlotObjCBox" + slotIdx + ".Visible", false);

            ui.set("#QSlotRewABox" + slotIdx + ".Visible", true);
            ui.set("#QSlotRewA" + slotIdx + ".Text", "+" + formatNumber(m.gold()) + " Or");
            ui.set("#QSlotRewBBox" + slotIdx + ".Visible", true);
            ui.set("#QSlotRewB" + slotIdx + ".Text", "+" + formatNumber(m.xp()) + " XP");
            ui.set("#QSlotRewCBox" + slotIdx + ".Visible", true);
            ui.set("#QSlotRewC" + slotIdx + ".Text", "+" + formatNumber(m.contribution()) + " contribution");

            ui.set("#QSlotBtnAccept" + slotIdx + ".Visible", false);
            ui.set("#QSlotBtnActivate" + slotIdx + ".Visible", false);
            ui.set("#QSlotBtnAbandon" + slotIdx + ".Visible", !m.done());
            ui.set("#QSlotBtnClaim" + slotIdx + ".Visible", false);

            displayCache.add("guild:" + m.id());
            slotIdx++;
        }
        return slotIdx;
    }

    private static int renderClaimableList(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store,
                                           UUID myUUID, int slotIdx, List<String> displayCache, Player player) {
        List<PlayerQuest> claimable = QuestManager.getClaimableQuests(myUUID);
        for (PlayerQuest pq : claimable) {
            if (slotIdx >= MAX_QUEST_SLOTS) break;
            QuestModel model = QuestManager.getQuest(pq.getQuestId());
            if (model == null) continue;

            fillQuestSlot(ui, slotIdx, model, pq, true, "done", player);
            displayCache.add(pq.getQuestId());
            slotIdx++;
        }
        return slotIdx;
    }

    // ==================== FILL QUEST SLOT ====================

    private static void fillQuestSlot(UICommandBuilder ui, int i, QuestModel model, PlayerQuest pq, boolean owned, String currentViewCategory, Player player) {
        ui.set("#QSlot" + i + ".Visible", true);
        ui.set("#QSlot" + i + ".Background", "#0c1018");
        ui.set("#QSlotName" + i + ".Text", model.getName());
        ui.set("#QSlotName" + i + ".Style.TextColor", "#ffffff");
        ui.set("#QSlotCat" + i + ".Text", model.getCategory().getDisplayName());
        ui.set("#QSlotCat" + i + ".Style.TextColor", "#8899aa");
        ui.set("#QSlotDiff" + i + ".Text", model.getDifficulty().getDisplayName());
        ui.set("#QSlotDiff" + i + ".Style.TextColor", model.getDifficulty().getColor());
        ui.set("#QSlotProg" + i + ".Text", "");

        // Objectifs
        if (model instanceof NpcDialogueQuest nq && nq.getCompletionCondition() != null) {
            fillSlotMultiObjectives(ui, i, nq.getCompletionCondition(), pq);
        } else {
            ui.set("#QSlotObjABox" + i + ".Visible", true);
            ui.set("#QSlotObjA" + i + ".Text", model.getObjectiveText());
            if (pq != null && pq.isCompleted()) {
                ui.set("#QSlotObjAC" + i + ".Text", "OK");
                ui.set("#QSlotObjAC" + i + ".Style.TextColor", "#4CAF50");
                ui.set("#QSlotObjA" + i + ".Style.TextColor", "#4CAF50");
            } else if (pq != null) {
                String prog;
                if ((model.getType() == QuestType.MINAGE || model.getType() == QuestType.RECOLTE)
                        && player != null && model.getTargetId() != null) {
                    int invCount = QuestManager.countItemInInventory(player, model.getTargetId());
                    prog = invCount + "/" + model.getTargetAmount();
                } else {
                    prog = pq.getProgress() + "/" + model.getTargetAmount();
                }
                ui.set("#QSlotObjAC" + i + ".Text", prog);
                ui.set("#QSlotObjAC" + i + ".Style.TextColor", "#ffffff");
                ui.set("#QSlotObjA" + i + ".Style.TextColor", "#ddeeff");
            } else {
                ui.set("#QSlotObjAC" + i + ".Text", "");
                ui.set("#QSlotObjA" + i + ".Style.TextColor", "#ddeeff");
            }
            ui.set("#QSlotObjBBox" + i + ".Visible", false);
            ui.set("#QSlotObjCBox" + i + ".Visible", false);
        }

        // Recompenses
        fillSlotRewards(ui, i, model);

        // Boutons — contexte-dependant
        ui.set("#QSlotBtnAbandon" + i + ".Visible", false);
        ui.set("#QSlotBtnAccept" + i + ".Visible", false);
        ui.set("#QSlotBtnActivate" + i + ".Visible", false);
        ui.set("#QSlotBtnClaim" + i + ".Visible", false);

        if (pq != null && pq.isCompleted()) {
            ui.set("#QSlotBtnClaim" + i + ".Visible", true);
        } else if (owned) {
            // ACTIVER : seulement dans la vue "en cours" ET si la quête n'est pas déjà active
            if (pq != null && !pq.isActive() && "progress".equals(currentViewCategory)) {
                ui.set("#QSlotBtnActivate" + i + ".Visible", true);
            }
            ui.set("#QSlotBtnAbandon" + i + ".Visible", true);
        } else {
            ui.set("#QSlotBtnAccept" + i + ".Visible", true);
            ui.set("#QSlotBtnAccept" + i + ".Text", "ACCEPTER");
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
            ui.set("#QSlotRew" + boxes[rewIdx] + i + ".Text", "+" + formatNumber(model.getRewardXP()) + " XP");
            rewIdx++;
        }
        if (model.getRewardGold() > 0 && rewIdx < 3) {
            ui.set("#QSlotRew" + boxes[rewIdx] + "Box" + i + ".Visible", true);
            ui.set("#QSlotRew" + boxes[rewIdx] + i + ".Text", "+" + formatNumber(model.getRewardGold()) + " Or");
            rewIdx++;
        }
        if (model.getRewardTitleId() != null && rewIdx < 3) {
            ui.set("#QSlotRew" + boxes[rewIdx] + "Box" + i + ".Visible", true);
            ui.set("#QSlotRew" + boxes[rewIdx] + i + ".Text", "Titre: " + model.getRewardTitleId());
            rewIdx++;
        }
        for (int r = rewIdx; r < 3; r++) {
            ui.set("#QSlotRew" + boxes[r] + "Box" + i + ".Visible", false);
        }
    }

    // ==================== HANDLERS ====================

    private static List<String> getCache(UUID uuid) {
        return playerDisplayCaches.getOrDefault(uuid, List.of());
    }

    public static boolean handleAccept(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx = parseIdx(slotIndex);
        UUID uuid = getPlayerUUID(ref, store);
        if (uuid == null) return false;
        List<String> cache = getCache(uuid);
        if (idx < 0 || idx >= cache.size()) return false;

        String entry = cache.get(idx);

        if (entry.startsWith("guild:")) return false;                                    // mission de guilde : au tableau

        boolean ok = QuestManager.acceptQuest(uuid, entry);
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

        // Pour les quetes MINAGE/RECOLTE : verifier et retirer les items
        if (model.getType() == QuestType.MINAGE || model.getType() == QuestType.RECOLTE) {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null || model.getTargetId() == null) return false;
            int count = QuestManager.countItemInInventory(player, model.getTargetId());
            if (count < model.getTargetAmount()) {
                PlayerRef pRefMsg = store.getComponent(ref, PlayerRef.getComponentType());
                if (pRefMsg != null) {
                    pRefMsg.sendMessage(Message.raw(
                            "Il vous manque des items ! " + count + "/" + model.getTargetAmount()
                                    + " " + model.getTargetId().replace("hytale:", "")));
                }
                return false;
            }
            QuestManager.removeItemsFromInventory(player, model.getTargetId(), model.getTargetAmount());
        }

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return false;

        int oldLevel = data.getLevel();
        data.addExperience(model.getRewardXP());
        data.addMoney(model.getRewardGold());
        if (model.getRewardTitleId() != null) data.addTitle(model.getRewardTitleId());

        if (model.getCooldownMinutes() > 0) {
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

        // Mettre a jour les stats
        com.eldanior.system.Leveling.utils.StatCalculator.updatePlayerStats(ref, store, data);

        PlayerRef pRefClaim = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRefClaim != null) {
            com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pRefClaim,
                    "QUETE TERMINEE", model.getName(), true);
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

    public static boolean handleAbandon(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx = parseIdx(slotIndex);
        UUID uuid = getPlayerUUID(ref, store);
        if (uuid == null) return false;
        List<String> cache = getCache(uuid);
        if (idx < 0 || idx >= cache.size()) return false;

        Player player = store.getComponent(ref, Player.getComponentType());
        if (cache.get(idx).startsWith("guild:")) {                                         // mission du tableau de guilde
            com.eldanior.system.boards.GuildBoard.abandonMission(uuid, cache.get(idx).substring(6));
            if (player != null) player.getPlayerRef().sendMessage(Message.raw("Mission de guilde abandonnee."));
            return true;
        }
        QuestManager.abandonQuest(uuid, cache.get(idx));
        saveQuestData(uuid, ref, store);

        if (player != null) player.getPlayerRef().sendMessage(Message.raw("Quete abandonnee."));
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

    // ==================== UTILS ====================

    private static String formatNumber(long number) {
        if (number < 1000) return String.valueOf(number);
        StringBuilder sb = new StringBuilder();
        String str = String.valueOf(number);
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) sb.append(' ');
            sb.append(str.charAt(i));
        }
        return sb.toString();
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
        viewingCategory.remove(uuid);
    }
}
