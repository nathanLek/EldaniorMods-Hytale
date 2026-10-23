package com.eldanior.system.guards;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Garde mort : son poste le fera reapparaitre (GuardManager.RESPAWN_MS). */
public class GuardDeathSystem extends EntityTickingSystem<EntityStore> {

    private final Set<UUID> handled = ConcurrentHashMap.newKeySet();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        if (npc == null || GuardType.fromRole(npc.getRoleName()) == null) return;
        UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
        if (u == null || !handled.add(u.getUuid())) return;
        GuardManager.onGuardDeath(u.getUuid());
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(NPCEntity.getComponentType(), DeathComponent.getComponentType());
    }
}
