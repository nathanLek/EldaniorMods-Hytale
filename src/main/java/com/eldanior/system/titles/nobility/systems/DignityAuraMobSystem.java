package com.eldanior.system.titles.nobility.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.config.configs.Mobs.MobLevelData;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;
import java.util.*;

public class DignityAuraMobSystem extends EntityTickingSystem<EntityStore> {

    private EntityEffect auraLight = null;
    private EntityEffect auraMedium = null;
    private EntityEffect auraHeavy = null;
    private EntityEffect auraStun = null;
    private boolean effectsLoaded = false;

    private void loadEffects() {
        if (effectsLoaded) return;
        effectsLoaded = true;
        try {
            var assetMap = EntityEffect.getAssetMap();

            int i1 = assetMap.getIndexOrDefault("Dignity_Aura_Light", -1);
            if (i1 >= 0) auraLight = assetMap.getAsset(i1);

            int i2 = assetMap.getIndexOrDefault("Dignity_Aura_Medium", -1);
            if (i2 >= 0) auraMedium = assetMap.getAsset(i2);

            int i3 = assetMap.getIndexOrDefault("Dignity_Aura_Heavy", -1);
            if (i3 >= 0) auraHeavy = assetMap.getAsset(i3);

            int i4 = assetMap.getIndexOrDefault("Dignity_Aura_Root", -1);
            if (i4 >= 0) auraStun = assetMap.getAsset(i4);

            if (auraLight == null) { int i = assetMap.getIndexOrDefault("Slow", -1); if (i >= 0) auraLight = assetMap.getAsset(i); }
            if (auraMedium == null) auraMedium = auraLight;
            if (auraHeavy == null) auraHeavy = auraLight;
            if (auraStun == null) auraStun = auraHeavy;
        } catch (Exception ignored) {}
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        loadEffects();

        Ref<EntityStore> mobRef = chunk.getReferenceTo(index);
        if (!mobRef.isValid()) return;

        NPCEntity npc = store.getComponent(mobRef, java.util.Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npc == null) return;

        TransformComponent npcTransform = store.getComponent(mobRef, TransformComponent.getComponentType());
        if (npcTransform == null) return;

        // Recuperer le niveau du mob
        int mobLevel = 1;
        ComponentType<EntityStore, MobLevelData> mobLevelType = EldaniorSystem.get().getMobLevelDataType();
        if (mobLevelType != null) {
            MobLevelData mobData = store.getComponent(mobRef, mobLevelType);
            if (mobData != null) mobLevel = mobData.getLevel();
        }

        Vector3d npcPos = npcTransform.getPosition();

        for (Map.Entry<UUID, Vector3d> playerEntry : PlayerPositionTracker.PLAYER_POSITIONS.entrySet()) {
            UUID playerUUID = playerEntry.getKey();

            Integer playerDignity = PlayerPositionTracker.PLAYER_DIGNITY.get(playerUUID);
            if (playerDignity == null || playerDignity < 5) continue;
            if (!DignityAuraSystem.isEmitterActive(playerUUID)) continue;

            // Recuperer le niveau du joueur
            Integer playerLevel = PlayerPositionTracker.PLAYER_LEVELS.get(playerUUID);
            if (playerLevel == null) playerLevel = 1;

            // Calcul du ratio de niveau :
            // mob level <= player level -> ratio 1.0 (plein effet)
            // mob level > player level mais <= player level + 50 -> ratio degressif
            // mob level > player level + 50 -> ratio 0 (aucun effet)
            int levelGap = mobLevel - playerLevel;
            float levelRatio;
            if (levelGap <= 0) {
                levelRatio = 1.0f;
            } else if (levelGap <= 50) {
                levelRatio = 1.0f - (levelGap / 50.0f);
            } else {
                continue; // Mob trop haut level, aucun effet
            }

            int auraRadius = getAuraRadius(playerDignity);
            double distSq = distanceSquared(npcPos, playerEntry.getValue());
            if (distSq > (double) auraRadius * auraRadius) continue;

            double dist = Math.sqrt(distSq);
            float distanceRatio = Math.max(0f, 1.0f - (float) (dist / auraRadius));
            float slow = Math.min(0.90f, playerDignity * 0.009f) * distanceRatio * levelRatio;

            EffectControllerComponent effectCtrl = store.getComponent(mobRef, EffectControllerComponent.getComponentType());
            if (effectCtrl == null) break;

            if (slow >= 0.55f && auraStun != null) {
                effectCtrl.addEffect(mobRef, auraStun, 2.0f, OverlapBehavior.OVERWRITE, store);
            } else if (slow >= 0.35f && auraHeavy != null) {
                effectCtrl.addEffect(mobRef, auraHeavy, 2.0f, OverlapBehavior.OVERWRITE, store);
            } else if (slow >= 0.20f && auraMedium != null) {
                effectCtrl.addEffect(mobRef, auraMedium, 2.0f, OverlapBehavior.OVERWRITE, store);
            } else if (slow >= 0.05f && auraLight != null) {
                effectCtrl.addEffect(mobRef, auraLight, 2.0f, OverlapBehavior.OVERWRITE, store);
            }
            break;
        }
    }

    private static int getAuraRadius(int dignity) {
        if (dignity >= 100) return 20;
        if (dignity >= 75) return 16;
        if (dignity >= 50) return 12;
        if (dignity >= 30) return 8;
        if (dignity >= 15) return 6;
        if (dignity >= 5) return 4;
        return 0;
    }

    private double distanceSquared(Vector3d a, Vector3d b) {
        double dx = a.x - b.x; double dy = a.y - b.y; double dz = a.z - b.z;
        return dx * dx + dy * dy + dz * dz;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}
