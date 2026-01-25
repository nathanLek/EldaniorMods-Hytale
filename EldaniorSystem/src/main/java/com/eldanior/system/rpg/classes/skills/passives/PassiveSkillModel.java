package com.eldanior.system.rpg.classes.skills.passives;

import com.eldanior.system.components.PlayerLevelData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public abstract class PassiveSkillModel {

    private final String id;
    private final String name;
    private final String description;

    // Intervalle d'activation (ex: 1.0f = toutes les secondes)
    private final float tickInterval;

    public PassiveSkillModel(String id, String name, String description, float tickInterval) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tickInterval = tickInterval;
    }

    // C'est ici que la magie opère (à définir dans chaque skill)
    public abstract void onTick(Ref<EntityStore> playerRef, Store<EntityStore> store, PlayerLevelData data);

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public float getTickInterval() { return tickInterval; }
}