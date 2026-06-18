package com.eldanior.system.skills.system;

import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gère les états d'invisibilité des joueurs.
 * - PARTIAL : semi-transparent (tint alpha), détectable par SixthSense
 * - TOTAL : complètement invisible, plus difficile à détecter
 */
public class InvisibilityManager {

    public enum InvisType { NONE, PARTIAL, TOTAL }

    private static final String EFFECT_PARTIAL = "Elda_InvisPartielle";
    private static final String EFFECT_TOTAL = "Elda_InvisTotal";

    // UUID joueur -> type d'invisibilité actif
    private static final Map<UUID, InvisType> invisiblePlayers = new ConcurrentHashMap<>();

    public static void applyInvisibility(UUID uuid, Ref<EntityStore> ref, InvisType type, ComponentAccessor<EntityStore> store) {
        // Retirer l'ancien effet si présent
        removeInvisibility(uuid, ref, store);

        String effectId = type == InvisType.PARTIAL ? EFFECT_PARTIAL : EFFECT_TOTAL;
        EffectsManager.applyEffect(ref, effectId, store);
        invisiblePlayers.put(uuid, type);
    }

    public static void applyInvisibility(UUID uuid, Ref<EntityStore> ref, InvisType type, float durationSeconds, ComponentAccessor<EntityStore> store) {
        removeInvisibility(uuid, ref, store);

        String effectId = type == InvisType.PARTIAL ? EFFECT_PARTIAL : EFFECT_TOTAL;
        EffectsManager.applyCustomEffect(ref, effectId, durationSeconds, OverlapBehavior.OVERWRITE, store);
        invisiblePlayers.put(uuid, type);
    }

    public static void removeInvisibility(UUID uuid, Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        InvisType current = invisiblePlayers.getOrDefault(uuid, InvisType.NONE);
        if (current != InvisType.NONE) {
            String effectId = current == InvisType.PARTIAL ? EFFECT_PARTIAL : EFFECT_TOTAL;
            EffectsManager.removeEffect(ref, effectId, store);
            invisiblePlayers.remove(uuid);
        }
    }

    public static InvisType getInvisType(UUID uuid) {
        return invisiblePlayers.getOrDefault(uuid, InvisType.NONE);
    }

    public static boolean isInvisible(UUID uuid) {
        return invisiblePlayers.containsKey(uuid) && invisiblePlayers.get(uuid) != InvisType.NONE;
    }

    public static boolean isPartiallyInvisible(UUID uuid) {
        return invisiblePlayers.getOrDefault(uuid, InvisType.NONE) == InvisType.PARTIAL;
    }

    public static boolean isTotallyInvisible(UUID uuid) {
        return invisiblePlayers.getOrDefault(uuid, InvisType.NONE) == InvisType.TOTAL;
    }
}
