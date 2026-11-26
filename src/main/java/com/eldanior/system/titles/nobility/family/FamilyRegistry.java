package com.eldanior.system.titles.nobility.family;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.PersistenceUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registre de chaque maison, tenu par son intendant (2026-11-11 : « il n'y a pas de contenu dans la fenetre » — la liste ne
 * montrait que les membres connectes) :
 *  - les membres, connectes ou non : nom, rang, niveau, classe, derniere visite (mis a jour a la connexion et a chaque
 *    lecture des membres connectes) ;
 *  - le livre de comptes : les JOURNAL dernieres operations (depots, retraits, achats, entrees, rangs, ordres) ;
 *  - les exclusions en attente d'un membre absent (appliquees a sa connexion, PlayerLoginSystem).
 * Fichier eldanior_data/family_registry.properties : m.<famille>.<uuid> = nom|rang|niveau|classe|vu ;
 * l.<famille>.<n> = instant|texte ; k.<famille>.<uuid> = 1 ; w.<famille>.<uuid> = 1 (manuel du 1er Mouvement deja remis,
 * FamilyMovements, 2026-11-12) ; r.<famille>.<uuid> = rang a appliquer a sa connexion (2026-11-18) ;
 * a.<famille> = allocation hebdomadaire des Doyens et du Seigneur sur les coffres ; s.<famille>.<uuid> = debut|depense (semaine).
 */
public final class FamilyRegistry {

    private FamilyRegistry() {}

    public static final int JOURNAL = 40;

    public record Entry(UUID uuid, String name, String role, int level, String classId, long seen) {}

    public record Line(long at, String text) {}

    private static final Map<String, Map<UUID, Entry>> members = new ConcurrentHashMap<>();
    private static final Map<String, List<Line>> journal = new ConcurrentHashMap<>();
    private static final Map<String, java.util.Set<UUID>> pendingKicks = new ConcurrentHashMap<>();
    private static final Map<String, java.util.Set<UUID>> welcomed = new ConcurrentHashMap<>();
    /** Rangs changes pendant l'absence du membre (2026-11-18), appliques a sa connexion. */
    private static final Map<String, Map<UUID, String>> pendingRoles = new ConcurrentHashMap<>();
    /** Allocation hebdomadaire sur les coffres des Doyens et du Seigneur (2026-11-18), fixee par le Patriarche. */
    private static final Map<String, Long> allowances = new ConcurrentHashMap<>();
    /** Depenses sur les coffres par membre : {debut de la semaine, montant}. */
    private static final Map<String, Map<UUID, long[]>> spending = new ConcurrentHashMap<>();
    public static final long DEFAULT_ALLOWANCE = 5_000, WEEK_MS = 7L * 24 * 60 * 60 * 1000;
    private static Path file;

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("family_registry.properties");
        load();
    }

    // ===== Accueil : manuel du 1er Mouvement (FamilyMovements) =====

    /** true si ce membre a deja recu le manuel du 1er Mouvement de cette famille. */
    public static boolean welcomed(String familyId, UUID uuid) {
        if (familyId == null || uuid == null) return true;
        var set = welcomed.get(FamilyManager.migrateId(familyId).toLowerCase());
        return set != null && set.contains(uuid);
    }

    public static void markWelcomed(String familyId, UUID uuid) {
        if (familyId == null || uuid == null) return;
        if (welcomed.computeIfAbsent(FamilyManager.migrateId(familyId).toLowerCase(), k -> ConcurrentHashMap.newKeySet()).add(uuid)) save();
    }

    /** Serment de la voie martiale (tout est perdu, famille comprise) : une nouvelle entree redonnera le manuel. */
    public static void forgetWelcomes(UUID uuid) {
        if (uuid == null) return;
        boolean changed = false;
        for (var set : welcomed.values()) changed |= set.remove(uuid);
        if (changed) save();
    }

    // ===== Membres =====

    /** Note la presence d'un membre (connecte) de cette famille. */
    public static void touch(String familyId, UUID uuid, String name, String role, int level, String classId) {
        if (familyId == null || familyId.isEmpty() || uuid == null) return;
        String fam = FamilyManager.migrateId(familyId);
        Map<UUID, Entry> m = members.computeIfAbsent(fam, k -> new ConcurrentHashMap<>());
        Entry old = m.get(uuid);
        long now = System.currentTimeMillis();
        Entry e = new Entry(uuid, name, role == null ? "" : role, level, classId == null ? "" : classId, now);
        m.put(uuid, e);
        // une famille a la fois : le joueur quitte le registre des autres
        for (var en : members.entrySet()) if (!en.getKey().equals(fam)) en.getValue().remove(uuid);
        if (old == null || !old.role().equals(e.role()) || old.level() != e.level() || now - old.seen() > 600_000) save();
    }

    /** Inscrit un membre tel quel (joueurs fictifs d'essai, /es intendant fictifs) : rang, niveau, classe et derniere visite donnes. */
    public static void inscribe(String familyId, UUID uuid, String name, String role, int level, String classId, long seen) {
        if (familyId == null || familyId.isEmpty() || uuid == null) return;
        members.computeIfAbsent(FamilyManager.migrateId(familyId), k -> new ConcurrentHashMap<>())
                .put(uuid, new Entry(uuid, name, role == null ? "" : role, level, classId == null ? "" : classId, seen));
        save();
    }

    public static void remove(String familyId, UUID uuid) {
        if (familyId == null || uuid == null) return;
        Map<UUID, Entry> m = members.get(FamilyManager.migrateId(familyId));
        if (m != null && m.remove(uuid) != null) save();
    }

    public static void clear(String familyId) {
        if (familyId == null) return;
        String fam = FamilyManager.migrateId(familyId);
        members.remove(fam);
        save();
    }

    /** Membres inscrits, du plus haut rang au plus bas puis par nom. */
    public static List<Entry> members(String familyId) {
        Map<UUID, Entry> m = familyId == null ? null : members.get(FamilyManager.migrateId(familyId));
        List<Entry> out = m == null ? new ArrayList<>() : new ArrayList<>(m.values());
        out.sort((a, b) -> {
            int ra = rank(a.role()), rb = rank(b.role());
            return ra != rb ? Integer.compare(rb, ra) : a.name().compareToIgnoreCase(b.name());
        });
        return out;
    }

    private static int rank(String code) {
        FamilyRole r = FamilyRole.of(code);
        return r == null ? 0 : r.ordinal();
    }

    // ===== Exclusions differees =====

    public static void kickLater(String familyId, UUID uuid) {
        pendingKicks.computeIfAbsent(FamilyManager.migrateId(familyId), k -> ConcurrentHashMap.newKeySet()).add(uuid);
        remove(familyId, uuid);
        save();
    }

    /** true (et l'exclusion est consommee) si ce joueur a ete exclu de cette famille pendant son absence. */
    public static boolean consumeKick(String familyId, UUID uuid) {
        if (familyId == null || uuid == null) return false;
        var set = pendingKicks.get(FamilyManager.migrateId(familyId));
        if (set == null || !set.remove(uuid)) return false;
        save();
        return true;
    }

    // ===== Rangs differes (2026-11-18) =====

    /** Change le rang d'un membre absent : le registre l'affiche aussitot, son personnage le prend a sa connexion. */
    public static void roleLater(String familyId, UUID uuid, FamilyRole role) {
        if (familyId == null || uuid == null || role == null) return;
        String fam = FamilyManager.migrateId(familyId);
        pendingRoles.computeIfAbsent(fam, k -> new ConcurrentHashMap<>()).put(uuid, role.code());
        Map<UUID, Entry> m = members.get(fam);
        Entry e = m != null ? m.get(uuid) : null;
        if (e != null) m.put(uuid, new Entry(uuid, e.name(), role.code(), e.level(), e.classId(), e.seen()));
        save();
    }

    /** Rang en attente pour ce membre (null : aucun). */
    public static FamilyRole pendingRole(String familyId, UUID uuid) {
        var m = familyId == null || uuid == null ? null : pendingRoles.get(FamilyManager.migrateId(familyId));
        String code = m != null ? m.get(uuid) : null;
        return code != null ? FamilyRole.of(code) : null;
    }

    /** Rang en attente (consomme), applique par PlayerLoginSystem ; null : aucun. */
    public static FamilyRole consumeRole(String familyId, UUID uuid) {
        var m = familyId == null || uuid == null ? null : pendingRoles.get(FamilyManager.migrateId(familyId));
        String code = m != null ? m.remove(uuid) : null;
        if (code == null) return null;
        save();
        return FamilyRole.of(code);
    }

    // ===== Coffres : allocation hebdomadaire (2026-11-18) =====

    public static long allowance(String familyId) {
        Long a = familyId == null ? null : allowances.get(FamilyManager.migrateId(familyId));
        return a != null ? a : DEFAULT_ALLOWANCE;
    }

    public static void setAllowance(String familyId, long amount) {
        if (familyId == null) return;
        allowances.put(FamilyManager.migrateId(familyId), Math.max(0, amount));
        save();
    }

    /** Montant deja depense sur les coffres par ce membre depuis le debut de sa semaine. */
    public static long spent(String familyId, UUID uuid) {
        var m = familyId == null || uuid == null ? null : spending.get(FamilyManager.migrateId(familyId));
        long[] v = m != null ? m.get(uuid) : null;
        return v == null || System.currentTimeMillis() - v[0] > WEEK_MS ? 0 : v[1];
    }

    /** Debut de la semaine de depenses de ce membre (0 : aucune depense en cours). */
    public static long spendingSince(String familyId, UUID uuid) {
        var m = familyId == null || uuid == null ? null : spending.get(FamilyManager.migrateId(familyId));
        long[] v = m != null ? m.get(uuid) : null;
        return v == null || System.currentTimeMillis() - v[0] > WEEK_MS ? 0 : v[0];
    }

    public static void addSpent(String familyId, UUID uuid, long amount) {
        if (familyId == null || uuid == null || amount <= 0) return;
        var m = spending.computeIfAbsent(FamilyManager.migrateId(familyId), k -> new ConcurrentHashMap<>());
        long now = System.currentTimeMillis();
        long[] v = m.get(uuid);
        if (v == null || now - v[0] > WEEK_MS) v = new long[]{now, 0};
        v[1] += amount;
        m.put(uuid, v);
        save();
    }

    // ===== Livre de comptes =====

    public static void log(String familyId, String text) {
        if (familyId == null || text == null) return;
        List<Line> l = journal.computeIfAbsent(FamilyManager.migrateId(familyId), k -> new java.util.concurrent.CopyOnWriteArrayList<>());
        l.add(0, new Line(System.currentTimeMillis(), text));
        while (l.size() > JOURNAL) l.remove(l.size() - 1);
        save();
    }

    public static List<Line> journal(String familyId) {
        List<Line> l = familyId == null ? null : journal.get(FamilyManager.migrateId(familyId));
        return l == null ? List.of() : List.copyOf(l);
    }

    /** « a l'instant », « il y a 5 min », « il y a 3 h », « il y a 2 j ». */
    public static String ago(long at) {
        long s = Math.max(0, System.currentTimeMillis() - at) / 1000;
        if (s < 60) return "à l'instant";
        if (s < 3600) return "il y a " + s / 60 + " min";
        if (s < 86400) return "il y a " + s / 3600 + " h";
        return "il y a " + s / 86400 + " j";
    }

    // ===== Fichier =====

    private static synchronized void save() {
        if (file == null) return;
        try {
            if (!Files.exists(file.getParent())) Files.createDirectories(file.getParent());
            Properties p = new Properties();
            p.setProperty("_version", "1");
            for (var f : members.entrySet())
                for (Entry e : f.getValue().values())
                    p.setProperty("m." + f.getKey() + "." + e.uuid(), String.join("|", clean(e.name()), clean(e.role()),
                            String.valueOf(e.level()), clean(e.classId()), String.valueOf(e.seen())));
            for (var f : journal.entrySet()) {
                int i = 0;
                for (Line l : f.getValue()) p.setProperty("l." + f.getKey() + "." + (i++), l.at() + "|" + l.text());
            }
            for (var f : pendingKicks.entrySet())
                for (UUID u : f.getValue()) p.setProperty("k." + f.getKey() + "." + u, "1");
            for (var f : welcomed.entrySet())
                for (UUID u : f.getValue()) p.setProperty("w." + f.getKey() + "." + u, "1");
            for (var f : pendingRoles.entrySet())
                for (var r : f.getValue().entrySet()) p.setProperty("r." + f.getKey() + "." + r.getKey(), r.getValue());
            for (var a : allowances.entrySet()) p.setProperty("a." + a.getKey() + ".0", String.valueOf(a.getValue()));
            for (var f : spending.entrySet())
                for (var v : f.getValue().entrySet()) p.setProperty("s." + f.getKey() + "." + v.getKey(), v.getValue()[0] + "|" + v.getValue()[1]);
            PersistenceUtils.writeAtomicWithBackup(file, p, "Eldanior - registres des maisons (intendants)");
        } catch (Exception e) {
            EldaniorLogger.error("[Registre] sauvegarde", e);
        }
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace('|', '/');
    }

    private static void load() {
        members.clear();
        journal.clear();
        pendingKicks.clear();
        welcomed.clear();
        pendingRoles.clear();
        allowances.clear();
        spending.clear();
        if (file == null || !Files.exists(file)) return;
        try {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(file)) { p.load(in); }
            Map<String, Map<Integer, Line>> lines = new LinkedHashMap<>();
            for (String k : p.stringPropertyNames()) {
                String[] parts = k.split("\\.", 3);
                if (parts.length < 3) continue;
                String v = p.getProperty(k);
                try {
                    switch (parts[0]) {
                        case "m" -> {
                            String[] f = v.split("\\|", -1);
                            UUID u = UUID.fromString(parts[2]);
                            members.computeIfAbsent(parts[1], x -> new ConcurrentHashMap<>()).put(u, new Entry(u, f[0], f[1],
                                    Integer.parseInt(f[2]), f[3], Long.parseLong(f[4])));
                        }
                        case "l" -> {
                            int bar = v.indexOf('|');
                            lines.computeIfAbsent(parts[1], x -> new java.util.TreeMap<>())
                                    .put(Integer.parseInt(parts[2]), new Line(Long.parseLong(v.substring(0, bar)), v.substring(bar + 1)));
                        }
                        case "k" -> pendingKicks.computeIfAbsent(parts[1], x -> ConcurrentHashMap.newKeySet()).add(UUID.fromString(parts[2]));
                        case "w" -> welcomed.computeIfAbsent(parts[1], x -> ConcurrentHashMap.newKeySet()).add(UUID.fromString(parts[2]));
                        case "r" -> pendingRoles.computeIfAbsent(parts[1], x -> new ConcurrentHashMap<>()).put(UUID.fromString(parts[2]), v);
                        case "a" -> allowances.put(parts[1], Long.parseLong(v));
                        case "s" -> {
                            int bar = v.indexOf('|');
                            spending.computeIfAbsent(parts[1], x -> new ConcurrentHashMap<>()).put(UUID.fromString(parts[2]),
                                    new long[]{Long.parseLong(v.substring(0, bar)), Long.parseLong(v.substring(bar + 1))});
                        }
                        default -> { }
                    }
                } catch (Exception ignored) { }
            }
            for (var e : lines.entrySet())
                journal.put(e.getKey(), new java.util.concurrent.CopyOnWriteArrayList<>(e.getValue().values()));
        } catch (Exception e) {
            EldaniorLogger.error("[Registre] chargement", e);
        }
    }
}
