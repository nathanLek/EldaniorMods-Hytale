package com.eldanior.system.quest.dialogue;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Objectifs a remplir pour completer une quete narrative.
 */
public class QuestCondition {

    private int requiredLevel = 0;
    private String requiredItemId = null;
    private String requiredQuestCompleted = null;
    private boolean requirePK = false;

    public QuestCondition() {}

    public QuestCondition level(int level) { this.requiredLevel = level; return this; }
    public QuestCondition item(String itemId) { this.requiredItemId = itemId; return this; }
    public QuestCondition questCompleted(String questId) { this.requiredQuestCompleted = questId; return this; }
    public QuestCondition pk() { this.requirePK = true; return this; }

    public int getRequiredLevel() { return requiredLevel; }
    public String getRequiredItemId() { return requiredItemId; }
    public String getRequiredQuestCompleted() { return requiredQuestCompleted; }

    /**
     * Verifie si le joueur remplit toutes les conditions.
     * Retourne une liste de raisons d'echec (vide si tout est OK).
     */
    public List<String> check(PlayerLevelData data, Player player) {
        List<String> failures = new ArrayList<>();

        if (requiredLevel > 0 && data.getLevel() < requiredLevel) {
            failures.add("Niveau " + requiredLevel + " requis (vous etes " + data.getLevel() + ")");
        }

        if (requiredItemId != null && player != null) {
            boolean hasItem = false;
            var hotbar = player.getInventory().getHotbar();
            for (short i = 0; i < 9; i++) {
                ItemStack item = hotbar.getItemStack(i);
                if (item != null && !item.isEmpty() && item.getItemId().equalsIgnoreCase(requiredItemId)) {
                    hasItem = true;
                    break;
                }
            }
            if (!hasItem) {
                failures.add("Objet requis : " + requiredItemId);
            }
        }

        if (requiredQuestCompleted != null) {
            boolean found = false;
            if (data.getQuestData() != null) {
                for (String entry : data.getQuestData().split("\\|")) {
                    if (entry.startsWith(requiredQuestCompleted + ":") && entry.contains(":COMPLETED")) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) failures.add("Quete requise non completee");
        }

        if (requirePK && !data.isPK()) {
            failures.add("Statut PK requis");
        }

        return failures;
    }

    /**
     * Nombre total d'objectifs.
     */
    public int getObjectiveCount() {
        int count = 0;
        if (requiredLevel > 0) count++;
        if (requiredQuestCompleted != null) count++;
        if (requiredItemId != null) count++;
        if (requirePK) count++;
        return count;
    }

    /**
     * Liste des objectifs avec statut (check/cross) pour affichage.
     */
    public List<String> getObjectivesWithStatus(PlayerLevelData data, Player player) {
        List<String> lines = new ArrayList<>();
        int num = 1;

        if (requiredLevel > 0) {
            boolean done = data != null && data.getLevel() >= requiredLevel;
            String progress = done ? "OK" : (data != null ? data.getLevel() + "/" + requiredLevel : "...");
            lines.add(num + ". Atteindre Niveau " + requiredLevel + "   [" + progress + "]");
            num++;
        }

        if (requiredQuestCompleted != null) {
            boolean done = isQuestCompleted(data, requiredQuestCompleted);
            lines.add(num + ". Indice   [" + (done ? "OK" : "...") + "]");
            num++;
        }

        if (requiredItemId != null) {
            boolean done = hasItem(player, requiredItemId);
            // Aussi verifier via un flag dans questData si le player est null (HUD)
            if (!done && data != null && data.getQuestData() != null
                    && data.getQuestData().contains("item_found:" + requiredItemId)) {
                done = true;
            }
            lines.add(num + ". Trouver: " + formatItemName(requiredItemId) + "   [" + (done ? "OK" : "...") + "]");
            num++;
        }

        if (requirePK) {
            boolean done = data != null && data.isPK();
            lines.add(num + ". Devenir PK   [" + (done ? "OK" : "...") + "]");
        }

        return lines;
    }

    private boolean isQuestCompleted(PlayerLevelData data, String questId) {
        if (data == null || data.getQuestData() == null) return false;
        for (String entry : data.getQuestData().split("\\|")) {
            if (entry.startsWith(questId + ":") && entry.contains(":COMPLETED")) return true;
        }
        return false;
    }

    private boolean hasItem(Player player, String itemId) {
        if (player == null || itemId == null) return false;
        var hotbar = player.getInventory().getHotbar();
        for (short i = 0; i < 9; i++) {
            ItemStack item = hotbar.getItemStack(i);
            if (item != null && !item.isEmpty() && item.getItemId().equalsIgnoreCase(itemId)) return true;
        }
        return false;
    }

    /**
     * Texte simple des objectifs (sans statut).
     */
    public String getRequirementText() {
        List<String> reqs = new ArrayList<>();
        if (requiredLevel > 0) reqs.add("Atteindre Niveau " + requiredLevel);
        if (requiredQuestCompleted != null) reqs.add("Indice");
        if (requiredItemId != null) reqs.add("Trouver: " + formatItemName(requiredItemId));
        if (requirePK) reqs.add("Devenir PK");
        return reqs.isEmpty() ? "Aucun" : String.join("  |  ", reqs);
    }

    private String formatItemName(String itemId) {
        if (itemId == null) return "???";
        String name = itemId.contains(":") ? itemId.substring(itemId.indexOf(':') + 1) : itemId;
        return name.replaceAll("([a-z])([A-Z])", "$1 $2").replace("_", " ");
    }
}
