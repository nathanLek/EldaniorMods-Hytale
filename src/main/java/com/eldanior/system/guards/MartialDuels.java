package com.eldanior.system.guards;

import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Duels de quete contre les PNJ poses (2026-11-13, demande de l'utilisateur) : les Epreuves du Dojo ne font plus apparaitre
 * d'adversaire ; le Sensei designe un PNJ deja poste qui correspond au critere (Disciple de tel rang, Sensei Saint Martial...),
 * et le joueur va le defier (service « Duel » du PNJ). Pont public vers les postes (GuardManager) pour quest.chapitres.
 */
public final class MartialDuels {

    private MartialDuels() {}

    /** Un PNJ poste qu'on peut defier : poste, nom affiche, monde, position, rang martial (palier). */
    public record Candidate(String postId, String name, String world, Vector3d position, int tier) {}

    /** Types de PNJ qui acceptent un duel (service « Duel »). */
    public static boolean duels(GuardType t) {
        return t == GuardType.SENSEI || t == GuardType.DISCIPLE;
    }

    /** PNJ postes de ce type et de ce rang martial exact (palier), vivants. */
    public static List<Candidate> candidates(GuardType type, int tier) {
        List<Candidate> out = new ArrayList<>();
        for (GuardManager.Post p : GuardManager.getPosts()) {
            if (p.type != type || p.owner != null || MartialRanks.tier(p) != tier) continue;
            out.add(of(p));
        }
        return out;
    }

    public static Candidate of(String postId) {
        GuardManager.Post p = GuardManager.post(postId);
        return p != null ? of(p) : null;
    }

    private static Candidate of(GuardManager.Post p) {
        return new Candidate(p.id, GuardManager.nameplate(p), p.world, p.position(), MartialRanks.tier(p));
    }

    /**
     * Lieu-dit d'une position (2026-11-13 : pas de coordonnees ni de distance, un lieu proche) : « a Hanamura » dans une ville ;
     * « sur le territoire X, pres de Hanamura » dans un territoire (ville la plus proche) ; sinon « pres de X » (ville, sinon
     * territoire le plus proche) ; « loin de toute ville » s'il n'y a aucune zone nommee.
     */
    public static String placeName(String world, Vector3d at) {
        com.eldanior.system.territory.ParcelData city = null, land = null, nearCity = null, nearLand = null;
        double bestCity = Double.MAX_VALUE, bestLand = Double.MAX_VALUE;
        for (var d : com.eldanior.system.territory.ParcelManager.getAll()) {
            if (d.getName() == null || d.getName().isBlank() || (world != null && !world.equals(d.getWorld()))) continue;
            var t = d.getType();
            boolean isCity = t == com.eldanior.system.territory.ParcelType.CITY;
            boolean isLand = t == com.eldanior.system.territory.ParcelType.TERRITORY || t == com.eldanior.system.territory.ParcelType.GRAND_TERRITORY;
            if (!isCity && !isLand) continue;
            boolean inside = at.x >= d.getMinX() && at.x <= d.getMaxX() && at.z >= d.getMinZ() && at.z <= d.getMaxZ();
            double dx = Math.max(0, Math.max(d.getMinX() - at.x, at.x - d.getMaxX()));
            double dz = Math.max(0, Math.max(d.getMinZ() - at.z, at.z - d.getMaxZ()));
            double dist = Math.hypot(dx, dz);
            if (isCity) {
                if (inside) city = d;
                else if (dist < bestCity) { bestCity = dist; nearCity = d; }
            } else if (inside) {
                if (land == null || t == com.eldanior.system.territory.ParcelType.TERRITORY) land = d;   // le plus precis
            } else if (dist < bestLand) { bestLand = dist; nearLand = d; }
        }
        if (city != null) return "à " + name(city);
        if (land != null) return "sur le territoire " + name(land) + (nearCity != null ? ", près de " + name(nearCity) : "");
        if (nearCity != null) return "près de " + name(nearCity);
        return nearLand != null ? "près du territoire " + name(nearLand) : "loin de toute ville";
    }

    private static String name(com.eldanior.system.territory.ParcelData d) { return d.getName().replace('_', ' ').trim(); }

    /** Type de PNJ d'un poste (null s'il n'existe plus). */
    public static GuardType typeOf(String postId) {
        GuardManager.Post p = GuardManager.post(postId);
        return p != null ? p.type : null;
    }
}
