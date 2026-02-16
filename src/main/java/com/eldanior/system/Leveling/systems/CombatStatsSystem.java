package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.StatConfig;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CombatStatsSystem extends DamageEventSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    // Configurable ici : Multiplicateur de dégâts critique (1.8 = +80% dégâts)
    private static final float CRIT_MULTIPLIER = 1.8f;

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                       @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer,
                       @Nonnull Damage damage) {

        if (damage.isCancelled()) return;

        // 1. Gestion de l'Attaquant (Bonus Force + Critique)
        applyOffensiveStats(damage, store);

        // 2. Gestion de la Victime (Réduction Endurance)
        applyEnduranceDefense(index, archetypeChunk, store, damage);
    }

    private void applyOffensiveStats(Damage damage, Store<EntityStore> store) {
        Damage.Source source = damage.getSource();

        // On vérifie si la source est une entité vivante
        if (!(source instanceof Damage.EntitySource entitySource)) return;

        Ref<EntityStore> attackerRef = entitySource.getRef();
        if (!attackerRef.isValid()) return;

        // Récupération des données de l'attaquant
        PlayerLevelData attackerData = store.getComponent(attackerRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (attackerData == null) return;

        ClassModel attackerClass = ClassManager.get(attackerData.getPlayerClassId());

        // --- A. BONUS DE FORCE ---
        // Utilise StatConfig : Points * 0.032
        float strengthBonus = StatConfig.STRENGTH_DAMAGE.getFinalValue(attackerData, attackerClass);

        float currentDamage = damage.getAmount();
        currentDamage += strengthBonus;

        // --- B. COUP CRITIQUE ---
        // Utilise LuckSystem (qui utilise StatConfig.LUCK_CRITICAL)
        if (LuckSystem.isCriticalHit(attackerData)) {
            currentDamage *= CRIT_MULTIPLIER;
            LOGGER.atSevere().log("Coup Critique Donnée ===> ", currentDamage);
            // Optionnel : Ajouter un effet visuel ou sonore ici plus tard
        }

        damage.setAmount(currentDamage);
    }

    private void applyEnduranceDefense(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, Damage damage) {
        Ref<EntityStore> victimRef = chunk.getReferenceTo(index);
        if (!victimRef.isValid()) return;

        // Récupération des données de la victime
        PlayerLevelData victimData = store.getComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (victimData == null) return;

        ClassModel victimClass = ClassManager.get(victimData.getPlayerClassId());

        // --- C. DEFENSE ENDURANCE ---
        // Utilise StatConfig : Points * 0.3
        float defense = StatConfig.ENDURANCE_DEFENSE.getFinalValue(victimData, victimClass);

        float currentDamage = damage.getAmount();
        currentDamage -= defense;

        // On s'assure qu'on ne soigne pas l'ennemi en tapant (minimum 1 dégât)
        if (currentDamage < 1) currentDamage = 1;

        damage.setAmount(currentDamage);
    }

    // --- Partie Technique Hytale ---
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public SystemGroup<EntityStore> getGroup() {
        try {
            Class<?> mod = Class.forName("com.hypixel.hytale.server.core.modules.entity.damage.DamageModule");
            Object inst = mod.getMethod("get").invoke(null);
            return (SystemGroup<EntityStore>) mod.getMethod("getFilterDamageGroup").invoke(inst);
        } catch (Throwable e) {
            LOGGER.atSevere().log("Erreur critique dans CombatStatsSystem : " + e.getMessage());
            return null;
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}