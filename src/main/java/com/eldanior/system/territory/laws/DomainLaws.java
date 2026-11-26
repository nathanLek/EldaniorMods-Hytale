package com.eldanior.system.territory.laws;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.FamilyRole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Lois d'un domaine (2026-11-18, demande de l'utilisateur : « etre plus realiste dans l'immersion »), reglees chez l'intendant
 * (Domaines > fiche > Lois) par le Patriarche, le Seigneur ou le maitre du domaine, et appliquees par DomainLawSystem :
 *  - couvre-feu (heures du jeu) : tout joueur dehors qui n'est pas chez lui, ni officier de la maison, paie une amende
 *    (decision de l'utilisateur : amende, pas d'expulsion), versee au tresor du domaine ;
 *  - frontieres : ouvertes, droit d'entree (preleve a l'entree, valable 2 jours) ou fermees (les gardes chassent les etrangers) ;
 *  - armes rengainees : une arme en main dans le domaine = amende ;
 *  - bannis : les gardes les attaquent a vue.
 * Une loi vaut pour toute la zone et ses terres vassales (geographiques) ; la plus proche du joueur l'emporte.
 */
public final class DomainLaws {

    private DomainLaws() {}

    public static final long DEFAULT_FINE = 500, DEFAULT_TOLL = 200;
    /** Validite d'un droit d'entree paye (temps reel). */
    public static final long TOLL_VALID_MS = 48L * 3600 * 1000;
    static final List<ParcelType> DOMAIN_TYPES = List.of(ParcelType.KINGDOM, ParcelType.GRAND_TERRITORY, ParcelType.TERRITORY, ParcelType.CITY);

    public enum Border {
        OPEN("", "ouvertes"), TOLL("toll", "droit d'entrée"), CLOSED("closed", "fermées");
        public final String code, label;
        Border(String code, String label) { this.code = code; this.label = label; }
        public static Border of(String code) {
            for (Border b : values()) if (b.code.equals(code)) return b;
            return OPEN;
        }
        public Border next() { return values()[(ordinal() + 1) % values().length]; }
    }

    // ==================== LECTURE ====================

    public static boolean isDomain(ParcelData p) { return p != null && DOMAIN_TYPES.contains(p.getType()); }

    /** Zones qui contiennent la position, de la plus petite a la plus grande (parents geographiques). */
    public static List<ParcelData> chain(String world, double x, double y, double z) {
        List<ParcelData> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        ParcelData p = ParcelManager.getParcelAt(world, x, y, z);
        while (p != null && seen.add(p.getId()) && out.size() < 16) {
            out.add(p);
            String parent = p.getParentId();
            p = parent != null && !parent.isEmpty() ? ParcelManager.get(parent) : null;
        }
        return out;
    }

    /** Heures du couvre-feu {debut, fin} (0-23), null s'il n'y en a pas. */
    public static int[] curfew(ParcelData d) {
        String c = d.law("curfew");
        int dash = c.indexOf('-');
        if (dash <= 0) return null;
        try {
            int a = Integer.parseInt(c.substring(0, dash)), b = Integer.parseInt(c.substring(dash + 1));
            if (a < 0 || a > 23 || b < 0 || b > 23 || a == b) return null;
            return new int[]{a, b};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean inCurfew(int[] c, int hour) {
        if (c == null) return false;
        return c[0] < c[1] ? hour >= c[0] && hour < c[1] : hour >= c[0] || hour < c[1];
    }

    public static String curfewLabel(ParcelData d) {
        int[] c = curfew(d);
        return c == null ? "aucun" : "de " + c[0] + " h à " + c[1] + " h";
    }

    public static long fine(ParcelData d) { return Math.max(1, d.lawLong("fine", DEFAULT_FINE)); }
    public static long toll(ParcelData d) { return Math.max(1, d.lawLong("toll", DEFAULT_TOLL)); }
    public static Border border(ParcelData d) { return Border.of(d.law("border")); }
    public static boolean armsBanned(ParcelData d) { return "1".equals(d.law("arms")); }

    /** Bannis : uuid -> nom. */
    public static Map<UUID, String> banned(ParcelData d) {
        Map<UUID, String> out = new LinkedHashMap<>();
        for (String part : d.law("banned").split(",")) {
            int bar = part.indexOf('|');
            if (bar <= 0) continue;
            try { out.put(UUID.fromString(part.substring(0, bar)), part.substring(bar + 1)); } catch (IllegalArgumentException ignored) { }
        }
        return out;
    }

    public static void setBanned(ParcelData d, Map<UUID, String> list) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<UUID, String> e : list.entrySet()) {
            if (sb.length() > 0) sb.append(',');
            sb.append(e.getKey()).append('|').append(e.getValue().replace(",", " ").replace("|", " "));
        }
        d.setLaw("banned", sb.toString());
    }

    /** Fin de validite du droit d'entree paye par ce joueur (0 : aucun). Les droits expires sont oublies. */
    public static long paidUntil(ParcelData d, UUID u) {
        long now = System.currentTimeMillis(), mine = 0;
        StringBuilder keep = new StringBuilder();
        boolean changed = false;
        for (String part : d.law("paid").split(",")) {
            int bar = part.indexOf('|');
            if (bar <= 0) continue;
            long end;
            try { end = Long.parseLong(part.substring(bar + 1)); } catch (NumberFormatException e) { changed = true; continue; }
            if (end <= now) { changed = true; continue; }
            if (part.substring(0, bar).equals(u.toString())) mine = end;
            if (keep.length() > 0) keep.append(',');
            keep.append(part);
        }
        if (changed) d.setLaw("paid", keep.toString());
        return mine;
    }

    public static void markPaid(ParcelData d, UUID u, long until) {
        StringBuilder sb = new StringBuilder();
        for (String part : d.law("paid").split(",")) {
            if (part.isEmpty() || part.startsWith(u.toString() + "|")) continue;
            if (sb.length() > 0) sb.append(',');
            sb.append(part);
        }
        if (sb.length() > 0) sb.append(',');
        sb.append(u).append('|').append(until);
        d.setLaw("paid", sb.toString());
    }

    // ==================== QUI EST CHEZ SOI ====================

    /** Maison d'une zone (id migre, "" sans maison). */
    public static String house(ParcelData p) {
        return p == null || p.getFamilyId().isEmpty() ? "" : FamilyManager.migrateId(p.getFamilyId());
    }

    private static String familyOf(PlayerLevelData data) {
        return data == null || data.getNobleFamilyId() == null || data.getNobleFamilyId().isEmpty() ? "" : FamilyManager.migrateId(data.getNobleFamilyId());
    }

    private static boolean under(ParcelData p, ParcelData root) {
        Set<String> seen = new HashSet<>();
        for (String id = p.getParentId(); id != null && !id.isEmpty() && seen.add(id); ) {
            if (id.equals(root.getId())) return true;
            ParcelData parent = ParcelManager.get(id);
            id = parent != null ? parent.getParentId() : null;
        }
        return false;
    }

    /** A des droits sur ce bien (proprietaire, locataire, membre) ? */
    public static boolean rights(ParcelData p, UUID u) {
        return p.isOwner(u) || p.isMember(u) || u.equals(p.getRenterUUID());
    }

    /**
     * Gens du domaine : membres de sa maison, de la maison suzeraine (qui tient une terre au-dessus) ou d'une maison vassale (qui
     * tient une terre dedans), et habitants (proprietaire, locataire ou membre d'un bien du domaine).
     */
    public static boolean insider(ParcelData d, UUID u, PlayerLevelData data) {
        if (u == null) return false;
        String fam = familyOf(data), h = house(d);
        if (rights(d, u)) return true;
        if (!fam.isEmpty()) {
            if (fam.equals(h)) return true;
            for (ParcelData p : ParcelManager.getAll()) {
                if (!isDomain(p) || !fam.equals(house(p))) continue;
                if (under(p, d) || under(d, p)) return true;
            }
        }
        for (ParcelData p : ParcelManager.getAll()) if (rights(p, u) && under(p, d)) return true;
        return false;
    }

    /** Officier de la maison du domaine (Chevalier ou plus) : il fait respecter la loi, il n'y est pas soumis. */
    public static boolean official(ParcelData d, PlayerLevelData data) {
        String fam = familyOf(data), h = house(d);
        if (fam.isEmpty() || !fam.equals(h)) return false;
        FamilyRole r = FamilyRole.effective(FamilyRole.of(data.getStatus()), NobilityRank.fromString(data.getNobilityRank()));
        return r != null && r.ordinal() >= FamilyRole.CHEVALIER.ordinal();
    }

    /** Chez soi : sur un bien (hors domaine) ou l'on a des droits, ex. sa maison, sa chambre d'auberge louee. */
    public static boolean atHome(List<ParcelData> chain, UUID u) {
        for (ParcelData p : chain) {
            if (isDomain(p)) return false;
            if (rights(p, u)) return true;
        }
        return false;
    }

    // ==================== LOI QUI S'APPLIQUE ICI ====================

    /** Domaine le plus proche dont le couvre-feu est en cours a cette heure (null : aucun). */
    public static ParcelData curfewHere(List<ParcelData> chain, int hour) {
        for (ParcelData p : chain) if (isDomain(p) && inCurfew(curfew(p), hour)) return p;
        return null;
    }

    public static ParcelData armsHere(List<ParcelData> chain) {
        for (ParcelData p : chain) if (isDomain(p) && armsBanned(p)) return p;
        return null;
    }

    /** Domaine dont ce joueur est banni, s'il s'y trouve. */
    public static ParcelData banHere(List<ParcelData> chain, UUID u) {
        for (ParcelData p : chain) if (isDomain(p) && banned(p).containsKey(u)) return p;
        return null;
    }

    /** Premier domaine (de l'interieur vers l'exterieur) dont la frontiere arrete ce joueur. */
    public static ParcelData borderHere(List<ParcelData> chain, UUID u, PlayerLevelData data) {
        for (ParcelData p : chain) {
            if (!isDomain(p) || border(p) == Border.OPEN) continue;
            if (insider(p, u, data)) continue;
            return p;
        }
        return null;
    }

    /** Arme degainee en main ? Un katana au fourreau (« _Fourreau ») et un bouclier ne comptent pas. */
    public static boolean isWeapon(String id) {
        if (id == null || id.contains("Fourreau") || id.startsWith("Weapon_Shield")) return false;
        return id.startsWith("Weapon_") || id.startsWith("Eldanior_Weapon_") || id.startsWith("Artefact_") || id.contains("_Handgun_");
    }
}
