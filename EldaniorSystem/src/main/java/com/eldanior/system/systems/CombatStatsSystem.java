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

@SuppressWarnings({"unchecked", "deprecation"})
public class CombatStatsSystem extends DamageEventSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Random random = new Random();

    @Override
    public void handle(int index, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer, Damage damage) {
        if (damage.isCancelled()) return;

        // 1. FORCE & CHANCE (Attaquant)
        applyOffensiveStats(damage, store);

        // 2. ENDURANCE (Défenseur)
        // C'est ici que l'Endurance agit comme bouclier !
        applyEnduranceDefense(index, archetypeChunk, store, damage);
    }

    // Gère la FORCE (Dégâts) et la CHANCE (Critique)
    private void applyOffensiveStats(Damage damage, Store<EntityStore> store) {
        Damage.Source source = damage.getSource();
        if (source instanceof Damage.EntitySource entitySource) {
            Ref<EntityStore> attackerRef = entitySource.getRef();
            if (attackerRef.isValid()) {
                PlayerLevelData attackerData = store.getComponent(attackerRef, EldaniorSystem.get().getPlayerLevelDataType());
                if (attackerData != null) {
                    float currentDamage = damage.getAmount();

                    // 3000 pts * 0.032 = +96 Dégâts (+5 base = 101 Dégâts)
                    PlayerLevelData data = new PlayerLevelData();
                    float strengthBonus = data.getStrength() * 0.032f;
                    currentDamage += strengthBonus;

                    damage.setAmount(currentDamage);

                    // CHANCE (Critique)
                    if (LuckSystem.isCriticalHit(data)) {
                        damage.setAmount(currentDamage * 1.8f);
                    }

                    damage.setAmount(currentDamage);
                }
            }
        }
    }

    // Gère l'ENDURANCE (Réduction de dégâts)
    private void applyEnduranceDefense(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, Damage damage) {
        Ref<EntityStore> victimRef = chunk.getReferenceTo(index);
        if (!victimRef.isValid()) return;

        PlayerLevelData victimData = store.getComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (victimData != null) {
            float currentDamage = damage.getAmount();

            // --- ENDURANCE ---
            // Formule : Chaque point d'Endurance réduit les dégâts de 0.3
            float defense = (float) (victimData.getEndurance() * 0.3);

            // Application de la réduction
            currentDamage -= defense;

            // Minimum 1 dégât
            if (currentDamage < 1) currentDamage = 1;

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
            LOGGER.atSevere().log("Erreur CombatStatsSystem : " + e.getMessage());
            return null;
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}