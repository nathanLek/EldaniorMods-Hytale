package com.eldanior.system.presentoir;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Tient a jour l'entite de chaque presentoir. Un chunk qui s'endort retire ses entites du monde et les remet a son reveil : celle
 * qui revient alors qu'une autre l'a remplacee, ou qui montre un contenu perime, est supprimee (pas de doublon sur le socle).
 */
public class PresentoirEntitySystem extends RefSystem<EntityStore> {

    @Override
    public void onEntityAdded(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl AddReason reason, @NonNullDecl Store<EntityStore> store,
                              @NonNullDecl CommandBuffer<EntityStore> cb) {
        PresentoirEntity tag = store.getComponent(ref, PresentoirEntity.type());
        if (tag == null) return;
        Presentoir p = PresentoirManager.find(tag.key());
        if (p == null || !tag.look().equals(p.look()) || (p.entityValid() && !p.entity.equals(ref))) cb.tryRemoveEntity(ref, RemoveReason.REMOVE);
        else p.entity = ref;
    }

    @Override
    public void onEntityRemove(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl RemoveReason reason, @NonNullDecl Store<EntityStore> store,
                               @NonNullDecl CommandBuffer<EntityStore> cb) {
        PresentoirEntity tag = store.getComponent(ref, PresentoirEntity.type());
        Presentoir p = tag != null ? PresentoirManager.find(tag.key()) : null;
        if (p != null && ref.equals(p.entity)) p.entity = null;
    }

    @Override
    public Query<EntityStore> getQuery() { return PresentoirEntity.type(); }
}
