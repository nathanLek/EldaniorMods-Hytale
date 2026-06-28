package com.eldanior.system.quest;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;
import com.eldanior.system.quest.dialogue.QuestCondition;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestHud extends CustomUIHud {

    private final UUID ownerUUID;
    private PlayerLevelData cachedData;
    private Player cachedPlayer;

    public QuestHud(PlayerRef playerRef, UUID ownerUUID) {
        super(playerRef, "quest_hud");
        this.ownerUUID = ownerUUID;
    }

    public void setCachedData(PlayerLevelData data, Player player) {
        this.cachedData = data;
        this.cachedPlayer = player;
    }

    @Override
    protected void build(UICommandBuilder ui) {
        ui.append("Quest/QuestHud.ui");
        render(ui);
    }

    @Override
    public void update(boolean firstUpdate, UICommandBuilder ui) {
        render(ui);
        super.update(firstUpdate, ui);
    }

    /** Accessible depuis CombinedHud */
    public void renderPublic(UICommandBuilder ui) { render(ui); }

    private void render(UICommandBuilder ui) {
        // === QUETE PRINCIPALE ===
        PlayerQuest mainQuest = findMainQuest();
        if (mainQuest != null) {
            QuestModel mainModel = QuestManager.getQuest(mainQuest.getQuestId());
            if (mainModel != null) {
                ui.set("#QHudMainPanel.Visible", true);
                ui.set("#QHudMainName.Text", mainModel.getName());
                ui.set("#QHudMainDiff.Text", mainModel.getDifficulty().getDisplayName());

                if (mainModel instanceof NpcDialogueQuest nq && nq.getCompletionCondition() != null) {
                    renderObjectiveBoxes(ui, "QHudMain", nq.getCompletionCondition());
                } else {
                    // Quete simple : afficher objectif + progression dans box 1
                    String objText = mainModel.getObjectiveText();
                    String prog;
                    if (mainQuest.isCompleted()) {
                        prog = "OK";
                    } else if ((mainModel.getType() == QuestType.MINAGE || mainModel.getType() == QuestType.RECOLTE)
                            && cachedPlayer != null && mainModel.getTargetId() != null) {
                        int invCount = QuestManager.countItemInInventory(cachedPlayer, mainModel.getTargetId());
                        prog = invCount + "/" + mainModel.getTargetAmount();
                    } else {
                        prog = mainQuest.getProgress() + "/" + mainModel.getTargetAmount();
                    }
                    boolean done = mainQuest.isCompleted() || "OK".equals(prog);
                    ui.set("#QHudMainObj1Box.Visible", true);
                    ui.set("#QHudMainObj1.Text", objText);
                    ui.set("#QHudMainObj1Count.Text", prog);
                    ui.set("#QHudMainObj1.Style.TextColor", done ? "#4CAF50" : "#ddeeff");
                    ui.set("#QHudMainObj1Count.Style.TextColor", done ? "#4CAF50" : "#ffffff");
                    ui.set("#QHudMainObj2Box.Visible", false);
                    ui.set("#QHudMainObj3Box.Visible", false);
                }
                ui.set("#QHudMainProg.Text", "");
            } else {
                ui.set("#QHudMainPanel.Visible", false);
            }
        } else {
            ui.set("#QHudMainPanel.Visible", false);
        }

        // === QUETE ACTIVE (journaliere/secondaire) ===
        PlayerQuest active = QuestManager.getActiveQuest(ownerUUID);
        if (active != null && !active.getQuestId().equals(mainQuest != null ? mainQuest.getQuestId() : "")) {
            QuestModel model = QuestManager.getQuest(active.getQuestId());
            // Skip info-only quests (talk_tavernier etc)
            if (model instanceof NpcDialogueQuest infoNq && infoNq.isInfoOnly()) model = null;
            if (model != null) {
                ui.set("#QuestHudPanel.Visible", true);
                ui.set("#QHudName.Text", model.getName());
                ui.set("#QHudDiff.Text", model.getDifficulty().getDisplayName());

                if (model instanceof NpcDialogueQuest nq && nq.getCompletionCondition() != null) {
                    renderObjectiveBoxes(ui, "QHud", nq.getCompletionCondition());
                } else {
                    // Quete simple : afficher objectif + progression dans box 1
                    String objText = model.getObjectiveText();
                    String prog;
                    if (active.isCompleted()) {
                        prog = "OK";
                    } else if ((model.getType() == QuestType.MINAGE || model.getType() == QuestType.RECOLTE)
                            && cachedPlayer != null && model.getTargetId() != null) {
                        // Progression basee sur l'inventaire pour MINAGE/RECOLTE
                        int invCount = QuestManager.countItemInInventory(cachedPlayer, model.getTargetId());
                        prog = invCount + "/" + model.getTargetAmount();
                    } else {
                        prog = active.getProgress() + "/" + model.getTargetAmount();
                    }
                    boolean done = active.isCompleted() || "OK".equals(prog);
                    ui.set("#QHudObj1Box.Visible", true);
                    ui.set("#QHudObj1.Text", objText);
                    ui.set("#QHudObj1Count.Text", prog);
                    ui.set("#QHudObj1.Style.TextColor", done ? "#4CAF50" : "#ddeeff");
                    ui.set("#QHudObj1Count.Style.TextColor", done ? "#4CAF50" : "#ffffff");
                    ui.set("#QHudObj2Box.Visible", false);
                    ui.set("#QHudObj3Box.Visible", false);
                }
                ui.set("#QHudProg.Text", "");
                ui.set("#QHudReward.Text", model.getRewardText());
            } else {
                ui.set("#QuestHudPanel.Visible", false);
            }
        } else {
            ui.set("#QuestHudPanel.Visible", false);
        }
    }

    /**
     * Cherche la quete principale active du joueur.
     */
    private void renderObjectiveBoxes(UICommandBuilder ui, String prefix, QuestCondition cond) {
        int num = 1;
        List<String[]> objectives = new ArrayList<>(); // [label, counter, done]

        if (cond.getRequiredLevel() > 0) {
            int current = cachedData != null ? cachedData.getLevel() : 0;
            boolean done = current >= cond.getRequiredLevel();
            objectives.add(new String[]{"Lvl " + cond.getRequiredLevel(), done ? "OK" : current + "/" + cond.getRequiredLevel(), done ? "1" : "0"});
        }

        if (cond.getRequiredQuestCompleted() != null) {
            boolean done = cachedData != null && cachedData.getQuestData() != null
                    && cachedData.getQuestData().contains(cond.getRequiredQuestCompleted() + ":")
                    && cachedData.getQuestData().contains(":COMPLETED");
            objectives.add(new String[]{"Indice", done ? "OK" : "0/1", done ? "1" : "0"});
        }

        if (cond.getRequiredItemId() != null) {
            boolean done = hasItemInFullInventory(cachedPlayer, cond.getRequiredItemId());
            String name = cond.getRequiredItemId().replaceAll("([a-z])([A-Z])", "$1 $2").replace("_", " ");
            if (name.contains(":")) name = name.substring(name.indexOf(':') + 1);
            objectives.add(new String[]{name, done ? "OK" : "0/1", done ? "1" : "0"});
        }

        for (int i = 1; i <= 3; i++) {
            if (i <= objectives.size()) {
                String[] obj = objectives.get(i - 1);
                boolean done = "1".equals(obj[2]);
                ui.set("#" + prefix + "Obj" + i + "Box.Visible", true);
                ui.set("#" + prefix + "Obj" + i + ".Text", i + ". " + obj[0]);
                ui.set("#" + prefix + "Obj" + i + "Count.Text", obj[1]);

                if (done) {
                    ui.set("#" + prefix + "Obj" + i + ".Style.TextColor", "#4CAF50");
                    ui.set("#" + prefix + "Obj" + i + "Count.Style.TextColor", "#4CAF50");
                } else {
                    ui.set("#" + prefix + "Obj" + i + ".Style.TextColor", "#ddeeff");
                    ui.set("#" + prefix + "Obj" + i + "Count.Style.TextColor", "#ffffff");
                }
            } else {
                ui.set("#" + prefix + "Obj" + i + "Box.Visible", false);
            }
        }
    }

    private void hideObjectiveBoxes(UICommandBuilder ui, String prefix) {
        for (int i = 1; i <= 3; i++) {
            ui.set("#" + prefix + "Obj" + i + "Box.Visible", false);
        }
    }

    /**
     * Parcourt l'inventaire complet (hotbar + storage + backpack) pour trouver un item.
     */
    private boolean hasItemInFullInventory(Player player, String itemId) {
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
        try {
            for (short i = 0; i < 8; i++) {
                ItemStack item = inv.getBackpack().getItemStack(i);
                if (item != null && !item.isEmpty() && item.getItemId().equalsIgnoreCase(itemId)) return true;
            }
        } catch (IllegalArgumentException ignored) { /* backpack may have 0 capacity */ }
        return false;
    }

    private PlayerQuest findMainQuest() {
        for (PlayerQuest pq : QuestManager.getPlayerQuests(ownerUUID)) {
            QuestModel model = QuestManager.getQuest(pq.getQuestId());
            if (model != null && model.isMainStory() && !pq.isCompleted()) {
                return pq;
            }
        }
        return null;
    }
}
