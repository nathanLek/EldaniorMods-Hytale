package com.eldanior.system.gui.tabs;

import com.eldanior.system.classement.ClassementManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;

public class ClassementsTab {

    public static final int MAX_RANK_SLOTS = 10;
    private static final java.util.concurrent.ConcurrentHashMap<java.util.UUID, String> playerCategories = new java.util.concurrent.ConcurrentHashMap<>();

    private static java.util.UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        com.hypixel.hytale.server.core.universe.PlayerRef pRef = store.getComponent(ref, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
        if (pRef == null) return new java.util.UUID(0, 0);
        try { return com.eldanior.system.config.UUIDExtractor.getUUID(pRef); } catch (Exception e) { return new java.util.UUID(0, 0); }
    }

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        java.util.UUID uuid = getPlayerUUID(ref, store);
        String category = playerCategories.getOrDefault(uuid, "mobs");
        populateCategory(ui, category);
    }

    public static void switchCategory(String category, UICommandBuilder ui, java.util.UUID uuid) {
        playerCategories.put(uuid, category);
        populateCategory(ui, category);
    }

    /** @deprecated Use switchCategory(category, ui, uuid) */
    public static void switchCategory(String category, UICommandBuilder ui) {
        populateCategory(ui, category);
    }

    private static void populateCategory(UICommandBuilder ui, String category) {
        // Toggle active indicator on button text
        ui.set("#RkBtnMobs.Text", "mobs".equals(category) ? "> MOBS <" : "MOBS");
        ui.set("#RkBtnPvP.Text", "pvp".equals(category) ? "> PVP <" : "PVP");
        ui.set("#RkBtnGuildFam.Text", "guildfam".equals(category) ? "> GUILDES & FAM <" : "GUILDES & FAM");
        ui.set("#RkBtnDuel.Text", "duel".equals(category) ? "> DUEL <" : "DUEL");

        // Title
        String title;
        String color;
        List<ClassementManager.RankEntry> entries;

        switch (category) {
            case "pvp" -> {
                title = "CLASSEMENT PVP - KILLS JOUEURS";
                color = "#cc4444";
                entries = ClassementManager.getPvPRanking(MAX_RANK_SLOTS);
            }
            case "guildfam" -> {
                title = "CLASSEMENT GUILDES & FAMILLES";
                color = "#D4AF37";
                entries = ClassementManager.getGuildFamilyRanking(MAX_RANK_SLOTS);
            }
            case "duel" -> {
                title = "CLASSEMENT DUEL - VICTOIRES";
                color = "#ff9800";
                entries = ClassementManager.getDuelRanking(MAX_RANK_SLOTS);
            }
            default -> {
                title = "CLASSEMENT MOBS - TOTAL KILLS";
                color = "#4CAF50";
                entries = ClassementManager.getMobRanking(MAX_RANK_SLOTS);
            }
        }

        ui.set("#RkTitle.Text", title);

        // Duel placeholder (plus utilise)
        ui.set("#RkDuelSoon.Visible", false);

        for (int i = 0; i < MAX_RANK_SLOTS; i++) {
            if (i < entries.size() && !"duel".equals(category)) {
                ClassementManager.RankEntry entry = entries.get(i);
                ui.set("#RkRow" + i + ".Visible", true);

                // Couleur position : or/argent/bronze pour top 3
                String posColor = switch (i) {
                    case 0 -> "#FFD700";
                    case 1 -> "#C0C0C0";
                    case 2 -> "#CD7F32";
                    default -> "#667788";
                };
                ui.set("#RkPos" + i + ".Text", "#" + (i + 1));
                ui.set("#RkPos" + i + ".Style.TextColor", posColor);
                ui.set("#RkName" + i + ".Text", entry.name());
                ui.set("#RkVal" + i + ".Text", String.valueOf(entry.value()));
            } else {
                ui.set("#RkRow" + i + ".Visible", false);
            }
        }
    }
}
