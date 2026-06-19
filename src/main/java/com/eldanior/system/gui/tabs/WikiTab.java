package com.eldanior.system.gui.tabs;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Onglet Wiki : affiche toutes les commandes disponibles avec explications.
 * Le contenu change selon que le joueur est admin ou non.
 */
public class WikiTab {

    private static final ConcurrentHashMap<UUID, Integer> playerPages = new ConcurrentHashMap<>();
    private static final int TOTAL_PAGES = 13;
    private static final int ADMIN_PAGE = 12;

    private static final String[] PAGE_NAMES = {
        "General", "Leveling", "Groupe", "Duel", "Echange", "Guilde", "Famille",
        "Noblesse", "Territoires", "Classes", "Competences", "Titres", "Admin"
    };

    private static int getPage(UUID uuid) { return playerPages.getOrDefault(uuid, 0); }
    private static void setPage(UUID uuid, int page) { playerPages.put(uuid, page); }

    private static UUID getPlayerUUID(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return new UUID(0, 0);
        try { return UUIDExtractor.getUUID(pRef); } catch (Exception e) { return new UUID(0, 0); }
    }

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID uuid = getPlayerUUID(ref, store);
        int currentPage = getPage(uuid);

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
            setPage(uuid, currentPage);
            ui.set("#WikiPage0.Visible", true);
            ui.set("#WikiPageInfo.Text", PAGE_NAMES[0] + " (1/" + TOTAL_PAGES + ")");
        }
    }

    public static boolean handlePrev(UUID uuid) {
        int page = getPage(uuid);
        if (page > 0) { setPage(uuid, page - 1); return true; }
        return false;
    }

    public static boolean handleNext(UUID uuid) {
        int page = getPage(uuid);
        if (page < TOTAL_PAGES - 1) { setPage(uuid, page + 1); return true; }
        return false;
    }

    /** @deprecated Use handlePrev(uuid) */
    public static boolean handlePrev() { return false; }
    /** @deprecated Use handleNext(uuid) */
    public static boolean handleNext() { return false; }
}
