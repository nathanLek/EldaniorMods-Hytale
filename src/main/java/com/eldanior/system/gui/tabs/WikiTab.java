package com.eldanior.system.gui.tabs;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Onglet Wiki : affiche toutes les commandes disponibles avec explications.
 * Le contenu change selon que le joueur est admin ou non.
 */
public class WikiTab {

    private static int currentPage = 0;
    private static final int TOTAL_PAGES = 13;
    private static final int ADMIN_PAGE = 12;

    private static final String[] PAGE_NAMES = {
        "General", "Leveling", "Groupe", "Duel", "Echange", "Guilde", "Famille",
        "Noblesse", "Territoires", "Classes", "Competences", "Titres", "Admin"
    };

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        boolean isAdmin = false;
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            isAdmin = player != null && player.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION);
        } catch (Exception ignored) {}

        // Navigation
        ui.set("#WikiPageInfo.Text", PAGE_NAMES[currentPage] + " (" + (currentPage + 1) + "/" + TOTAL_PAGES + ")");
        ui.set("#WikiBtnPrev.Visible", currentPage > 0);
        ui.set("#WikiBtnNext.Visible", currentPage < TOTAL_PAGES - 1);

        // Cacher toutes les pages
        for (int i = 0; i < TOTAL_PAGES; i++) {
            ui.set("#WikiPage" + i + ".Visible", i == currentPage);
        }

        // Afficher/cacher la page admin
        ui.set("#WikiPage" + ADMIN_PAGE + ".Visible", currentPage == ADMIN_PAGE && isAdmin);
        if (currentPage == ADMIN_PAGE && !isAdmin) {
            currentPage = 0;
            ui.set("#WikiPage0.Visible", true);
            ui.set("#WikiPageInfo.Text", PAGE_NAMES[0] + " (1/" + TOTAL_PAGES + ")");
        }
    }

    public static boolean handlePrev() {
        if (currentPage > 0) { currentPage--; return true; }
        return false;
    }

    public static boolean handleNext() {
        if (currentPage < TOTAL_PAGES - 1) { currentPage++; return true; }
        return false;
    }
}
