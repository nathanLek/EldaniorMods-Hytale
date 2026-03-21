package com.eldanior.system.classes;

import com.eldanior.system.classes.definitions.*;
import com.eldanior.system.classes.definitions.marchand.*;
import com.eldanior.system.classes.definitions.warrior.*;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;

import java.util.*;

public class ClassManager {

    private static final Map<String, ClassModel> classes = new HashMap<>();
    private static final Set<String> evolutionIds = new HashSet<>();

    public static void init() {
        register(new Novice());

        register(new Warrior());
        register(new Mage());
        register(new Assassin());
        register(new Archer());
        register(new Merchant());

        register(new Templier());
        register(new Epeiste());
        register(new Fantassin());
        register(new Brute());
        register(new Mercenaire());
        register(new AvantGarde());
        register(new Ravageur());
        register(new Bretteur());
        register(new LameMage());
        register(new Champion());
        register(new Colosse());
        register(new Executeur());
        register(new GardienRunique());
        register(new Titan());
        register(new Heros());
        register(new Fleau());
        register(new SangDragon());
        register(new DivineApotre());

        register(new BlackMarketPrince());
        register(new GoldBaron());
        register(new GuildMaster());
        register(new MasterArtisan());
        register(new ProsperityAvatar());
        register(new RelicHunter());
        register(new Smuggler());
        register(new WorldForger());
        register(new UnderworldKing());


        for (ClassModel model : classes.values()) {
            if (model.getType() != ClassType.NOVICE) {
                evolutionIds.addAll(model.getNextClassId());
            }
        }

        System.out.println("[Eldanior] Evolution IDs détectés : " + evolutionIds);
        System.out.println("[Eldanior] " + classes.size() + " classes RPG chargees.");
    }

    public static boolean isEvolution(String id) {
        return evolutionIds.contains(id);
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