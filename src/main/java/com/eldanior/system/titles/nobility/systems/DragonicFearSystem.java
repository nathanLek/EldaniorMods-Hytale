package com.eldanior.system.titles.nobility.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

import javax.annotation.Nonnull;

/**
 * Peur draconique — partie MONSTRES du palier Dragon de l'Aura de Dignite.
 *
 * Les monstres proches d'un porteur d'aura de palier Dragon (dignite >= 1000) subissent la
 * zone draconique (voir {@link RuneAuraSystem#applyDragonZone}) : degats de chaleur croissants
 * a l'approche + ralentissement ecrasant pres du coeur. La chaleur intense sert de dissuasion
 * (pas de teleportation).
 */
public class DragonicFearSystem extends EntityTickingSystem<EntityStore> {

    private float updateTimer = 0;
    private boolean shouldUpdate = false;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        if (index == 0) {
            updateTimer += dt;
            if (updateTimer >= 0.5f) {
                shouldUpdate = true;
                updateTimer = 0;
            } else {
                shouldUpdate = false;
            }
        }
        if (!shouldUpdate) return;

        Ref<EntityStore> mobRef = chunk.getReferenceTo(index);
        if (!mobRef.isValid()) return;

        NPCEntity npc = store.getComponent(mobRef, NPCEntity.getComponentType());
        if (npc == null) return;

        TransformComponent transform = store.getComponent(mobRef, TransformComponent.getComponentType());
        if (transform == null) return;
        Vector3d pos = transform.getPosition();

        Vector3d dragonEmitter = RuneAuraSystem.dragonEmitterPosAffecting(pos, null);
        if (dragonEmitter == null) return;

        RuneAuraSystem.applyDragonZone(mobRef, pos, dragonEmitter, store);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}
