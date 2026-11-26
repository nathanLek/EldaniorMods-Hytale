package com.eldanior.system.territory.teleport;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.FamilyRole;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Reseau des Portails Continentaux (2026-11-18/19) : un seul par Marquisat (Grand Territoire), bati par un admin
 * (ContinentalGates). Le Marquis l'ouvre ou le ferme et fixe le prix du voyage chez son intendant (Domaines > fiche du Marquisat >
 * Regles du domaine > Teleporteur Continental). Voyage payant pour tout le monde (decision de l'utilisateur), paye au Gardien,
 * verse au tresor du Marquisat de depart ; destinations : les autres portails ouverts du meme monde.
 * Le petit bloc teleporteur du 2026-11-18 a ete supprime le 2026-11-19 (« il ne sert plus a rien »).
 * Donnees dans les lois du Marquisat (ParcelData.law) : tp (ouvert), tpPrice, tpAt (« monde|x|y|z|gate|dir »), tpNpc.
 */
public final class ContinentalTeleporters {

    private ContinentalTeleporters() {}

    public static final long DEFAULT_PRICE = 1_000;

    /** Portail d'un Marquisat : centre de la plateforme (x, y, z), dir = axe de passage 0..3. */
    public record Spot(String world, int x, int y, int z, boolean gate, int dir) {
        /** Axe de passage (avant) et lateral, en blocs. */
        public int fx() { return new int[]{0, 1, 0, -1}[dir & 3]; }
        public int fz() { return new int[]{1, 0, -1, 0}[dir & 3]; }
        public int lx() { return -fz(); }
        public int lz() { return fx(); }
        /** Arrivee : devant le portail (cote d'entree), a 3 blocs. */
        public Vector3d arrival() { return new Vector3d(x + 0.5 - fx() * 3, y + 1.1, z + 0.5 - fz() * 3); }
        public String code() { return world + "|" + x + "|" + y + "|" + z + "|gate|" + dir; }
        static Spot parse(String s) {
            String[] p = s == null ? new String[0] : s.split("\\|");
            if (p.length != 6) return null;                                            // ancien bloc teleporteur : oublie
            try { return new Spot(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]), true, Integer.parseInt(p[5])); }
            catch (NumberFormatException e) { return null; }
        }
    }

    /** Marquisat (Grand Territoire) qui contient cette zone, elle comprise. */
    public static ParcelData marquisatOf(ParcelData p) {
        Set<String> seen = new HashSet<>();
        while (p != null && seen.add(p.getId())) {
            if (p.getType() == ParcelType.GRAND_TERRITORY) return p;
            String parent = p.getParentId();
            p = parent != null && !parent.isEmpty() ? ParcelManager.get(parent) : null;
        }
        return null;
    }

    public static ParcelData marquisatAt(String world, double x, double y, double z) {
        return marquisatOf(ParcelManager.getParcelAt(world, x, y, z));
    }

    public static Spot spot(ParcelData m) { return m != null ? Spot.parse(m.law("tpAt")) : null; }
    public static boolean active(ParcelData m) { return m != null && "1".equals(m.law("tp")); }
    public static long price(ParcelData m) { return Math.max(0, m.lawLong("tpPrice", DEFAULT_PRICE)); }

    /** Autres portails ouverts du meme monde. */
    public static List<ParcelData> destinations(ParcelData from) {
        List<ParcelData> out = new ArrayList<>();
        Spot here = spot(from);
        for (ParcelData m : ParcelManager.getByType(ParcelType.GRAND_TERRITORY)) {
            if (m.getId().equals(from.getId()) || !active(m)) continue;
            Spot s = spot(m);
            if (s != null && (here == null || s.world().equals(here.world()))) out.add(m);
        }
        out.sort(java.util.Comparator.comparing(ParcelData::getName, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    /** Le Marquis (maitre du Marquisat), le Patriarche ou le Seigneur de sa maison, un admin. */
    public static boolean canManage(ParcelData m, UUID u, PlayerLevelData data, boolean admin) {
        if (admin) return true;
        if (m == null || u == null) return false;
        if (m.isOwner(u)) return true;
        String h = m.getFamilyId().isEmpty() ? "" : FamilyManager.migrateId(m.getFamilyId());
        if (h.isEmpty() || data == null || data.getNobleFamilyId() == null || !h.equals(FamilyManager.migrateId(data.getNobleFamilyId()))) return false;
        FamilyRole r = data.isPatriarch() ? FamilyRole.PATRIARCH : data.getFamilyRole();
        return r == FamilyRole.PATRIARCH || r == FamilyRole.VICE;
    }

    /** Ouvre ou ferme le portail (le voile, lui, ne s'allume qu'apres un passage paye). */
    public static void setActive(ParcelData m, boolean on) {
        m.setLaw("tp", on ? "1" : "");
        ParcelManager.save();
    }
}
