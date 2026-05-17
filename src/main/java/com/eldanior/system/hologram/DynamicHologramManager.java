package com.eldanior.system.hologram;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.territory.ArenaManager;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Gere les hologrammes dynamiques qui se mettent a jour automatiquement.
 * Utilise pour afficher les classements d'arene en temps reel.
 *
 * Usage : /es hologram create arena_<arenaName>
 * Cela cree un hologramme dynamique qui affiche le top 5 de l'arene.
 */
public class DynamicHologramManager {

    // arenaParcelId -> holoId
    private static final Map<String, String> arenaHolograms = new ConcurrentHashMap<>();

    private static final int TOP_PLAYERS = 5;
    private static final String[] RANK_COLORS = {
            "§6", // 1st - Or
            "§f", // 2nd - Argent
            "§c", // 3rd - Bronze
            "§7", // 4th - Gris
            "§8"  // 5th - Gris fonce
    };

    /**
     * Cree un hologramme dynamique pour une arene.
     */
    public static String createArenaHologram(String arenaParcelId, double x, double y, double z, String worldName) {
        ParcelData arena = ParcelManager.get(arenaParcelId);
        if (arena == null || arena.getType() != ParcelType.ARENA) return null;

        // Generer les lignes initiales
        List<String> lines = buildArenaLines(arenaParcelId, arena.getName());

        // Creer l'hologramme via HologramManager
        HologramData holo = HologramManager.create(lines, x, y, z, worldName);

        if (holo != null) {
            arenaHolograms.put(arenaParcelId, holo.getId());
            EldaniorLogger.info("[DynamicHolo] Hologramme arene cree: " + holo.getId() +
                    " pour " + arena.getName());
            return holo.getId();
        }

        return null;
    }

    /**
     * Met a jour tous les hologrammes dynamiques d'arene.
     * Appele periodiquement (toutes les 60s).
     */
    public static void updateAll() {
        for (Map.Entry<String, String> entry : arenaHolograms.entrySet()) {
            try {
                String arenaId = entry.getKey();
                String holoId = entry.getValue();

                HologramData holo = HologramManager.get(holoId);
                if (holo == null) {
                    arenaHolograms.remove(arenaId);
                    continue;
                }

                ParcelData arena = ParcelManager.get(arenaId);
                if (arena == null) {
                    arenaHolograms.remove(arenaId);
                    continue;
                }

                // Construire les nouvelles lignes
                List<String> newLines = buildArenaLines(arenaId, arena.getName());

                // Despawn l'ancien et respawn avec le nouveau texte
                HologramManager.delete(holoId);
                HologramData newHolo = HologramManager.create(newLines,
                        holo.getX(), holo.getY(), holo.getZ(), holo.getWorldName());

                if (newHolo != null) {
                    arenaHolograms.put(arenaId, newHolo.getId());
                }
            } catch (Exception e) {
                EldaniorLogger.error("DynamicHolo.update", e);
            }
        }
    }

    /**
     * Construit les lignes de texte pour l'hologramme d'arene.
     */
    private static List<String> buildArenaLines(String arenaId, String arenaName) {
        List<String> lines = new ArrayList<>();

        // Titre
        lines.add("§6§l⚔ " + arenaName.replace('_', ' ') + " ⚔");
        lines.add("§7— CLASSEMENT ARENE —");
        lines.add(" ");

        // Top 5
        List<Map.Entry<String, ArenaManager.ArenaStats>> top =
                ArenaManager.getTopPlayers(arenaId, TOP_PLAYERS);

        if (top.isEmpty()) {
            lines.add("§8Aucun combattant");
        } else {
            for (int i = 0; i < top.size(); i++) {
                Map.Entry<String, ArenaManager.ArenaStats> entry = top.get(i);
                String color = i < RANK_COLORS.length ? RANK_COLORS[i] : "§8";
                String medal = switch (i) {
                    case 0 -> "§6①";
                    case 1 -> "§f②";
                    case 2 -> "§c③";
                    default -> "§7" + (i + 1) + ".";
                };

                ArenaManager.ArenaStats stats = entry.getValue();
                String ratio = String.format("%.1f", stats.getRatio());

                lines.add(medal + " " + color + entry.getKey() +
                        " §7— " + stats.kills + "K/" + stats.deaths + "D §8(" + ratio + ")");
            }
        }

        lines.add(" ");
        lines.add("§8Mis a jour toutes les 60s");

        return lines;
    }

    /**
     * Demarre le timer de mise a jour automatique.
     */
    public static void startUpdateTimer() {
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                updateAll();
            } catch (Exception e) {
                EldaniorLogger.error("DynamicHolo.timer", e);
            }
        }, 60, 60, TimeUnit.SECONDS);

        EldaniorLogger.info("[DynamicHolo] Timer de mise a jour demarre (60s)");
    }

    /**
     * Cree automatiquement un hologramme pour chaque arene existante.
     * Appele au demarrage si besoin.
     */
    public static void autoCreateForAllArenas() {
        for (ParcelData p : ParcelManager.getAll()) {
            if (p.getType() != ParcelType.ARENA) continue;
            if (arenaHolograms.containsKey(p.getId())) continue;

            // Positionner l'hologramme au centre de l'arene, 3 blocs au-dessus
            double cx = (p.getMinX() + p.getMaxX()) / 2.0;
            double cy = p.getMaxY() + 3;
            double cz = (p.getMinZ() + p.getMaxZ()) / 2.0;

            createArenaHologram(p.getId(), cx, cy, cz, p.getWorld());
        }
    }

    public static int getCount() {
        return arenaHolograms.size();
    }
}
