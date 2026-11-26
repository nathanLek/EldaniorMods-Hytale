package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entites de sort visibles d'un seul joueur (2026-11-10, double rouge du Sens martial). Meme mecanique que le masquage des
 * joueurs du moteur (EntityTrackerSystems.HideFromPlayer) : apres la collecte des entites visibles de chaque joueur
 * (CollectVisible), les entites privees d'un autre joueur sont retirees de sa liste ; le client ne les recoit donc jamais.
 * Enregistre dans EldaniorSystem.
 */
public final class PrivateVisibility extends EntityTickingSystem<EntityStore> {

    /** entite privee -> le seul joueur qui la voit */
    private static final Map<Ref<EntityStore>, Ref<EntityStore>> OWNERS = new ConcurrentHashMap<>();

    static void show(Ref<EntityStore> entity, Ref<EntityStore> onlyFor) {
        if (entity != null && onlyFor != null) OWNERS.put(entity, onlyFor);
    }

    static void forget(Ref<EntityStore> entity) {
        if (entity != null) OWNERS.remove(entity);
    }

    private final Set<Dependency<EntityStore>> dependencies =
            Set.of(new SystemDependency<>(Order.AFTER, EntityTrackerSystems.CollectVisible.class));

    @Override
    public SystemGroup<EntityStore> getGroup() { return EntityTrackerSystems.FIND_VISIBLE_ENTITIES_GROUP; }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() { return dependencies; }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() { return EntityTrackerSystems.EntityViewer.getComponentType(); }

    @Override
    public boolean isParallel(int archetypeChunkSize, int taskCount) { return false; }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> cb) {
        if (OWNERS.isEmpty()) return;
        EntityTrackerSystems.EntityViewer viewer = chunk.getComponent(index, EntityTrackerSystems.EntityViewer.getComponentType());
        if (viewer == null || viewer.visible == null) return;
        Ref<EntityStore> me = chunk.getReferenceTo(index);
        for (Iterator<Ref<EntityStore>> it = viewer.visible.iterator(); it.hasNext(); ) {
            Ref<EntityStore> r = it.next();
            Ref<EntityStore> owner = OWNERS.get(r);
            if (owner == null) continue;
            if (!r.isValid()) { OWNERS.remove(r); continue; }
            if (!owner.equals(me)) { it.remove(); viewer.hiddenCount++; }
        }
    }
}
