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

        // Famille royale
        register(new Eldanior());

        // Familles Marquis
        register(new Zippel());
        register(new Runkandel());
        register(new Luminara());
        register(new Valmontis());

        // Familles Duc (8 familles — 2 par Marquisat)
        // Guerriers
        register(new Ironveil());       // Guerrier — volonte de fer
        register(new Warbane());        // Guerrier — heritage de guerre
        // Mages
        register(new Frostguard());     // Mage — resilience du givre
        register(new Spellweave());     // Mage — maitrise arcanique
        // Assassin
        register(new Nighthollow());    // Assassin — frappe de l'ombre
        // Archer
        register(new Swiftquiver());    // Archer — precision mortelle
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
        return families.get(id);
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
