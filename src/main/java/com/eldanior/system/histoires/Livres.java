package com.eldanior.system.histoires;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Livres lisibles (2026-11-14) : histoires (Eldanior_Histoire_*) et Tomes Imperiaux (Eldanior_Tome_*), poses ou tenus en main.
 * Les textes sont dans le server.lang (scripts/histoires/gen_histoires.py) : items.<Id>.page1, .page2... et .surtitre.
 */
public final class Livres {

    private Livres() {}

    private static final int MAX_PAGES = 40;
    private static final Map<String, Integer> PAGES = new ConcurrentHashMap<>();

    public static boolean isLivre(String id) {
        return id != null && (id.startsWith("Eldanior_Histoire_") || id.startsWith("Eldanior_Tome_"));
    }

    static String key(String id, String suffix) { return "server.items." + id + "." + suffix; }

    /** La cle existe-t-elle dans la langue par defaut (les livres ne sont ecrits qu'en francais, dans en-US) ? */
    static boolean exists(String key) {
        try {
            String s = I18nModule.get().getMessage(I18nModule.DEFAULT_LANGUAGE, key);
            return s != null && !s.isEmpty() && !s.equals(key);
        } catch (Exception e) {
            return false;
        }
    }

    /** Nombre de pages (cles .pageN consecutives) ; 0 : livre sans pages, on affiche sa description. */
    static int pageCount(String id) {
        return PAGES.computeIfAbsent(id, k -> {
            int n = 0;
            while (n < MAX_PAGES && exists(key(k, "page" + (n + 1)))) n++;
            return n;
        });
    }

    /** Ouvre la lecture. A appeler sur le thread du monde. */
    public static void open(Ref<EntityStore> ref, Store<EntityStore> store, Player player, PlayerRef pr, String id) {
        try {
            player.getPageManager().openCustomPage(ref, store, new LivrePage(pr, id));
        } catch (Exception e) {
            EldaniorLogger.error("Livres.open " + id, e);
        }
    }
}
