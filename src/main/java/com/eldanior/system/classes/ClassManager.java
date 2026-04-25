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
        register(new DragonAncestral());

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
        register(new com.eldanior.system.classes.definitions.warrior.Protecteur());
        register(new com.eldanior.system.classes.definitions.warrior.Berserker());
        register(new com.eldanior.system.classes.definitions.warrior.Duelliste());
        register(new com.eldanior.system.classes.definitions.warrior.Paladin());
        register(new com.eldanior.system.classes.definitions.warrior.Gladiateur());
        register(new com.eldanior.system.classes.definitions.warrior.Samourai());
        register(new com.eldanior.system.classes.definitions.warrior.Warlord());
        register(new com.eldanior.system.classes.definitions.warrior.ChevalierNoir());
        register(new com.eldanior.system.classes.definitions.warrior.Croise());

        // Mage evolutions (27)
        register(new com.eldanior.system.classes.definitions.mage.Elementaliste());
        register(new com.eldanior.system.classes.definitions.mage.Enchanteur());
        register(new com.eldanior.system.classes.definitions.mage.Necromancien());
        register(new com.eldanior.system.classes.definitions.mage.Invocateur());
        register(new com.eldanior.system.classes.definitions.mage.Guerisseur());
        register(new com.eldanior.system.classes.definitions.mage.Pyromancien());
        register(new com.eldanior.system.classes.definitions.mage.Cryomancien());
        register(new com.eldanior.system.classes.definitions.mage.Archimage());
        register(new com.eldanior.system.classes.definitions.mage.Sorcier());
        register(new com.eldanior.system.classes.definitions.mage.Druide());
        register(new com.eldanior.system.classes.definitions.mage.Illusionniste());
        register(new com.eldanior.system.classes.definitions.mage.Mystique());
        register(new com.eldanior.system.classes.definitions.mage.Thaumaturge());
        register(new com.eldanior.system.classes.definitions.mage.Alchimiste());
        register(new com.eldanior.system.classes.definitions.mage.Sage());
        register(new com.eldanior.system.classes.definitions.mage.Magus());
        register(new com.eldanior.system.classes.definitions.mage.Liche());
        register(new com.eldanior.system.classes.definitions.mage.Oracle());
        register(new com.eldanior.system.classes.definitions.mage.MaitreElementaire());
        register(new com.eldanior.system.classes.definitions.mage.Chronoturge());
        register(new com.eldanior.system.classes.definitions.mage.Archonte());
        register(new com.eldanior.system.classes.definitions.mage.MageVoid());
        register(new com.eldanior.system.classes.definitions.mage.Primordial());
        register(new com.eldanior.system.classes.definitions.mage.Demiurge());
        register(new com.eldanior.system.classes.definitions.mage.Prophete());
        register(new com.eldanior.system.classes.definitions.mage.AvatarArcanique());
        register(new com.eldanior.system.classes.definitions.mage.DieuDesArcanes());

        // Assassin evolutions (27)
        register(new com.eldanior.system.classes.definitions.assassin.Voleur());
        register(new com.eldanior.system.classes.definitions.assassin.Rodeur());
        register(new com.eldanior.system.classes.definitions.assassin.Sicaire());
        register(new com.eldanior.system.classes.definitions.assassin.Eclaireur());
        register(new com.eldanior.system.classes.definitions.assassin.Empoisonneur());
        register(new com.eldanior.system.classes.definitions.assassin.Saboteur());
        register(new com.eldanior.system.classes.definitions.assassin.Acrobate());
        register(new com.eldanior.system.classes.definitions.assassin.MaitreLame());
        register(new com.eldanior.system.classes.definitions.assassin.OmbreFurtive());
        register(new com.eldanior.system.classes.definitions.assassin.Chasseur());
        register(new com.eldanior.system.classes.definitions.assassin.Ninja());
        register(new com.eldanior.system.classes.definitions.assassin.Espion());
        register(new com.eldanior.system.classes.definitions.assassin.Corsaire());
        register(new com.eldanior.system.classes.definitions.assassin.Traqueur());
        register(new com.eldanior.system.classes.definitions.assassin.LameNoire());
        register(new com.eldanior.system.classes.definitions.assassin.MaitreOmbre());
        register(new com.eldanior.system.classes.definitions.assassin.Faucheur());
        register(new com.eldanior.system.classes.definitions.assassin.PhantomBlade());
        register(new com.eldanior.system.classes.definitions.assassin.GrandMaitrePoison());
        register(new com.eldanior.system.classes.definitions.assassin.AngeDechu());
        register(new com.eldanior.system.classes.definitions.assassin.Spectre());
        register(new com.eldanior.system.classes.definitions.assassin.SeigneurPoison());
        register(new com.eldanior.system.classes.definitions.assassin.LameEternelle());
        register(new com.eldanior.system.classes.definitions.assassin.OmbreSupreme());
        register(new com.eldanior.system.classes.definitions.assassin.EmpereurDesOmbres());
        register(new com.eldanior.system.classes.definitions.assassin.AvatarDuNeant());
        register(new com.eldanior.system.classes.definitions.assassin.DieuDesOmbres());

        // Archer evolutions (15)
        register(new com.eldanior.system.classes.definitions.archer.Tireur());
        register(new com.eldanior.system.classes.definitions.archer.Chasseur());
        register(new com.eldanior.system.classes.definitions.archer.Arbaletrier());
        register(new com.eldanior.system.classes.definitions.archer.Eclaireur());
        register(new com.eldanior.system.classes.definitions.archer.Franc_Tireur());
        register(new com.eldanior.system.classes.definitions.archer.RangerElite());
        register(new com.eldanior.system.classes.definitions.archer.ArcMystique());
        register(new com.eldanior.system.classes.definitions.archer.MaitreChasse());
        register(new com.eldanior.system.classes.definitions.archer.SniperDivin());
        register(new com.eldanior.system.classes.definitions.archer.TireurElementaire());
        register(new com.eldanior.system.classes.definitions.archer.GeneralArcher());
        register(new com.eldanior.system.classes.definitions.archer.OeilDeFaucon());
        register(new com.eldanior.system.classes.definitions.archer.ArcAncien());
        register(new com.eldanior.system.classes.definitions.archer.AvatarDeLArc());
        register(new com.eldanior.system.classes.definitions.archer.DieuDeLArc());
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
        register(new com.eldanior.system.classes.definitions.marchand.Negociant());
        register(new com.eldanior.system.classes.definitions.marchand.Caravanier());
        register(new com.eldanior.system.classes.definitions.marchand.Prospecteur());
        register(new com.eldanior.system.classes.definitions.marchand.Banquier());
        register(new com.eldanior.system.classes.definitions.marchand.MarchandDivin());
        register(new com.eldanior.system.classes.definitions.marchand.RoiDuCommerce());


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