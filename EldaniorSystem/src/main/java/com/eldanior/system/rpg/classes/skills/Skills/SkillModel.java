package com.eldanior.system.rpg.classes.skills.Skills;

import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.rpg.enums.ClassType;
import com.eldanior.system.rpg.enums.Rarity;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;

public abstract class SkillModel {

    // --- Identité ---
    private final String id;
    private final String name;
    private final String description;
    private final Rarity rarity;
    private final ClassType classType;

    // --- Mécaniques Temporelles ---
    private final float activationTime;
    private final float cooldown;
    private final float tickInterval;

    // --- Propriétés ---
    private final boolean isPassive;
    private final float radius;
    private boolean isSelected;

    // --- Coûts & Ressources ---
    private final float resourceCostPerSecond;

    // --- Placeholders (Futur) ---
    private String itemActivation;
    private String consumableRequired;

    // --- Effets ---
    private final List<String> effectIds;

    // ================= CONSTRUCTEUR COMPLET =================
    public SkillModel(String id, String name, String description,
                      Rarity rarity, ClassType classType,
                      float activationTime, float cooldown, float tickInterval,
                      boolean isPassive, float resourceCostPerSecond, float radius,
                      List<String> effectIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.classType = classType;
        this.activationTime = activationTime;
        this.cooldown = cooldown;
        this.tickInterval = tickInterval;
        this.isPassive = isPassive;
        this.resourceCostPerSecond = resourceCostPerSecond;
        this.radius = radius;

        this.effectIds = (effectIds != null) ? effectIds : new ArrayList<>();
        this.isSelected = false;
        this.itemActivation = null;
        this.consumableRequired = null;
    }

    // ================= LOGIQUE ABSTRAITE =================

    /**
     * Méthode appelée à chaque tick pour le porteur du skill.
     * Ajout de CommandBuffer pour les sons.
     */
    public abstract void onTick(Ref<EntityStore> playerRef, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, PlayerLevelData data);

    /**
     * Méthode appelée par l'AuraSystem lorsqu'une entité est dans le rayon du skill.
     * Ajout de CommandBuffer pour les sons d'impact.
     */
    public abstract void onAuraTick(Ref<EntityStore> source, Ref<EntityStore> target, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, double distance);

    // ================= LOGIQUE MÉTIER =================

    public String getResourceType() {
        if (this.classType == ClassType.MAGE || this.classType == ClassType.DRAGON) {
            return "Mana";
        }
        return "Endurance";
    }

    public float getRarityMultiplier() {
        return switch (this.rarity) {
            case COMMON -> 1.0f;
            case RARE -> 1.25f;
            case EPIC -> 1.5f;
            case UNIQUE -> 2.0f;
            case LEGENDARY -> 2.5f;
            case DIVINE -> 3.5f;
            default -> 1.0f;
        };
    }

    // ================= GETTERS & SETTERS =================

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Rarity getRarity() { return rarity; }
    public ClassType getClassType() { return classType; }
    public float getActivationTime() { return activationTime; }
    public float getCooldown() { return cooldown; }
    public float getTickInterval() { return tickInterval; }
    public boolean isPassive() { return isPassive; }
    public float getRadius() { return radius; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
    public float getResourceCostPerSecond() { return resourceCostPerSecond; }
    public List<String> getEffectIds() { return effectIds; }

    public void setItemActivation(String item) { this.itemActivation = item; }
    public void setConsumableRequired(String item) { this.consumableRequired = item; }
}