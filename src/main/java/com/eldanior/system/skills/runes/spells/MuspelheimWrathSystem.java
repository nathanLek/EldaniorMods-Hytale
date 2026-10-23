package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;

/** Fait avancer, a chaque tick, les pluies de meteorites du Courroux de Muspelheim (voir {@link MuspelheimWrath}). */
public class MuspelheimWrathSystem extends EntityTickingSystem<EntityStore> {

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> caster = chunk.getReferenceTo(index);
        if (!caster.isValid()) return;
        UUID uuid = MuspelheimWrath.getUuid(caster, store);
        if (uuid == null || !MuspelheimWrath.hasActiveRain(uuid)) return;
        MuspelheimWrath.tick(uuid, caster, store, commandBuffer);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
