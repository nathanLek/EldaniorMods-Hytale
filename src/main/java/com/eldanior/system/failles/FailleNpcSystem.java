package com.eldanior.system.failles;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * PNJ apparus dans le monde d'une faille : ceux enregistres dans les pieces vanilla (Duc gobelin, gardes...) arrivent au collage
 * et sont retires (les monstres de la faille sont places par FailleManager, a son niveau). Plus tard : phases du boss et
 * invocations, adoptees par la faille.
 */
public class FailleNpcSystem extends RefSystem<EntityStore> {

    @Override
    public void onEntityAdded(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl AddReason reason, @NonNullDecl Store<EntityStore> store,
                              @NonNullDecl CommandBuffer<EntityStore> cb) {
        var world = store.getExternalData().getWorld();
        if (world == null || !FailleManager.isRiftWorld(world.getName())) return;
        if (FailleManager.onNpcAdded(world, ref, store)) cb.tryRemoveEntity(ref, RemoveReason.REMOVE);
    }

    @Override
    public void onEntityRemove(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl RemoveReason reason, @NonNullDecl Store<EntityStore> store,
                               @NonNullDecl CommandBuffer<EntityStore> cb) { }

    @Override
    public Query<EntityStore> getQuery() { return NPCEntity.getComponentType(); }
}
