package com.eldanior.system.classes;

import com.eldanior.system.classes.definitions.*;
import com.eldanior.system.classes.models.ClassModel;

import java.util.HashMap;
import java.util.Map;

public class ClassManager {

    private static final Map<String, ClassModel> classes = new HashMap<>();

    public static void init() {
        register(new Warrior());
        register(new Mage());
        register(new Assassin());
        register(new Archer());
        register(new Merchant());
        System.out.println("[Eldanior] " + classes.size() + " classes RPG chargees.");
    }

    public static void register(ClassModel model) {
        if (model != null) {
            classes.put(model.getId(), model);
        }
    }

    public static ClassModel get(String id) {
        return classes.get(id);
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