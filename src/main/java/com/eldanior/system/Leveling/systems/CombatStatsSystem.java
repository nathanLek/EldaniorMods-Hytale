package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.components.PlayerLevelData;
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

// REMOVED: import org.jspecify.annotations.NonNull;
import javax.annotation.Nonnull; // You already had this, we will use it
import javax.annotation.Nullable;
import java.util.Random;

@SuppressWarnings({"unchecked", "deprecation"})
public class CombatStatsSystem extends DamageEventSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Random random = new Random();

    @Override
    // CHANGED: @NonNull (JSpecify) to @Nonnull (javax)
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, Damage damage) {
        if (damage.isCancelled()) return;
        applyOffensiveStats(damage, store);
        applyEnduranceDefense(index, archetypeChunk, store, damage);
    }

    private void applyOffensiveStats(Damage damage, Store<EntityStore> store) {
        Damage.Source source = damage.getSource();
        if (source instanceof Damage.EntitySource entitySource) {
            Ref<EntityStore> attackerRef = entitySource.getRef();
            if (attackerRef.isValid()) {
                PlayerLevelData attackerData = store.getComponent(attackerRef, EldaniorSystem.get().getPlayerLevelDataType());

                if (attackerData != null) {
                    float currentDamage = damage.getAmount();

                    // FIXED: Removed 'new PlayerLevelData()' and used 'attackerData'
                    float strengthBonus = attackerData.getStrength() * 0.032f;

                    currentDamage += strengthBonus;

                    // Note: You were setting damage twice in the original code.
                    // I cleaned up the flow below:
                    if (LuckSystem.isCriticalHit(attackerData)) {
                        currentDamage *= 1.8f; // Apply crit multiplier
                    }

                    damage.setAmount(currentDamage);
                }
            }
        }
    }

    private void applyEnduranceDefense(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, Damage damage) {
        Ref<EntityStore> victimRef = chunk.getReferenceTo(index);
        if (!victimRef.isValid()) return;

        PlayerLevelData victimData = store.getComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (victimData != null) {
            float currentDamage = damage.getAmount();
            float defense = (float) (victimData.getEndurance() * 0.3);
            currentDamage -= defense;

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