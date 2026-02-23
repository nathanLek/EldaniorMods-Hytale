package com.eldanior.system.config.configs.Mobs;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MobDamageReductionSystem extends DamageEventSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    // Ecart de niveau à partir duquel la réduction est totalement annulée (joueur >> mob)
    private static final float MAX_LEVEL_GAP = 100f;
    // Dégâts minimum garantis si écart > -MAX_LEVEL_GAP
    private static final float MIN_DAMAGE = 1f;

    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getFilterDamageGroup();
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                       @Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> commandBuffer,
                       @Nonnull Damage damage) {

        Ref<EntityStore> targetRef = chunk.getReferenceTo(index);
        Damage.Source source = damage.getSource();

        if (!(source instanceof Damage.EntitySource entitySource)) return;

        Ref<EntityStore> attackerRef = entitySource.getRef();
        Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());

        ComponentType<EntityStore, MobLevelData> mobLevelType = EldaniorSystem.get().getMobLevelDataType();

        // CAS 1 : Joueur attaque un mob
        if (attackerPlayer != null) {
            MobLevelData mobData = store.getComponent(targetRef, mobLevelType);
            if (mobData == null || !mobData.isStatsApplied()) return;

            PlayerLevelData playerData = store.getComponent(attackerRef, EldaniorSystem.get().getPlayerLevelDataType());
            int playerLevel = (playerData != null) ? playerData.getLevel() : 1;
            int mobLevel = mobData.getLevel();

            float baseHP = getBaseHP(store, targetRef);
            float multiplier = MobVirtualHPSystem.getHPMultiplier(mobLevel, baseHP);
            float originalDamage = damage.getAmount();

            float finalDamage = computeFinalDamage(originalDamage, multiplier, playerLevel, mobLevel);
            damage.setAmount(finalDamage);

            LOGGER.atInfo().log("[DamageReduction] Lv." + playerLevel + " vs " + mobData.getMobTypeId()
                    + " Lv." + mobLevel + " | Brut:" + originalDamage + " → Final:" + finalDamage
                    + " (multiplier:" + multiplier + ")");
        }

        // CAS 2 : Mob attaque
        else {
            MobLevelData mobData = store.getComponent(attackerRef, mobLevelType);
            if (mobData == null || !mobData.isStatsApplied()) return;

            float damageBonus = mobData.getLevel() * com.eldanior.system.config.configs.MobsWorldConfig.DAMAGE_PER_LEVEL;
            damage.setAmount(damage.getAmount() + damageBonus);
        }
    }

    private float computeFinalDamage(float originalDamage, float multiplier, int playerLevel, int mobLevel) {
        float levelGap = playerLevel - mobLevel;

        // Joueur très supérieur (+100 niveaux) → aucune réduction
        if (levelGap >= MAX_LEVEL_GAP) {
            return originalDamage;
        }

        // Joueur supérieur (gap positif) → réduction atténuée proportionnellement
        // Joueur inférieur ou égal (gap <= 0) → réduction normale
        float reductionFactor;
        if (levelGap > 0) {
            reductionFactor = 1.0f - (levelGap / MAX_LEVEL_GAP);
        } else {
            reductionFactor = 1.0f;
        }

        // Diviseur effectif : entre 1.0 (aucune réduction) et multiplier (réduction pleine)
        float effectiveDivisor = 1.0f + (multiplier - 1.0f) * reductionFactor;
        float reducedDamage = originalDamage / effectiveDivisor;

        // Minimum 1 dégât garanti sauf si joueur très inférieur (-100 niveaux ou plus)
        if (levelGap > -MAX_LEVEL_GAP && reducedDamage < MIN_DAMAGE) {
            return MIN_DAMAGE;
        }

        return reducedDamage;
    }

    private float getBaseHP(Store<EntityStore> store, Ref<EntityStore> mobRef) {
        EntityStatMap statMap = store.getComponent(mobRef,
                EntityStatsModule.get().getEntityStatMapComponentType());

        if (statMap != null) {
            int healthIndex = DefaultEntityStatTypes.getHealth();
            if (statMap.get(healthIndex) != null) {
                return Objects.requireNonNull(statMap.get(healthIndex)).getMax();
            }
        }
        return 100.0f;
    }
}