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
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;;

public class CompetencesTab {

    public static final int MAX_SKILL_SLOTS = 50;
    private static final List<String> cachedSkillIds = new ArrayList<>();

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return;

        cachedSkillIds.clear();
        Set<String> seen = new LinkedHashSet<>();

        // 1. Skills innes de la classe
        ClassModel classModel = ClassManager.get(data.getPlayerClassId());
        if (classModel != null && classModel.getSkillsPassiveIds() != null) {
            for (PassiveSkill ps : classModel.getSkillsPassiveIds()) {
                seen.add(ps.name());
            }
        }

        // 2. Skill familial (noblesse)
        String familyId = data.getNobleFamilyId();
        if (familyId != null && !familyId.isEmpty()) {
            NobleFamilyModel family = FamilyManager.get(familyId);
            if (family != null && family.getFamilyPassive() != null) {
                seen.add(family.getFamilyPassive().name());
            }
        }

        // 3. Skills appris (parchemins)
        List<String> unlocked = data.getUnlockedSkills();
        if (unlocked != null) {
            seen.addAll(unlocked);
        }

        cachedSkillIds.addAll(seen);

        ui.set("#SkillCount.Text", "COMPETENCES (" + cachedSkillIds.size() + ")");

        for (int i = 0; i < MAX_SKILL_SLOTS; i++) {
            if (i < cachedSkillIds.size()) {
                String skillId = cachedSkillIds.get(i);
                boolean enabled = data.isSkillEnabled(skillId);

                // Try passive first
                SkillInfo info = getSkillInfo(skillId);

                ui.set("#SkillCard" + i + ".Visible", true);
                ui.set("#SkillName" + i + ".Text", info.name);
                ui.set("#SkillType" + i + ".Text", info.type + (info.rarity.isEmpty() ? "" : " - " + info.rarity));
                ui.set("#SkillDesc" + i + ".Text", info.description);
                ui.set("#SkillStats" + i + ".Text", info.stats);
                // Active skills with catalystId -> GIVE button, passive -> toggle
                if ("Active".equals(info.type)) {
                    ui.set("#SkillToggle" + i + ".Text", "GIVE");
                } else {
                    ui.set("#SkillToggle" + i + ".Text", enabled ? "ACTIF" : "ACTIVER");
                }
            } else {
                ui.set("#SkillCard" + i + ".Visible", false);
            }
        }
    }

    public static boolean handleToggle(String slotIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try { idx = Integer.parseInt(slotIndex); } catch (NumberFormatException e) { return false; }
        if (idx < 0 || idx >= cachedSkillIds.size()) return false;

        String skillId = cachedSkillIds.get(idx);

        // Check if it's an active skill with catalystId -> give item
        var opt = SkillManager.getSkillFromId(skillId);
        if (opt.isPresent() && opt.get().catalystId() != null) {
            return handleGiveItem(opt.get().catalystId(), ref, store);
        }

        // Passive toggle
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return false;

        if (data.isSkillEnabled(skillId)) {
            data.disableSkill(skillId);
        } else {
            data.enableSkill(skillId);
        }

        store.putComponent(ref, type, data);
        return true;
    }

    private static boolean handleGiveItem(String catalystId, Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            com.hypixel.hytale.server.core.entity.entities.Player player =
                    store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
            if (player == null) return false;

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
        } catch (IllegalArgumentException ignored) {}

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
        // Deduce rarity from the logic class package name
        try {
            if (passive.getLogic() != null) {
                String className = passive.getLogic().getClass().getPackageName();
                if (className.contains("Divin")) return "DIVIN";
                if (className.contains("Legendaire")) return "LEGENDAIRE";
                if (className.contains("Epique")) return "EPIQUE";
                if (className.contains("Rare")) return "RARE";
                if (className.contains("Common")) return "COMMUN";
                if (className.contains("Family")) return "FAMILLE";
            }
        } catch (Exception ignored) {}
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
}
