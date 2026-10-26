package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PNJ a role de poste (garde, criminel, aventurier...) qui n'est le PNJ d'aucun poste ni le complice d'aucun criminel : retire.
 * Ils viennent d'apparitions ratees dans un chunk decharge (l'entite etait creee quand meme, sans skin ni stats, et chaque
 * nouvel essai en ajoutait une) ou de postes retires pendant que leur zone etait dechargee. Chaque PNJ n'est verifie qu'une fois.
 */
public class GuardOrphanSystem extends EntityTickingSystem<EntityStore> {

    private final Set<UUID> checked = ConcurrentHashMap.newKeySet();
    private int removed;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        if (npc == null || GuardType.fromRole(npc.getRoleName()) == null) return;
        UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
        if (u == null || checked.contains(u.getUuid())) return;
        if (GuardManager.isKnown(u.getUuid())) { checked.add(u.getUuid()); return; }
        checked.add(u.getUuid());
        cb.removeEntity(ref, RemoveReason.REMOVE);
        if (++removed % 25 == 1) EldaniorLogger.info("[Guards] PNJ de poste orphelin retire (" + npc.getRoleName() + "), total : " + removed);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}
