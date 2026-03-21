package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.WeakHashMap;

public class GlobalRegenSystem extends EntityTickingSystem<EntityStore> {

    private static final float NATIVE_DETECTION_THRESHOLD = 0.5f;
    private static final float POTION_DETECTION_THRESHOLD = 2.0f;

    private final Map<Ref<EntityStore>, Float> previousMana = new WeakHashMap<>();
    private final Map<Ref<EntityStore>, Float> previousVitality = new WeakHashMap<>();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        ComponentType<EntityStore, EntityStatMap> statMapType = EntityStatsModule.get().getEntityStatMapComponentType();
        EntityStatMap statMap = store.getComponent(entityRef, statMapType);
        if (statMap == null) return;

        PlayerLevelData playerData = store.getComponent(entityRef, EldaniorSystem.get().getPlayerLevelDataType());

        for (StatConfig config : StatConfig.values()) {
            if (config.getType() != StatConfig.StatType.ATTRIBUTE || config.getRegenRate() <= 0) continue;
            if (config == StatConfig.ENDURANCE) continue;

            EntityStatValue statValue = statMap.get(config.getStatId());
            if (statValue == null) continue;

            float current = statValue.get();
            float max = statValue.getMax();
            float ourRegen = getAmountToAdd(dt, config, max, playerData);

            if (config == StatConfig.INTELLIGENCE) {
                handleCustomManaRegen(entityRef, statMap, config, current, max, ourRegen);
                continue;
            }

            if (current > 0 && current < max) {
                if (config == StatConfig.VITALITY) {
                    float previous = previousVitality.getOrDefault(entityRef, current);
                    float delta = current - previous;
                    float targetValue;

                    if (delta < -0.001f || delta > POTION_DETECTION_THRESHOLD) {
                        targetValue = Math.min(max, current + ourRegen);
                    } else {
                        targetValue = Math.min(max, previous + ourRegen);
                    }

                    statMap.setStatValue(config.getStatId(), targetValue);
                    previousVitality.put(entityRef, targetValue);
                    continue;
                }
                statMap.setStatValue(config.getStatId(), Math.min(max, current + ourRegen));
            } else if (current >= max && config == StatConfig.VITALITY) {
                previousVitality.put(entityRef, current);
            }
        }
    }

    private void handleCustomManaRegen(Ref<EntityStore> ref, EntityStatMap statMap, StatConfig config,
                                       float current, float max, float ourRegen) {
        float previous = previousMana.getOrDefault(ref, current);
        float delta = current - previous;
        float targetValue;

        if (delta > NATIVE_DETECTION_THRESHOLD && delta < POTION_DETECTION_THRESHOLD) {
            targetValue = Math.min(max, previous + ourRegen);
        } else {
            targetValue = Math.min(max, current + ourRegen);
        }

        statMap.setStatValue(config.getStatId(), targetValue);
        previousMana.put(ref, targetValue);
    }

    private static float getAmountToAdd(float dt, StatConfig config, float max, PlayerLevelData data) {
        float rate = config.getRegenRate();
        if (data != null && data.getActivePassives() != null) {
            for (PassiveSkill skill : data.getActivePassives()) {
                if (skill.getLogic() != null) {
                    rate *= skill.getLogic().getRegenMultiplier(config);
                }
            }
        }
        return (max * rate) / 20.0f;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}