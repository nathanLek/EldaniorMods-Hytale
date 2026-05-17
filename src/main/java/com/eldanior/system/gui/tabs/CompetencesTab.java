package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
import com.eldanior.system.skills.SkillManager;
import com.eldanior.system.skills.models.SkillModel;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;

public class CompetencesTab {

    public static final int MAX_SKILL_SLOTS = 20;
    private static int currentPage = 0;
    // false = passives, true = actives
    private static boolean viewingActives = false;

    // Rarity order: DIVINE (highest) -> COMMON (lowest)
    private static final List<String> RARITY_ORDER = List.of(
            "DIVIN", "LEGENDAIRE", "FAMILLE", "UNIQUE", "EPIQUE", "RARE", "UNCOMMON", "COMMUN", ""
    );

    private static List<String> buildSkillList(PlayerLevelData data) {
        Set<String> seen = new LinkedHashSet<>();

        ClassModel classModel = ClassManager.get(data.getPlayerClassId());
        if (classModel != null && classModel.getSkillsPassiveIds() != null) {
            for (PassiveSkill ps : classModel.getSkillsPassiveIds()) {
                seen.add(ps.name());
            }
        }

        String familyId = data.getNobleFamilyId();
        if (familyId != null && !familyId.isEmpty()) {
            NobleFamilyModel family = FamilyManager.get(familyId);
            if (family != null && family.getFamilyPassive() != null) {
                seen.add(family.getFamilyPassive().name());
            }
        }

        List<String> unlocked = data.getUnlockedSkills();
        if (unlocked != null) {
            seen.addAll(unlocked);
        }

        Set<String> enabled = data.getEnabledSkills();
        if (enabled != null) {
            seen.addAll(enabled);
        }

        return new ArrayList<>(seen);
    }

    private static int rarityIndex(String rarity) {
        int idx = RARITY_ORDER.indexOf(rarity);
        return idx >= 0 ? idx : RARITY_ORDER.size();
    }

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return;

        List<String> allSkillIds = buildSkillList(data);

        // Build SkillInfo for all skills
        List<SkillEntry> passives = new ArrayList<>();
        List<SkillEntry> actives = new ArrayList<>();

        for (String skillId : allSkillIds) {
            SkillInfo info = getSkillInfo(skillId);
            SkillEntry entry = new SkillEntry(skillId, info);
            if ("Active".equals(info.type)) {
                actives.add(entry);
            } else {
                passives.add(entry);
            }
        }

        // Sort by rarity (inverse: DIVINE first, COMMON last)
        Comparator<SkillEntry> byRarity = Comparator.comparingInt(e -> rarityIndex(e.info.rarity));
        passives.sort(byRarity);
        actives.sort(byRarity);

        // Select current list
        List<SkillEntry> currentList = viewingActives ? actives : passives;
        int totalItems = currentList.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / MAX_SKILL_SLOTS));
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;

        int startIdx = currentPage * MAX_SKILL_SLOTS;

        // Header
        String sectionName = viewingActives ? "ACTIVES" : "PASSIVES";
        ui.set("#SkillCount.Text", "COMPETENCES " + sectionName + " (" + totalItems + ")");
        ui.set("#SkillPage.Text", "Page " + (currentPage + 1) + " / " + totalPages);

        // Tab buttons highlight via background
        ui.set("#SkillTabPassive.Background", viewingActives ? "#1a2a3a" : "#2a4a6a");
        ui.set("#SkillTabActive.Background", viewingActives ? "#2a4a6a" : "#1a2a3a");

        for (int i = 0; i < MAX_SKILL_SLOTS; i++) {
            int listIdx = startIdx + i;
            if (listIdx < currentList.size()) {
                SkillEntry entry = currentList.get(listIdx);
                SkillInfo info = entry.info;
                boolean enabled = data.isSkillEnabled(entry.skillId);
                String rarityColor = getSkillRarityColor(info.rarity);

                ui.set("#SkillCard" + i + ".Visible", true);
                ui.set("#SkillName" + i + ".Text", info.name);
                ui.set("#SkillType" + i + ".Text", info.rarity.isEmpty() ? info.type : info.rarity);
                ui.set("#SkillDesc" + i + ".Text", info.description);
                ui.set("#SkillStats" + i + ".Text", info.stats);

                // Rarity color on label + border (same as market)
                ui.set("#SkillType" + i + ".Style.TextColor", rarityColor);
                ui.set("#SkillBorder" + i + ".Background", rarityColor);

                // Toggle button
                if ("Active".equals(info.type)) {
                    boolean alreadyHas = false;
                    var skillOpt = SkillManager.getSkillFromId(entry.skillId);
                    if (skillOpt.isPresent() && skillOpt.get().catalystId() != null) {
                        com.hypixel.hytale.server.core.entity.entities.Player p =
                                store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                        if (p != null) {
                            alreadyHas = playerHasItem(p, skillOpt.get().catalystId())
                                    || personalChestHasItem(ref, store, skillOpt.get().catalystId());
                        }
                    }
                    ui.set("#SkillToggle" + i + ".Text", alreadyHas ? "POSSEDE" : "GIVE");
                    ui.set("#SkillToggle" + i + ".Visible", !alreadyHas);
                } else {
                    ui.set("#SkillToggle" + i + ".Text", enabled ? "DESACTIVER" : "ACTIVER");
                    ui.set("#SkillToggle" + i + ".Background", enabled ? "#1a3a1a" : "#3a1a1a");
                    ui.set("#SkillToggle" + i + ".Visible", true);
                }
            } else {
                ui.set("#SkillCard" + i + ".Visible", false);
            }
        }
    }

    public static boolean handleToggle(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return false;

        List<String> allSkillIds = buildSkillList(data);

        // Rebuild the same sorted list to find the correct skill
        List<SkillEntry> passives = new ArrayList<>();
        List<SkillEntry> actives = new ArrayList<>();
        for (String skillId : allSkillIds) {
            SkillInfo info = getSkillInfo(skillId);
            SkillEntry entry = new SkillEntry(skillId, info);
            if ("Active".equals(info.type)) {
                actives.add(entry);
            } else {
                passives.add(entry);
            }
        }
        Comparator<SkillEntry> byRarity = Comparator.comparingInt(e -> rarityIndex(e.info.rarity));
        passives.sort(byRarity);
        actives.sort(byRarity);

        List<SkillEntry> currentList = viewingActives ? actives : passives;
        int realIdx = currentPage * MAX_SKILL_SLOTS + idx;
        if (realIdx < 0 || realIdx >= currentList.size()) return false;

        String skillId = currentList.get(realIdx).skillId;

        // Check if it's an active skill with catalystId -> give item
        var opt = SkillManager.getSkillFromId(skillId);
        if (opt.isPresent() && opt.get().catalystId() != null) {
            return handleGiveItem(opt.get().catalystId(), ref, store);
        }

        // Passive toggle
        if (data.isSkillEnabled(skillId)) {
            data.disableSkill(skillId);
        } else {
            data.enableSkill(skillId);
        }

        store.putComponent(ref, type, data);
        return true;
    }

    public static void nextPage() { currentPage++; }
    public static void prevPage() { if (currentPage > 0) currentPage--; }
    public static void switchToPassives() { viewingActives = false; currentPage = 0; }
    public static void switchToActives() { viewingActives = true; currentPage = 0; }

    private static String getSkillRarityColor(String rarity) {
        return switch (rarity) {
            case "DIVIN" -> "#7EB8DA";
            case "LEGENDAIRE" -> "#FFD700";
            case "EPIQUE" -> "#9C27B0";
            case "UNIQUE" -> "#cc4444";
            case "RARE" -> "#2196F3";
            case "UNCOMMON" -> "#4CAF50";
            case "FAMILLE" -> "#E91E63";
            default -> "#8899aa";
        };
    }

    private static boolean handleGiveItem(String catalystId, Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            com.hypixel.hytale.server.core.entity.entities.Player player =
                    store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            if (player == null) return false;

            if (playerHasItem(player, catalystId)) {
                player.sendMessage(com.hypixel.hytale.server.core.Message.raw("§cVous possedez deja cet item !"));
                return false;
            }

            if (personalChestHasItem(ref, store, catalystId)) {
                player.sendMessage(com.hypixel.hytale.server.core.Message.raw("§cCet item est deja dans votre coffre personnel !"));
                return false;
            }

            var itemStack = new com.hypixel.hytale.server.core.inventory.ItemStack(catalystId, 1);
            var result = player.getInventory().getHotbar().addItemStack(itemStack);
            if (result.succeeded()) {
                player.sendMessage(com.hypixel.hytale.server.core.Message.raw("§aItem obtenu : " + catalystId));
                return true;
            } else {
                player.sendMessage(com.hypixel.hytale.server.core.Message.raw("§cInventaire plein !"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean playerHasItem(com.hypixel.hytale.server.core.entity.entities.Player player, String itemId) {
        try {
            var inventory = player.getInventory();
            for (short i = 0; i < 9; i++) {
                var item = inventory.getHotbar().getItemStack(i);
                if (item != null && !item.isEmpty() && itemId.equals(item.getItemId())) return true;
            }
            for (short i = 0; i < 27; i++) {
                var item = inventory.getStorage().getItemStack(i);
                if (item != null && !item.isEmpty() && itemId.equals(item.getItemId())) return true;
            }
            for (short i = 0; i < 8; i++) {
                var item = inventory.getBackpack().getItemStack(i);
                if (item != null && !item.isEmpty() && itemId.equals(item.getItemId())) return true;
            }
        } catch (Exception e) { /* skip */ }
        return false;
    }

    private static boolean personalChestHasItem(Ref<EntityStore> ref, Store<EntityStore> store, String itemId) {
        try {
            var chestData = store.getComponent(ref, com.eldanior.system.Inventory.components.PlayerPersonalChestData.TYPE);
            if (chestData == null) return false;
            for (var item : chestData.getStoredItems()) {
                if (item != null && !item.isEmpty() && itemId.equals(item.getItemId())) return true;
            }
        } catch (Exception e) { /* skip */ }
        return false;
    }

    private static SkillInfo getSkillInfo(String skillId) {
        SkillInfo info = new SkillInfo();

        // Try PassiveSkill enum first
        try {
            PassiveSkill passive = PassiveSkill.valueOf(skillId.toUpperCase());
            info.name = passive.getDisplayName();
            info.type = "Passive";
            info.description = passive.getDescription();
            info.rarity = guessRarity(passive);
            info.stats = passive.getManaCost() > 0 ? "Mana: " + passive.getManaCost() : "";
            return info;
        } catch (IllegalArgumentException e) { /* argument invalide */ }

        // Try SkillModel (active skills)
        var opt = SkillManager.getSkillFromId(skillId);
        if (opt.isPresent()) {
            SkillModel model = opt.get();
            info.name = model.displayName();
            info.type = model.catalystId() != null ? "Active" : "Passive";
            info.description = model.requiredClass() != null ? "Classe: " + model.requiredClass() : "";
            info.rarity = "";
            info.stats = buildModelStats(model);
            return info;
        }

        // Fallback
        info.name = formatId(skillId);
        info.type = "?";
        return info;
    }

    private static String guessRarity(PassiveSkill passive) {
        // Aura de Dignité : rareté selon le niveau
        String name = passive.name();
        if (name.equals("DIGNITY_AURA_1")) return "RARE";
        if (name.equals("DIGNITY_AURA_2")) return "EPIQUE";
        if (name.equals("DIGNITY_AURA_3")) return "UNIQUE";
        if (name.equals("DIGNITY_AURA_4")) return "LEGENDAIRE";
        if (name.equals("DIGNITY_AURA_5")) return "DIVIN";

        try {
            if (passive.getLogic() != null) {
                String className = passive.getLogic().getClass().getPackageName();
                if (className.contains("Divin")) return "DIVIN";
                if (className.contains("Legendaire")) return "LEGENDAIRE";
                if (className.contains("Epique")) return "EPIQUE";
                if (className.contains("Unique")) return "UNIQUE";
                if (className.contains("Rare")) return "RARE";
                if (className.contains("Uncommon")) return "UNCOMMON";
                if (className.contains("Common")) return "COMMUN";
                if (className.contains("Family")) return "FAMILLE";
                if (className.contains("Craft")) return "COMMUN";
            }
        } catch (Exception e) { EldaniorLogger.error("CompetencesTab", e); }
        return "";
    }

    private static String buildModelStats(SkillModel model) {
        List<String> parts = new ArrayList<>();
        if (model.manaCost() > 0) parts.add("Mana: " + model.manaCost());
        if (model.cooldown() > 0) parts.add("CD: " + model.cooldown() + "s");
        if (model.damage() > 0) parts.add("Dmg: " + model.damage());
        if (model.range() > 0) parts.add("Range: " + model.range());
        if (model.duration() > 0) parts.add("Duree: " + model.duration() + "s");
        return parts.isEmpty() ? "" : String.join(" | ", parts);
    }

    private static String formatId(String id) {
        String[] parts = id.replace("_", " ").split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    private static class SkillInfo {
        String name = "?";
        String type = "?";
        String rarity = "";
        String description = "";
        String stats = "";
    }

    private static class SkillEntry {
        final String skillId;
        final SkillInfo info;

        SkillEntry(String skillId, SkillInfo info) {
            this.skillId = skillId;
            this.info = info;
        }
    }
}
