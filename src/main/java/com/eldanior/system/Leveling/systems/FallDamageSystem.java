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
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FallDamageSystem extends DamageEventSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                       @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer,
                       @Nonnull Damage damage) {

        // 1. Vérification rapide : Est-ce des dégâts de chute ?
        Damage.Source source = damage.getSource();
        if (!source.getClass().getSimpleName().contains("Fall")) {
            return;
        }

        // 2. Vérification de l'entité : Est-ce un joueur avec des données ?
        Ref<EntityStore> victimRef = archetypeChunk.getReferenceTo(index);
        if (!victimRef.isValid()) return;

        PlayerLevelData data = store.getComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null) return;

        // 3. Calcul de la réduction via StatConfig
        // On récupère le modèle de classe pour inclure les bonus éventuels
        ClassModel classModel = ClassManager.get(data.getPlayerClassId());

        // StatConfig gère le calcul (Agilité * Ratio) et le Cap (max 1.0)
        float reductionPercent = StatConfig.AGILITY_FALL_RESISTANCE.getFinalValue(data, classModel);

        // 4. Application de la réduction
        if (reductionPercent > 0) {
            float originalDamage = damage.getAmount();
            // Formule : Dégâts * (100% - Réduction%)
            float newDamage = originalDamage * (1.0f - reductionPercent);

            // On ne modifie que si le changement est significatif
            if (Math.abs(newDamage - originalDamage) > 0.01f) {
                // Si la réduction est de 100% (ou plus), on annule totalement les dégâts
                if (newDamage <= 0) {
                    newDamage = 0;
                }
                damage.setAmount(newDamage);
            }
        }
    }

    // --- Partie Technique Hytale (Ne pas toucher si ça fonctionne) ---
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public SystemGroup<EntityStore> getGroup() {
        try {
            Class<?> mod = Class.forName("com.hypixel.hytale.server.core.modules.entity.damage.DamageModule");
            Object inst = mod.getMethod("get").invoke(null);
            return (SystemGroup<EntityStore>) mod.getMethod("getFilterDamageGroup").invoke(inst);
        } catch (Throwable e) {
            return null;
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        // Optimisation : On ne cherche que les entités qui ont le composant Player
        // Cela évite de charger les données pour les mobs ou les items
        return Player.getComponentType();
    }
}