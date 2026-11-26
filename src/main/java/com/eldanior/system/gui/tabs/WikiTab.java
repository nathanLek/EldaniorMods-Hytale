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
 * Onglet Wiki : la page 0 liste les commandes joueur (seule page visible d'un joueur) ; les pages suivantes
 * (systemes, commandes d'administration) ne s'affichent qu'aux admins.
 */
public class WikiTab {

    private static final ConcurrentHashMap<UUID, Integer> playerPages = new ConcurrentHashMap<>();
    private static final int TOTAL_PAGES = 14;

    private static final String[] PAGE_NAMES = {
        "Commandes joueur", "Leveling", "Groupe", "Duel", "Echange", "Guilde", "Famille",
        "Noblesse", "Territoires", "Classes", "Competences", "Titres", "Primes", "Admin"
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

        boolean isAdmin = false;
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            isAdmin = player != null && player.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION);
        } catch (Exception ignored) {}

        // Joueur : la page 0 (commandes joueur) seule, sans navigation. Admin : toutes les pages (2026-11-12).
        int currentPage = isAdmin ? getPage(uuid) : 0;
        if (!isAdmin) setPage(uuid, 0);

        ui.set("#WikiPageInfo.Text", isAdmin ? PAGE_NAMES[currentPage] + " (" + (currentPage + 1) + "/" + TOTAL_PAGES + ")" : PAGE_NAMES[0]);
        ui.set("#WikiBtnPrev.Visible", isAdmin && currentPage > 0);
        ui.set("#WikiBtnNext.Visible", isAdmin && currentPage < TOTAL_PAGES - 1);
        for (int i = 0; i < TOTAL_PAGES; i++) {
            ui.set("#WikiPage" + i + ".Visible", i == currentPage);
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

    /** Nettoyage memoire quand un joueur se deconnecte */
    public static void cleanupPlayer(java.util.UUID uuid) {
        playerPages.remove(uuid);
    }
}
