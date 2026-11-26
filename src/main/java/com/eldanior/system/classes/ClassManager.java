package com.eldanior.system.classes;

import com.eldanior.system.classes.catalog.ClassCatalog;
import com.eldanior.system.classes.definitions.DragonAncestral;
import com.eldanior.system.classes.definitions.Mousquetaire;
import com.eldanior.system.classes.definitions.Novice;
import com.eldanior.system.classes.definitions.Observateur;
import com.eldanior.system.classes.definitions.luo.VoieMartiale;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;

import java.util.*;

public class ClassManager {

    /** Ordre d'enregistrement garde (cartes de la statue, listes) : bases, puis evolutions par famille et palier. */
    private static final Map<String, ClassModel> classes = new LinkedHashMap<>();
    private static final Set<String> evolutionIds = new HashSet<>();
    /** Classe -> classe dont elle est l'evolution (lignee : illustrations des cartes, ClassCards.ownArt). */
    private static final Map<String, String> parents = new HashMap<>();

    /**
     * Classes ordinaires : catalogue genere (classes.catalog.ClassCatalog, depuis scripts/classes/classes.json, refonte du
     * 2026-11-25). Classes speciales ecrites a la main : Novice, Dragon Ancestral, Observateur, Mousquetaire, voie martiale.
     */
    public static void init() {
        register(new DragonAncestral());
        register(new Observateur());                     // classe unique (Roi / Empereur Luo), jamais tiree
        register(new Novice());
        for (ClassModel m : ClassCatalog.all()) register(m);
        register(new Mousquetaire());                    // armes a feu : evolution de l'Archer (2026-11-16)
        for (ClassModel m : VoieMartiale.classes()) register(m);   // Rouleau de la Voie Martiale (Clan Luo)

        for (ClassModel model : classes.values()) {
            if (model.getType() != ClassType.NOVICE) {
                evolutionIds.addAll(model.getNextClassId());
                for (String n : model.getNextClassId()) parents.putIfAbsent(n, model.getId());
            }
        }

        System.out.println("[Eldanior] " + evolutionIds.size() + " evolutions detectees.");
        System.out.println("[Eldanior] " + classes.size() + " classes RPG chargees.");
    }

    /** Classe dont id est l'evolution, null pour une classe de base ou speciale. */
    public static String parentOf(String id) {
        return id != null ? parents.get(id) : null;
    }

    public static boolean isEvolution(String id) {
        return evolutionIds.contains(id)
                || com.eldanior.system.classes.definitions.luo.VoieMartiale.isMartial(id);   // voie martiale : jamais classe de depart
    }

    public static void register(ClassModel model) {
        if (model != null) {
            classes.put(model.getId(), model);
        }
    }

    public static Collection<ClassModel> getAll() {
        return classes.values();
    }

    public static ClassModel get(String id) {
        ClassModel cls = classes.get(id);
        if (cls == null && id != null && !id.isEmpty()) {
            System.err.println("[Eldanior] Classe introuvable: " + id + " — fallback vers novice");
            cls = classes.get("novice");
        }
        return cls;
    }

    public static ClassModel getByDisplayName(String name) {
        for (ClassModel model : classes.values()) {
            if (model.getDisplayName().equalsIgnoreCase(name)) {
                return model;
            }
        }
        return null;
    }

    public static String getAvailableIds() {
        if (classes.isEmpty()) return "AUCUNE (Erreur d'init)";
        return String.join(", ", classes.keySet());
    }


}