package com.eldanior.system;

import com.eldanior.system.commands.ESCommand;
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.rpg.classes.ClassManager; // ✅ Import
import com.eldanior.system.rpg.classes.skills.passives.SkillManager; // ✅ Import
import com.eldanior.system.rpg.classes.skills.passives.PassiveSkillSystem; // (Vérifie que le package est bon, parfois c'est system.systems ou rpg.skills.passives)
import com.eldanior.system.systems.*;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.ComponentType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EldaniorSystem extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static EldaniorSystem instance;
    private ComponentType<EntityStore, PlayerLevelData> playerLevelDataType;

    public EldaniorSystem(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    private final Map<UUID, UUID> lastAttackers = new ConcurrentHashMap<>();

    public Map<UUID, UUID> getLastAttackers() {
        return lastAttackers;
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log(">>> ELDANIOR SYSTEM : Initialisation... <<<");

        // =========================================================
        // 🚨 C'EST ICI QU'IL FALLAIT AJOUTER L'INIT ! 🚨
        // =========================================================
        try {
            ClassManager.init(); // Charge les classes (Warrior, Dragon, etc.)
            SkillManager.init(); // Charge les skills (Aura, etc.)
            LOGGER.atInfo().log("- RPG Managers (Classes & Skills) initialisés !");
        } catch (Exception e) {
            LOGGER.atSevere().log("ERREUR CRITIQUE lors du chargement des classes RPG : " + e.getMessage());
            e.printStackTrace();
        }
        // =========================================================

        // 1. Composants
        try {
            this.playerLevelDataType = this.getEntityStoreRegistry().registerComponent(
                    PlayerLevelData.class,
                    "PlayerLevelData",
                    PlayerLevelData.CODEC
            );
            LOGGER.atInfo().log("- Composant PlayerLevelData enregistré avec succès !");
        } catch (Exception e) {
            LOGGER.atSevere().log("ERREUR CRITIQUE : Impossible d'enregistrer PlayerLevelData ! " + e.getMessage());
        }

        try {
            // Enregistrement de ta commande principale
            // Assure-toi que ESCommand gère bien "setclass" et "classinfo" en sous-commandes
            // OU enregistre SetClassCommand ici si tu l'as séparée.
            this.getCommandRegistry().registerCommand(new ESCommand());

            // Si tu as séparé les commandes comme on a vu avant, ajoute-les aussi :
            // this.getCommandRegistry().registerCommand(new com.eldanior.system.commands.SetClassCommand());

            LOGGER.atInfo().log(">>> COMMANDES ENREGISTRÉES <<<");
        } catch (Exception e) {
            LOGGER.atSevere().log("ERREUR : Impossible d'enregistrer la commande ! " + e.getMessage());
        }

        try {
            // Enregistre les systèmes
            this.getEntityStoreRegistry().registerSystem(new CombatTrackerSystem());
            this.getEntityStoreRegistry().registerSystem(new CombatStatsSystem());
            this.getEntityStoreRegistry().registerSystem(new EnduranceSystem());
            this.getEntityStoreRegistry().registerSystem(new ManaSystem());
            this.getEntityStoreRegistry().registerSystem(new HealthRegenSystem());
            this.getEntityStoreRegistry().registerSystem(new FallDamageSystem());
            this.getEntityStoreRegistry().registerSystem(new SpeedSystem());

            // Le système de passifs (Régénération du Dragon)
            this.getEntityStoreRegistry().registerSystem(new PassiveSkillSystem());

            // Enregistre le détecteur de mort
            this.getEntityStoreRegistry().registerSystem(new DeathXPSystem());

            LOGGER.atInfo().log(">>> SYSTÈMES XP ACTIVÉS <<<");
        } catch (Exception e) {
            LOGGER.atSevere().log("Erreur lors de l'enregistrement des systèmes : " + e.getMessage());
        }
    }

    public static EldaniorSystem get() { return instance; }

    public ComponentType<EntityStore, PlayerLevelData> getPlayerLevelDataType() {
        return playerLevelDataType;
    }
}