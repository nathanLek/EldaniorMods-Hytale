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

@SuppressWarnings({"unchecked", "deprecation"})
public class FallDamageSystem extends DamageEventSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void handle(int index, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer, Damage damage) {

        if (damage == null) return;

        // --- ASTUCE : Vérification par le Nom ---
        Damage.Source source = damage.getSource();
        if (source == null) return;

        // On regarde si le mot "Fall" est dans le nom de la source de dégâts
        String sourceName = source.getClass().getSimpleName();
        if (!sourceName.contains("Fall")) {
            return; // Ce n'est pas une chute
        }

        // --- Calcul ---
        Ref<EntityStore> victimRef = archetypeChunk.getReferenceTo(index);
        if (!victimRef.isValid()) return;

        PlayerLevelData data = store.getComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType());

        if (data != null) {
            float damageAmount = damage.getAmount();

            // Objectif : 100% au lvl 999 (3000 pts)
            // 3000 * 0.00034 = 1.02 (102%)
            float reductionPercent = data.getAgility() * 0.00034f;

            if (reductionPercent > 1.0f) reductionPercent = 1.0f;

            float newDamage = damageAmount * (1.0f - reductionPercent);

            if (Math.abs(newDamage - damageAmount) > 0.01f) {
                damage.setAmount(newDamage);
            }
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
            return null;
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}