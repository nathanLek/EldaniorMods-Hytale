package com.eldanior.system;

import com.eldanior.system.commands.ESCommand;
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.components.SkillItemComponent;
import com.eldanior.system.items.Interaction.SkillInteractions;
import com.eldanior.system.rpg.classes.ClassManager;
import com.eldanior.system.rpg.classes.skills.Skills.SkillManager;
import com.eldanior.system.rpg.classes.skills.Skills.SkillSystem;
import com.eldanior.system.rpg.classes.skills.system.SkillsSystem;
import com.eldanior.system.systems.*;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
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

    private static final com.hypixel.hytale.codec.KeyedCodec<String> SKILL_ID_KEY =
            new com.hypixel.hytale.codec.KeyedCodec<>("SkillId", com.hypixel.hytale.codec.Codec.STRING);

    private final Map<UUID, UUID> lastAttackers = new ConcurrentHashMap<>();
    private final Map<UUID, java.util.List<com.hypixel.hytale.server.core.inventory.ItemStack>> persistentItems = new ConcurrentHashMap<>();

    public EldaniorSystem(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log(">>> ELDANIOR SYSTEM : Initialisation... <<<");

        // 1. INIT MANAGERS
        try {
            ClassManager.init();
            SkillManager.init();
            LOGGER.atInfo().log("- RPG Managers initialisés !");
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("ERREUR Managers");
        }

        // 2. ENREGISTREMENT COMPOSANTS (ECS)
        try {
            this.playerLevelDataType = this.getEntityStoreRegistry().registerComponent(
                    PlayerLevelData.class, "PlayerLevelData", PlayerLevelData.CODEC);

            this.getEntityStoreRegistry().registerComponent(
                    SkillItemComponent.class, "SkillItem", SkillItemComponent.CODEC);

            this.getCodecRegistry(Interaction.CODEC).register("SkillToggle", SkillInteractions.class, SkillInteractions.CODEC);

            LOGGER.atInfo().log("- Composants ECS enregistrés !");
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("ERREUR Composants");
        }

        // 3. ENREGISTREMENT COMMANDES
        this.getCommandRegistry().registerCommand(new ESCommand());

        // 4. ENREGISTREMENT SYSTÈMES ET EVENTS
        try {
            this.getEntityStoreRegistry().registerSystem(new CombatTrackerSystem());
            this.getEntityStoreRegistry().registerSystem(new CombatStatsSystem());
            this.getEntityStoreRegistry().registerSystem(new EnduranceSystem());
            this.getEntityStoreRegistry().registerSystem(new ManaSystem());
            this.getEntityStoreRegistry().registerSystem(new HealthRegenSystem());
            this.getEntityStoreRegistry().registerSystem(new FallDamageSystem());
            this.getEntityStoreRegistry().registerSystem(new SpeedSystem());
            this.getEntityStoreRegistry().registerSystem(new SkillSystem());
            this.getEntityStoreRegistry().registerSystem(new DeathXPSystem());
            this.getEntityStoreRegistry().registerSystem(new SkillsSystem());
            this.getEntityStoreRegistry().registerSystem(new SkillItemDropSystem());

            LOGGER.atInfo().log("- Systèmes ECS activés !");
        } catch (Exception e) {
            LOGGER.atSevere().log("Erreur enregistrement systèmes : " + e.getMessage());
        }

        LOGGER.atInfo().log(">>> ELDANIOR SYSTEM PRÊT <<<");
    }

    public static EldaniorSystem get() {
        return instance;
    }

    public ComponentType<EntityStore, PlayerLevelData> getPlayerLevelDataType() {
        return playerLevelDataType;
    }

    public Map<UUID, UUID> getLastAttackers() {
        return lastAttackers;
    }

    public static KeyedCodec<String> getSkillIdKey() {
        return SKILL_ID_KEY;
    }

    public Map<UUID, java.util.List<com.hypixel.hytale.server.core.inventory.ItemStack>> getPersistentItems() {
        return persistentItems;
    }
}