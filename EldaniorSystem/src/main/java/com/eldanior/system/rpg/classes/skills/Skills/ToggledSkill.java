package com.eldanior.system.rpg.classes.skills.Skills;

import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.rpg.enums.ClassType;
import com.eldanior.system.rpg.enums.Rarity;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Compétence qui s'active/désactive (ON/OFF).
 * Exemples : Auras, Buffs de vitesse, Régénération.
 */
public abstract class ToggledSkill extends SkillModel {

    private final float tickInterval; // Combien de temps entre chaque tick (ex: 1.0s)
    private final float radius;       // Rayon d'effet (si c'est une aura)

    public ToggledSkill(String id, String name, String description, Rarity rarity, ClassType classType,
                        float cooldown, float costPerSecond, float tickInterval, float radius) {
        super(id, name, description, rarity, classType, cooldown, costPerSecond);
        this.tickInterval = tickInterval;
        this.radius = radius;
    }

    // Appelé à chaque intervalle (pour consommer du mana ou mettre des particules sur le joueur)
    public abstract void onTick(Ref<EntityStore> playerRef, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, PlayerLevelData data);

    // Appelé pour chaque entité trouvée dans le rayon (pour infliger des dégâts ou soigner)
    public abstract void onAuraTick(Ref<EntityStore> source, Ref<EntityStore> target, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, double distance);

    public float getTickInterval() { return tickInterval; }
    public float getRadius() { return radius; }
}