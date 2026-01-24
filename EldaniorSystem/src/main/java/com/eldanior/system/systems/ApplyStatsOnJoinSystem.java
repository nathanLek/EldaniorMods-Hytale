package com.eldanior.system.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.utils.StatCalculator;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Ce système sert juste à ré-appliquer la vitesse et les PV quand on se connecte
public class ApplyStatsOnJoinSystem extends EntityTickingSystem<EntityStore> {

    // On garde une trace des joueurs "initialisés" pour ne pas le faire 20 fois par seconde
    private final Map<UUID, Boolean> initialized = new ConcurrentHashMap<>();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        UUIDComponent uuidComp = (UUIDComponent) store.getComponent(entityRef, UUIDComponent.getComponentType());
        if (uuidComp == null) return;

        // Si on a déjà géré ce joueur, on passe
        if (initialized.containsKey(uuidComp.getUuid())) return;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(entityRef, type);

        if (data != null) {
            // BOOM : On force l'application des stats (Vitesse, PV Max...)
            StatCalculator.updatePlayerStats(entityRef, store, data);

            // On marque le joueur comme "fait"
            initialized.put(uuidComp.getUuid(), true);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return UUIDComponent.getComponentType();
    }
}