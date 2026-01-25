package com.eldanior.system.rpg.classes;

import com.eldanior.system.rpg.classes.definitions.*;
import java.util.HashMap;
import java.util.Map;

public class ClassManager {

    // Stockage en mémoire des classes RPG
    private static final Map<String, ClassModel> classes = new HashMap<>();

    public static void init() {
        // --- CORRECTION ---
        // On n'enregistre pas des "Systèmes" Hytale ici.
        // On remplit simplement notre liste (Map) avec nos objets Java.

        register(new Warrior());
        register(new Mage());
        register(new Assassin());
        register(new Archer());
        register(new Merchant());

        // Ta classe spéciale
        register(new AncestralDragon());

        // Log de debug
        System.out.println("[Eldanior] " + classes.size() + " classes RPG chargees.");
    }

    // Ajoute la classe dans la Map
    public static void register(ClassModel model) {
        if (model != null) {
            classes.put(model.getId(), model);
        }
    }

    // Récupère une classe par son ID (ex: "warrior")
    public static ClassModel get(String id) {
        return classes.get(id);
    }

    // Récupère une classe par son Nom d'affichage (ex: "Guerrier")
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