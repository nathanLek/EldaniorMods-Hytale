package com.eldanior.system.titles.nobility.family;

import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.definitions.duc.*;
import com.eldanior.system.titles.nobility.family.definitions.marquis.*;
import com.eldanior.system.titles.nobility.family.definitions.royal.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class FamilyManager {

    private static final Map<String, NobleFamilyModel> families = new ConcurrentHashMap<>();

    // Familles prises (familyId -> UUID du patriarche sous forme de String)
    private static final Set<String> takenFamilies = ConcurrentHashMap.newKeySet();

    // Donnees runtime par famille (tresorerie + contribution)
    private static final Map<String, FamilyRuntimeData> runtimeData = new ConcurrentHashMap<>();

    public static FamilyRuntimeData getRuntimeData(String familyId) {
        return runtimeData.computeIfAbsent(familyId, k -> {
            FamilyRuntimeData data = new FamilyRuntimeData();
            // Tresorerie de depart selon le rang minimum de la famille
            NobleFamilyModel family = families.get(familyId);
            if (family != null) {
                switch (family.getMinimumRank()) {
                    case ROI -> data.addTreasury(1_500_000);
                    case MARQUIS -> data.addTreasury(1_000_000);
                    case DUC -> data.addTreasury(500_000);
                    default -> {}
                }
            }
            return data;
        });
    }

    /** Charge directement les donnees runtime (appele par PersistenceManager) */
    public static void setRuntimeData(String familyId, long treasury, long contribution) {
        FamilyRuntimeData data = new FamilyRuntimeData();
        data.addTreasury(treasury);
        data.addContribution(contribution);
        runtimeData.put(familyId, data);
    }

    public static class FamilyRuntimeData {
        private final AtomicLong treasury = new AtomicLong(0);
        private final AtomicLong contribution = new AtomicLong(0);

        public long getTreasury() { return treasury.get(); }
        public void addTreasury(long amount) { treasury.addAndGet(amount); }
        public boolean withdrawTreasury(long amount) {
            long prev = treasury.getAndUpdate(c -> c >= amount ? c - amount : c);
            return prev >= amount;
        }
        public long getContribution() { return contribution.get(); }
        public void addContribution(long points) { contribution.addAndGet(points); }
    }

    public static void init() {
        // Clear toutes les donnees
        families.clear();
        takenFamilies.clear();
        runtimeData.clear();
        elders.clear();
        merchants.clear();

        // Famille royale
        register(new Eldanior());

        // Familles Marquis
        register(new Zippel());
        register(new Runkandel());
        register(new Luo());
        register(new Valmontis());

        // Familles Duc (8 familles — 2 par Marquisat)
        // Guerriers
        register(new Ironveil());       // Guerrier — volonte de fer
        register(new Warbane());        // Guerrier — heritage de guerre
        // Mages
        register(new Frostguard());     // Mage — resilience du givre
        register(new Spellweave());     // Mage — maitrise arcanique
        // Les quatre grands clans de l'Empire Luo (familles sans territoire, rang de comte ; leurs chefs sont les Doyens)
        register(new com.eldanior.system.titles.nobility.family.definitions.clan.Sama());
        register(new com.eldanior.system.titles.nobility.family.definitions.clan.Hanazakura());   // ex-Swiftquiver
        register(new com.eldanior.system.titles.nobility.family.definitions.clan.Kage());         // ex-Nighthollow
        register(new com.eldanior.system.titles.nobility.family.definitions.clan.Setsuka());
        // Marchands
        register(new Goldcrest());      // Marchand — fortune doree
        register(new Silkroad());       // Marchand — routes du commerce

        System.out.println("[Eldanior] " + families.size() + " familles nobles chargees.");
    }

    public static void register(NobleFamilyModel family) {
        if (family != null) {
            families.put(family.getId(), family);
        }
    }

    public static NobleFamilyModel get(String id) {
        return id == null ? null : families.get(migrateId(id));
    }

    /** Anciens identifiants de famille encore presents dans les sauvegardes, et leur remplacant. */
    private static final Map<String, String> RENAMED = Map.of("luminara", "luo",
            "nighthollow", "kage", "swiftquiver", "hanazakura");                      // devenus des clans le 2026-11-10

    /**
     * Identifiant a jour d'une famille lu dans une sauvegarde (joueur, parcelle, ordre, tresorerie) :
     * la famille Luminara est devenue le Clan Luo le 2026-11-05.
     */
    public static String migrateId(String id) {
        if (id == null) return null;
        return RENAMED.getOrDefault(id.toLowerCase(Locale.ROOT), id);
    }

    public static NobleFamilyModel getByDisplayName(String name) {
        for (NobleFamilyModel family : families.values()) {
            if (family.getDisplayName().equalsIgnoreCase(name)) {
                return family;
            }
        }
        return null;
    }

    public static Collection<NobleFamilyModel> getAll() {
        return families.values();
    }

    public static String getAvailableIds() {
        if (families.isEmpty()) return "AUCUNE (Erreur d'init)";
        return String.join(", ", families.keySet());
    }

    // ==================== GESTION FAMILLES PRISES ====================

    public static boolean isFamilyTaken(String familyId) {
        return takenFamilies.contains(familyId);
    }

    public static void claimFamily(String familyId) {
        takenFamilies.add(familyId);
    }

    public static void releaseFamily(String familyId) {
        takenFamilies.remove(familyId);
        // Reset runtime data (tresorerie + contribution reviennent aux valeurs par defaut)
        runtimeData.remove(familyId);
        elders.remove(familyId);
    }

    // ==================== DOYENS ====================
    // Registre des Doyens de chaque famille, connectes ou non : la limite de FamilyRole.MAX_ELDERS ne peut pas se compter
    // sur les seuls joueurs en ligne. Sauvegarde dans families.properties (<famille>.elders).

    private static final Map<String, Set<UUID>> elders = new ConcurrentHashMap<>();

    public static Set<UUID> getElders(String familyId) {
        Set<UUID> set = elders.get(familyId);
        return set == null ? Set.of() : Collections.unmodifiableSet(set);
    }

    /** Inscrit un Doyen ; false si la famille en compte deja FamilyRole.MAX_ELDERS. */
    public static synchronized boolean addElder(String familyId, UUID uuid) {
        Set<UUID> set = elders.computeIfAbsent(familyId, k -> ConcurrentHashMap.newKeySet());
        if (set.contains(uuid)) return true;
        if (set.size() >= FamilyRole.MAX_ELDERS) return false;
        set.add(uuid);
        return true;
    }

    /** Chargement : inscrit sans controle de limite. */
    public static void restoreElder(String familyId, UUID uuid) {
        elders.computeIfAbsent(familyId, k -> ConcurrentHashMap.newKeySet()).add(uuid);
    }

    public static void removeElder(String familyId, UUID uuid) {
        if (familyId == null || uuid == null) return;
        Set<UUID> set = elders.get(familyId);
        if (set != null) set.remove(uuid);
    }

    /** Retire un joueur de tous les registres de Doyens (il a quitte sa famille, ou change de rang). */
    public static void removeElderEverywhere(UUID uuid) {
        if (uuid == null) return;
        for (Set<UUID> set : elders.values()) set.remove(uuid);
    }

    // ==================== MARCHANDS AGREES ====================
    // Etrangers agrees par une famille (Clan Luo : Comptoir de la Porte du Dragon), uuid -> dernier nom connu, pour les
    // retirer et les lister hors ligne. Sauvegarde dans families.properties (<famille>.merchants = uuid:nom,...).

    private static final Map<String, Map<UUID, String>> merchants = new ConcurrentHashMap<>();

    public static Map<UUID, String> getMerchants(String familyId) {
        Map<UUID, String> map = merchants.get(familyId);
        return map == null ? Map.of() : Collections.unmodifiableMap(map);
    }

    public static boolean isMerchant(String familyId, UUID uuid) {
        Map<UUID, String> map = merchants.get(familyId);
        return map != null && uuid != null && map.containsKey(uuid);
    }

    /** Agree un marchand ; false s'il l'etait deja (le nom est tout de meme mis a jour). */
    public static boolean addMerchant(String familyId, UUID uuid, String name) {
        return merchants.computeIfAbsent(familyId, k -> new ConcurrentHashMap<>()).put(uuid, name == null ? "" : name) == null;
    }

    /** Retire l'agrement d'un marchand, par nom (insensible a la casse) ; renvoie son uuid, ou null s'il n'etait pas agree. */
    public static UUID removeMerchant(String familyId, String name) {
        Map<UUID, String> map = merchants.get(familyId);
        if (map == null || name == null) return null;
        for (Map.Entry<UUID, String> e : map.entrySet()) {
            if (e.getValue().equalsIgnoreCase(name)) {
                map.remove(e.getKey());
                return e.getKey();
            }
        }
        return null;
    }

    /**
     * Retourne les familles disponibles pour un rang donne.
     */
    public static List<NobleFamilyModel> getAvailableFamiliesForRank(NobilityRank rank) {
        List<NobleFamilyModel> available = new ArrayList<>();
        for (NobleFamilyModel family : families.values()) {
            if (!takenFamilies.contains(family.getId()) && family.getMinimumRank() == rank) {
                available.add(family);
            }
        }
        return available;
    }

    /**
     * Retourne un affichage des familles disponibles pour un rang.
     */
    public static String getAvailableFamilyIdsForRank(NobilityRank rank) {
        List<NobleFamilyModel> available = getAvailableFamiliesForRank(rank);
        if (available.isEmpty()) return "Aucune famille disponible";
        StringBuilder sb = new StringBuilder();
        for (NobleFamilyModel f : available) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(f.getId()).append(" (").append(f.getDisplayName()).append(")");
        }
        return sb.toString();
    }
}
