package com.eldanior.system.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
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
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class CombatStatsSystem extends DamageEventSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Random random = new Random();

    @Override
    public void handle(int index, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer, Damage damage) {

        if (damage.isCancelled()) return;

        Ref<EntityStore> victimRef = archetypeChunk.getReferenceTo(index);
        if (!victimRef.isValid()) return;

        float originalDamage = damage.getAmount();
        float currentDamage = originalDamage;

        // --- 1. GESTION DE L'ATTAQUANT (FORCE & CHANCE) ---
        Damage.Source source = damage.getSource();

        if (source instanceof Damage.EntitySource entitySource) {
            Ref<EntityStore> attackerRef = entitySource.getRef();

            if (attackerRef.isValid()) {
                PlayerLevelData attackerData = store.getComponent(attackerRef, EldaniorSystem.get().getPlayerLevelDataType());

                if (attackerData != null) {

                    // --- NOUVELLE FORMULE (CIBLE : ~12 Dégâts pour 18 Force) ---
                    // (Force * 0.65) + (Niveau * Force * 0.02)
                    float strengthBonus = (float) ((attackerData.getStrength() * 0.50) + (attackerData.getLevel() * attackerData.getStrength() * 0.05));

                    currentDamage += strengthBonus;

                    // Message de debug
                    if (strengthBonus > 0) {
                        // LOGGER.atInfo().log("Bonus Force: +" + strengthBonus);
                    }

                    // CALCUL CHANCE : Critique
                    float critChance = attackerData.getLuck() * 0.5f;
                    if (random.nextFloat() * 100 < critChance) {
                        currentDamage *= 1.5f;
                    }
                }
            }
        }

        // --- 2. GESTION DE LA VICTIME (ENDURANCE) ---
        PlayerLevelData victimData = store.getComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType());

        if (victimData != null) {
            // J'ai aussi réduit un peu l'Endurance pour équilibrer avec la baisse de force
            // Formule : (End * 0.3) au lieu de 0.5
            float defense = (float) ((victimData.getEndurance() * 0.3) + (victimData.getLevel() * victimData.getEndurance() * 0.05));
            currentDamage -= defense;
            if (currentDamage < 1) currentDamage = 1;
        }

        // --- 3. APPLICATION ---
        if (Math.abs(currentDamage - originalDamage) > 0.01f) {
            damage.setAmount(currentDamage);
        }
    }

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        try {
            Class<?> mod = Class.forName("com.hypixel.hytale.server.core.modules.entity.damage.DamageModule");
            Object inst = mod.getMethod("get").invoke(null);
            return (SystemGroup<EntityStore>) mod.getMethod("getFilterDamageGroup").invoke(inst);
        } catch (Throwable e) {
            LOGGER.atSevere().log("Erreur lors de la récupération du Groupe de Dégâts : " + e.getMessage());
            return null;
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}