package com.eldanior.system.gui.tabs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.TitleManager;
import com.eldanior.system.titles.models.TitleBonus;
import com.eldanior.system.titles.models.TitleModel;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;

public class TitresTab {

    public static final int MAX_TITLE_SLOTS = 30;

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return;

        // Titre actuel
        String currentId = data.getCurrentTitle();
        TitleModel currentTitle = TitleManager.get(currentId);
        if (currentTitle != null) {
            ui.set("#TitreActuelName.Text", currentTitle.getDisplayName());
            ui.set("#TitreActuelDesc.Text", currentTitle.getDescription());
            ui.set("#TitreActuelBonus.Text", formatBonus(currentTitle.getBonus()));
        } else {
            ui.set("#TitreActuelName.Text", currentId != null ? currentId : "Aucun");
            ui.set("#TitreActuelDesc.Text", "");
            ui.set("#TitreActuelBonus.Text", "");
        }

        // Liste des titres possedes
        List<String> unlockedIds = data.getUnlockedTitles();
        List<TitleModel> titles = new ArrayList<>();
        if (unlockedIds != null) {
            for (String id : unlockedIds) {
                TitleModel model = TitleManager.get(id);
                if (model != null) titles.add(model);
            }
        }

        for (int i = 0; i < MAX_TITLE_SLOTS; i++) {
            if (i < titles.size()) {
                TitleModel title = titles.get(i);
                boolean isEquipped = title.getId().equals(currentId);

                ui.set("#TitleRow" + i + ".Visible", true);
                ui.set("#TitleName" + i + ".Text", title.getDisplayName());
                ui.set("#TitleRarity" + i + ".Text", title.getRarity().name());
                ui.set("#TitleDesc" + i + ".Text", title.getDescription());
                ui.set("#TitleBonus" + i + ".Text", formatBonus(title.getBonus()));

                // Bouton equiper : texte different si deja equipe
                ui.set("#TitleBtn" + i + ".Text", isEquipped ? "EQUIPE" : "EQUIPER");
            } else {
                ui.set("#TitleRow" + i + ".Visible", false);
            }
        }
    }

    public static boolean handleEquip(String titleIndex, Ref<EntityStore> ref, Store<EntityStore> store) {
        int idx;
        try {
            idx = Integer.parseInt(titleIndex);
        } catch (NumberFormatException e) {
            return false;
        }

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return false;

        List<String> unlockedIds = data.getUnlockedTitles();
        if (unlockedIds == null || idx < 0 || idx >= unlockedIds.size()) return false;

        String titleId = unlockedIds.get(idx);
        TitleModel model = TitleManager.get(titleId);
        if (model == null) return false;

        data.setCurrentTitle(titleId);
        store.putComponent(ref, type, data);

        com.eldanior.system.Leveling.utils.StatCalculator.updatePlayerStats(ref, store, data);
        return true;
    }

    private static String formatBonus(TitleBonus bonus) {
        if (bonus == null || bonus.equals(TitleBonus.NONE)) return "Aucun bonus";

        List<String> parts = new ArrayList<>();
        if (bonus.strength() != 0) parts.add("FOR +" + bonus.strength());
        if (bonus.vitality() != 0) parts.add("VIT +" + bonus.vitality());
        if (bonus.intelligence() != 0) parts.add("INT +" + bonus.intelligence());
        if (bonus.endurance() != 0) parts.add("END +" + bonus.endurance());
        if (bonus.agility() != 0) parts.add("AGL +" + bonus.agility());
        if (bonus.luck() != 0) parts.add("LCK +" + bonus.luck());

        return parts.isEmpty() ? "Aucun bonus" : String.join("  |  ", parts);
    }
}
