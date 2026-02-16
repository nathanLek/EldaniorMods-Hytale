package com.eldanior.system.config.configs.Mobs;

import com.eldanior.system.EldaniorSystem;
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

import javax.annotation.Nonnull;
import java.util.Objects;

public class MobDamageReductionSystem extends DamageEventSystem {

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

        if (!(source instanceof Damage.EntitySource entitySource)) {
            return;
        }

        Ref<EntityStore> attackerRef = entitySource.getRef();
        Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());

        ComponentType<EntityStore, MobLevelData> mobLevelType =
                EldaniorSystem.get().getMobLevelDataType();

        // CAS 1 : Joueur attaque un mob avec niveau → Réduit les dégâts reçus
        if (attackerPlayer != null) {
            MobLevelData mobData = store.getComponent(targetRef, mobLevelType);

            if (mobData != null && mobData.isStatsApplied()) {
                // Récupère les HP de base du mob
                EntityStatMap statMap = store.getComponent(targetRef,
                        EntityStatsModule.get().getEntityStatMapComponentType());

                float baseHP = 100.0f; // Valeur par défaut
                if (statMap != null) {
                    int healthIndex = DefaultEntityStatTypes.getHealth();
                    if (statMap.get(healthIndex) != null) {
                        baseHP = Objects.requireNonNull(statMap.get(healthIndex)).getMax();
                    }
                }

                float multiplier = MobVirtualHPSystem.getHPMultiplier(mobData.getLevel(), baseHP);
                float originalDamage = damage.getAmount();
                float reducedDamage = originalDamage / multiplier;

                damage.setAmount(reducedDamage);

                System.out.println("🛡️ [DamageReduction] " + mobData.getMobTypeId() + " Lv." + mobData.getLevel() +
                        " (Base:" + baseHP + ") - Dégâts: " + originalDamage + " → " + reducedDamage + " (÷" + multiplier + ")");
            }
        }

        // CAS 2 : Mob attaque (joueur ou autre) → Augmente les dégâts infligés
        else {
            MobLevelData mobData = store.getComponent(attackerRef, mobLevelType);

            if (mobData != null && mobData.isStatsApplied()) {
                float damageBonus = mobData.getLevel() * com.eldanior.system.config.configs.MobsWorldConfig.DAMAGE_PER_LEVEL;
                damage.setAmount(damage.getAmount() + damageBonus);
            }
        }
    }
}