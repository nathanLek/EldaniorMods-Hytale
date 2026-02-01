package com.eldanior.system.config.Effects;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.RemovalBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.entity.effect.ActiveEntityEffect;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class EffectsManager {

    // --- ACCÈS ET INITIALISATION ---

    public static EffectControllerComponent getOrCreateController(Ref<EntityStore> entityRef, ComponentAccessor<EntityStore> store) {
        ComponentType<EntityStore, EffectControllerComponent> type = EffectControllerComponent.getComponentType();
        EffectControllerComponent controller = store.getComponent(entityRef, type);
        if (controller == null) {
            controller = new EffectControllerComponent();
            store.putComponent(entityRef, type, controller);
        }
        return controller;
    }

    public static EffectControllerComponent getController(Ref<EntityStore> entityRef, ComponentAccessor<EntityStore> store) {
        return store.getComponent(entityRef, EffectControllerComponent.getComponentType());
    }

    // --- RÉCUPÉRATION D'ASSETS SÉCURISÉE ---

    public static EntityEffect getEffectAsset(String effectId) {
        // Sécurité : Si l'ID est null ou vide, on ne cherche pas
        if (effectId == null || effectId.isEmpty()) return null;
        return EntityEffect.getAssetMap().getAsset(effectId);
    }

    public static int getEffectIndex(String effectId) {
        // Sécurité : On vérifie d'abord que l'asset existe pour éviter les index invalides (-1 ou MIN_VALUE)
        if (getEffectAsset(effectId) == null) return -1;
        return EntityEffect.getAssetMap().getIndex(effectId);
    }

    // --- APPLICATION D'EFFETS ---

    public static boolean applyEffect(Ref<EntityStore> entityRef, String effectId, ComponentAccessor<EntityStore> store) {
        // On récupère l'asset de manière sécurisée
        EntityEffect effect = getEffectAsset(effectId);

        // Si l'effet n'existe pas, on arrête sans erreur
        if (effect == null) return false;

        EffectControllerComponent controller = getOrCreateController(entityRef, store);
        return controller.addEffect(entityRef, effect, store);
    }

    public static void applyCustomEffect(Ref<EntityStore> entityRef, String effectId, float duration, OverlapBehavior behavior, ComponentAccessor<EntityStore> store) {
        EntityEffect effect = getEffectAsset(effectId);

        if (effect != null) {
            EffectControllerComponent controller = getOrCreateController(entityRef, store);
            controller.addEffect(entityRef, effect, duration, behavior, store);
        }
    }

    public static void applyInfiniteEffect(Ref<EntityStore> entityRef, String effectId, ComponentAccessor<EntityStore> store) {
        EntityEffect effect = getEffectAsset(effectId);
        // On récupère l'index, qui sera valide car on a vérifié 'effect != null' juste au-dessus
        int index = getEffectIndex(effectId);

        if (effect != null && index >= 0) {
            EffectControllerComponent controller = getOrCreateController(entityRef, store);
            controller.addInfiniteEffect(entityRef, index, effect, store);
        }
    }

    // --- SUPPRESSION ET SOINS ---

    public static void removeEffect(Ref<EntityStore> entityRef, String effectId, ComponentAccessor<EntityStore> store) {
        // 1. Validation de l'ID
        if (effectId == null || effectId.isEmpty()) return;

        // 2. Validation de l'existence de l'effet dans le jeu
        if (getEffectAsset(effectId) == null) return;

        EffectControllerComponent controller = getController(entityRef, store);
        if (controller != null) {
            int index = getEffectIndex(effectId);
            // 3. Validation de l'index avant suppression pour éviter le crash
            if (index >= 0) {
                controller.removeEffect(entityRef, index, store);
            }
        }
    }

    public static void cureAllDebuffs(Ref<EntityStore> entityRef, ComponentAccessor<EntityStore> store) {
        EffectControllerComponent controller = getController(entityRef, store);
        if (controller == null) return;

        List<Integer> toRemove = new ArrayList<>();
        // Ici on parcourt les effets actifs, donc les index sont forcément valides
        for (Int2ObjectMap.Entry<ActiveEntityEffect> entry : controller.getActiveEffects().int2ObjectEntrySet()) {
            if (entry.getValue().isDebuff()) {
                toRemove.add(entry.getIntKey());
            }
        }

        for (int index : toRemove) {
            controller.removeEffect(entityRef, index, store);
        }
    }

    public static void clearAllEffects(Ref<EntityStore> entityRef, ComponentAccessor<EntityStore> store) {
        EffectControllerComponent controller = getController(entityRef, store);
        if (controller != null) {
            controller.clearEffects(entityRef, store);
        }
    }

    // --- ÉTAT ET ANALYSE ---

    public static boolean hasEffect(Ref<EntityStore> entityRef, String effectId, ComponentAccessor<EntityStore> store) {
        int index = getEffectIndex(effectId);
        if (index < 0) return false;

        EffectControllerComponent controller = getController(entityRef, store);
        return controller != null && controller.getActiveEffects().containsKey(index);
    }

    public static boolean isInvulnerable(Ref<EntityStore> entityRef, ComponentAccessor<EntityStore> store) {
        EffectControllerComponent controller = getController(entityRef, store);
        return controller != null && controller.isInvulnerable();
    }

    public static ActiveEntityEffect getActiveEffect(Ref<EntityStore> entityRef, String effectId, ComponentAccessor<EntityStore> store) {
        int index = getEffectIndex(effectId);
        if (index < 0) return null;

        EffectControllerComponent controller = getController(entityRef, store);
        if (controller == null) return null;
        return controller.getActiveEffects().get(index);
    }

    public static void forEachActiveEffect(Ref<EntityStore> entityRef, ComponentAccessor<EntityStore> store, BiConsumer<Integer, ActiveEntityEffect> action) {
        EffectControllerComponent controller = getController(entityRef, store);
        if (controller != null) {
            for (Int2ObjectMap.Entry<ActiveEntityEffect> entry : controller.getActiveEffects().int2ObjectEntrySet()) {
                action.accept(entry.getIntKey(), entry.getValue());
            }
        }
    }

    // --- LECTURE DE L'INSTANCE (ActiveEntityEffect) ---

    public static float getRemainingTime(ActiveEntityEffect active) { return active.getRemainingDuration(); }
    public static float getInitialDuration(ActiveEntityEffect active) { return active.getInitialDuration(); }
    public static boolean isInfinite(ActiveEntityEffect active) { return active.isInfinite(); }
    public static boolean isDebuff(ActiveEntityEffect active) { return active.isDebuff(); }
    public static int getEffectIndex(ActiveEntityEffect active) { return active.getEntityEffectIndex(); }

    // --- LECTURE DE LA CONFIGURATION (EntityEffect JSON) ---

    public static String getEffectName(EntityEffect effect) { return effect.getName(); }
    public static String getIcon(EntityEffect effect) { return effect.getStatusEffectIcon(); }
    public static OverlapBehavior getOverlap(EntityEffect effect) { return effect.getOverlapBehavior(); }
    public static RemovalBehavior getRemoval(EntityEffect effect) { return effect.getRemovalBehavior(); }
    public static String getModelChange(EntityEffect effect) { return effect.getModelChange(); }
    public static float getDamageCooldown(EntityEffect effect) { return effect.getDamageCalculatorCooldown(); }
    public static Object getDamageCalculator(EntityEffect effect) { return effect.getDamageCalculator(); }
    public static String getValueType(EntityEffect effect) { return effect.getValueType().toString(); }
    public static boolean isConfigInvulnerable(EntityEffect effect) { return effect.isInvulnerable(); }
}