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

    /** Per-player state for pagination, tab view, and cooldowns */
    private static final java.util.concurrent.ConcurrentHashMap<java.util.UUID, PlayerSkillState> playerStates = new java.util.concurrent.ConcurrentHashMap<>();

    private static PlayerSkillState getState(java.util.UUID uuid) {
        return playerStates.computeIfAbsent(uuid, k -> new PlayerSkillState());
    }

    private static class PlayerSkillState {
        int currentPage = 0;
        boolean viewingActives = false;
        boolean hasCooldownActive = false;
        final Map<Integer, CooldownEntry> activeCooldowns = new HashMap<>();
    }

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
        if (classModel != null && classModel.getActiveSkillIds() != null) {
            seen.addAll(classModel.getActiveSkillIds());
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

    private static java.util.UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        com.hypixel.hytale.server.core.universe.PlayerRef pRef = store.getComponent(ref, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
        if (pRef == null) return new java.util.UUID(0, 0);
        try { return com.eldanior.system.config.UUIDExtractor.getUUID(pRef); } catch (Exception e) { return new java.util.UUID(0, 0); }
    }

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return;
        java.util.UUID playerUUID = getPlayerUUID(ref, store);
        PlayerSkillState state = getState(playerUUID);
        state.hasCooldownActive = false;
        state.activeCooldowns.clear();

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
        List<SkillEntry> currentList = state.viewingActives ? actives : passives;
        int totalItems = currentList.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / MAX_SKILL_SLOTS));
        if (state.currentPage >= totalPages) state.currentPage = totalPages - 1;
        if (state.currentPage < 0) state.currentPage = 0;

        int startIdx = state.currentPage * MAX_SKILL_SLOTS;

        // Header
        String sectionName = state.viewingActives ? "ACTIVES" : "PASSIVES";
        ui.set("#SkillCount.Text", "COMPETENCES " + sectionName + " (" + totalItems + ")");
        ui.set("#SkillPage.Text", "Page " + (state.currentPage + 1) + " / " + totalPages);

        // Tab buttons highlight via background
        ui.set("#SkillTabPassive.Background", state.viewingActives ? "#1a2a3a" : "#2a4a6a");
        ui.set("#SkillTabActive.Background", state.viewingActives ? "#2a4a6a" : "#1a2a3a");

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

                // Rarity color on border + type label
                ui.set("#SkillBorder" + i + ".Background", rarityColor);
                ui.set("#SkillType" + i + ".Style.TextColor", rarityColor);

                // --- Colonne stats : Mana / Endurance / Vie / Cooldown / Taux ---
                long remainingCd = data.getRemainingCooldown(entry.skillId.toUpperCase());
                float progression = data.getSkillProgression(entry.skillId);
                boolean mastered = data.isSkillMastered(entry.skillId);

                // Mana (bleu)
                ui.set("#SkillMana" + i + ".Visible", info.manaCost > 0);
                if (info.manaCost > 0) {
                    ui.set("#SkillManaVal" + i + ".Text", String.valueOf(info.manaCost));
                }

                // Endurance (jaune-vert)
                ui.set("#SkillEndurance" + i + ".Visible", info.enduranceCost > 0);
                if (info.enduranceCost > 0) {
                    ui.set("#SkillEnduranceVal" + i + ".Text", (int)(info.enduranceCost * 100) + "%");
                }

                // Vie (rouge)
                ui.set("#SkillLife" + i + ".Visible", info.lifeCost > 0);
                if (info.lifeCost > 0) {
                    ui.set("#SkillLifeVal" + i + ".Text", (int)(info.lifeCost * 100) + "%");
                }

                // Cooldown (orange) — badge affiche toujours le MAX
                ui.set("#SkillCd" + i + ".Visible", info.cooldownMax > 0);
                if (info.cooldownMax > 0) {
                    ui.set("#SkillCdVal" + i + ".Text", formatCooldownMax(info.cooldownMax));
                }

                // Taux / Maitrise (vert)
                ui.set("#SkillMastery" + i + ".Visible", true);
                ui.set("#SkillMasteryVal" + i + ".Text", mastered ? "MAX" : String.format("%.2f%%", progression));

                // --- 3 états : ACTIF / INACTIF / EN RECHARGE ---
                if ("Active".equals(info.type)) {
                    // Skills actifs : gestion spéciale (give item)
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
                    ui.set("#SkillActiveBar" + i + ".Visible", alreadyHas);
                    ui.set("#SkillStateBadge" + i + ".Visible", false);
                    ui.set("#SkillToggle" + i + ".Visible", !alreadyHas);
                    ui.set("#SkillCdTimer" + i + ".Visible", false);
                    ui.set("#SkillToggleWrap" + i + ".Background", "#2a3040");
                } else if (remainingCd > 0) {
                    // EN RECHARGE
                    state.hasCooldownActive = true;
                    state.activeCooldowns.put(i, new CooldownEntry(System.currentTimeMillis() + remainingCd));
                    ui.set("#SkillActiveBar" + i + ".Visible", false);
                    ui.set("#SkillStateBadge" + i + ".Visible", true);
                    ui.set("#SkillStateBadge" + i + ".Text", "RECHARGE");
                    ui.set("#SkillStateBadge" + i + ".Style.TextColor", "#ef9f27");
                    ui.set("#SkillStateBadge" + i + ".Background", "#2a2410");
                    ui.set("#SkillToggleWrap" + i + ".Background", "#2a2410");
                    ui.set("#SkillToggle" + i + ".Visible", false);
                    ui.set("#SkillCdTimer" + i + ".Visible", true);
                    ui.set("#SkillCdTimer" + i + ".Text", formatCooldown(remainingCd));
                } else if (enabled) {
                    // ACTIF
                    ui.set("#SkillActiveBar" + i + ".Visible", true);
                    ui.set("#SkillStateBadge" + i + ".Visible", true);
                    ui.set("#SkillStateBadge" + i + ".Text", "ACTIVE");
                    ui.set("#SkillStateBadge" + i + ".Style.TextColor", "#7ed47e");
                    ui.set("#SkillStateBadge" + i + ".Background", "#1a3a1a");
                    ui.set("#SkillToggleWrap" + i + ".Background", "#1a3a1a");
                    ui.set("#SkillToggle" + i + ".Visible", true);
                    ui.set("#SkillToggle" + i + ".Text", "ON");
                    ui.set("#SkillCdTimer" + i + ".Visible", false);
                } else {
                    // INACTIF
                    ui.set("#SkillActiveBar" + i + ".Visible", false);
                    ui.set("#SkillStateBadge" + i + ".Visible", false);
                    ui.set("#SkillToggleWrap" + i + ".Background", "#2a3040");
                    ui.set("#SkillToggle" + i + ".Visible", true);
                    ui.set("#SkillToggle" + i + ".Text", "OFF");
                    ui.set("#SkillCdTimer" + i + ".Visible", false);
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

        java.util.UUID playerUUID = getPlayerUUID(ref, store);
        PlayerSkillState state = getState(playerUUID);

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

        List<SkillEntry> currentList = state.viewingActives ? actives : passives;
        int realIdx = state.currentPage * MAX_SKILL_SLOTS + idx;
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

    public static boolean hasCooldownActive() {
        // Check if ANY player has active cooldowns (used by SystemScreen to decide timer)
        for (PlayerSkillState s : playerStates.values()) {
            if (s.hasCooldownActive) return true;
        }
        return false;
    }

    public static boolean hasCooldownActive(java.util.UUID uuid) {
        PlayerSkillState state = playerStates.get(uuid);
        return state != null && state.hasCooldownActive;
    }

    // Met a jour UNIQUEMENT les timers cooldown (pas besoin du store)
    public static void updateCooldownTimers(UICommandBuilder ui, java.util.UUID uuid) {
        PlayerSkillState state = playerStates.get(uuid);
        if (state == null) return;
        boolean anyActive = false;
        for (var entry : state.activeCooldowns.entrySet()) {
            int slot = entry.getKey();
            CooldownEntry cd = entry.getValue();
            long remaining = cd.endTime - System.currentTimeMillis();
            if (remaining > 0) {
                ui.set("#SkillCdTimer" + slot + ".Text", formatCooldown(remaining));
                anyActive = true;
            } else {
                // Cooldown termine -- repasser en etat ACTIF
                ui.set("#SkillStateBadge" + slot + ".Visible", true);
                ui.set("#SkillStateBadge" + slot + ".Text", "ACTIVE");
                ui.set("#SkillStateBadge" + slot + ".Style.TextColor", "#7ed47e");
                ui.set("#SkillStateBadge" + slot + ".Background", "#1a3a1a");
                ui.set("#SkillActiveBar" + slot + ".Visible", true);
                ui.set("#SkillToggleWrap" + slot + ".Background", "#1a3a1a");
                ui.set("#SkillToggle" + slot + ".Visible", true);
                ui.set("#SkillToggle" + slot + ".Text", "ON");
                ui.set("#SkillCdTimer" + slot + ".Visible", false);
            }
        }
        if (!anyActive) {
            state.activeCooldowns.clear();
            state.hasCooldownActive = false;
        }
    }

    /** @deprecated Use updateCooldownTimers(ui, uuid) instead */
    public static void updateCooldownTimers(UICommandBuilder ui) {
        // Fallback: update all players (backward compat)
        for (var entry : playerStates.entrySet()) {
            updateCooldownTimers(ui, entry.getKey());
        }
    }

    private static class CooldownEntry {
        final long endTime;
        CooldownEntry(long endTime) { this.endTime = endTime; }
    }

    public static void nextPage(java.util.UUID uuid) { getState(uuid).currentPage++; }
    public static void prevPage(java.util.UUID uuid) { PlayerSkillState s = getState(uuid); if (s.currentPage > 0) s.currentPage--; }
    public static void switchToPassives(java.util.UUID uuid) { PlayerSkillState s = getState(uuid); s.viewingActives = false; s.currentPage = 0; }
    public static void switchToActives(java.util.UUID uuid) { PlayerSkillState s = getState(uuid); s.viewingActives = true; s.currentPage = 0; }

    /** @deprecated Use nextPage(uuid) */
    public static void nextPage() { }
    /** @deprecated Use prevPage(uuid) */
    public static void prevPage() { }
    /** @deprecated Use switchToPassives(uuid) */
    public static void switchToPassives() { }
    /** @deprecated Use switchToActives(uuid) */
    public static void switchToActives() { }

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
                player.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("Vous possedez deja cet item !"));
                return false;
            }

            if (personalChestHasItem(ref, store, catalystId)) {
                player.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("Cet item est deja dans votre coffre personnel !"));
                return false;
            }

            var itemStack = new com.hypixel.hytale.server.core.inventory.ItemStack(catalystId, 1);
            var result = player.getInventory().getHotbar().addItemStack(itemStack);
            if (result.succeeded()) {
                player.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("Item obtenu : " + catalystId));
                return true;
            } else {
                player.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("Inventaire plein !"));
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
            info.manaCost = passive.getManaCost();
            info.cooldownMax = passive.getCooldownSeconds();
            if (passive.getLogic() != null) {
                info.enduranceCost = passive.getLogic().getEnduranceCostPercent();
                info.lifeCost = passive.getLogic().getLifeCostPercent();
            }
            info.stats = "";
            return info;
        } catch (IllegalArgumentException e) { /* argument invalide */ }

        // Try SkillModel (active skills)
        var opt = SkillManager.getSkillFromId(skillId);
        if (opt.isPresent()) {
            SkillModel model = opt.get();
            info.name = model.displayName();
            if (model.catalystId() != null) {
                info.type = "Active";
                info.rarity = guessRarityFromSkillId(skillId);
                info.description = "ACTIVE";
            } else {
                info.type = "Passive";
                info.rarity = guessRarityFromSkillId(skillId);
                info.description = model.requiredClass() != null ? "Classe: " + model.requiredClass() : "";
            }
            info.stats = buildModelStats(model);
            return info;
        }

        // Fallback
        info.name = formatId(skillId);
        info.type = "?";
        return info;
    }

    private static final Map<String, String> SKILL_RARITY_MAP = Map.ofEntries(
            Map.entry("BOULE_DE_FEU", "UNCOMMON"),
            Map.entry("FLAMME_ARDENTE", "UNCOMMON"),
            Map.entry("SOUFFLE_EMBRASE", "RARE"),
            Map.entry("PIEGE_INCENDIAIRE", "RARE"),
            Map.entry("METEORE", "LEGENDAIRE"),
            Map.entry("NOVA_DE_FEU", "UNIQUE"),
            Map.entry("INFERNO", "EPIQUE"),
            Map.entry("SOUFFLE_DU_DRAGON", "LEGENDAIRE"),
            Map.entry("APOCALYPSE_IGNEE", "DIVIN"),
            Map.entry("BATON_MAGIQUE", "COMMUN"),
            Map.entry("TEMPETE_ELEMENTAIRE", "RARE"),
            Map.entry("LIEN_ENCHANTEMENT", "RARE"),
            Map.entry("MAIN_DU_TREPAS", "RARE")
    );

    private static String guessRarityFromSkillId(String skillId) {
        return SKILL_RARITY_MAP.getOrDefault(skillId.toUpperCase(), "COMMUN");
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

    private static String formatCooldown(long remainingMs) {
        long totalSeconds = remainingMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes > 0) {
            return minutes + "m" + String.format("%02d", seconds) + "s";
        }
        return seconds + "s";
    }

    private static String formatCooldownMax(float seconds) {
        int min = (int)(seconds / 60);
        int sec = (int)(seconds % 60);
        if (min > 0 && sec > 0) return min + "m" + String.format("%02d", sec) + "s";
        if (min > 0) return min + "m";
        return sec + "s";
    }

    private static class SkillInfo {
        String name = "?";
        String type = "?";
        String rarity = "";
        String description = "";
        String stats = "";
        int manaCost = 0;
        float enduranceCost = 0f;
        float lifeCost = 0f;
        float cooldownMax = 0f;
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
