package com.eldanior.system.guards;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interaction (touche F) avec un PNJ de poste civil : quand son role passe en etat $Interaction, NpcServices ouvre le service
 * (ou la fenetre de discussion) pour le joueur vise. Remplace les anciens systemes du maitre de guilde, du marchand Luo et du
 * recrutement des aventuriers.
 */
public class NpcInteractSystem extends EntityTickingSystem<EntityStore> {

    private static final Set<Integer> inInteraction = ConcurrentHashMap.newKeySet();   // PNJ deja traites pour l'interaction en cours

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        GuardType type = npc != null ? GuardType.fromRole(npc.getRoleName()) : null;
        if (type == null || !NpcServices.talks(type) || GuardType.isCompanionRole(npc.getRoleName())) return;
        StateSupport st = StateSupport.get(ref, store);
        String state = st != null ? st.getStateName() : null;
        int key = System.identityHashCode(npc);
        if (state == null || !state.contains("$Interaction")) { inInteraction.remove(key); return; }
        if (!inInteraction.add(key)) return;
        GuardManager.Post p = GuardManager.postOf(ref, store);
        MarkedEntitySupport marks = MarkedEntitySupport.get(ref, store);
        Ref<EntityStore>[] targets = marks != null ? marks.getEntityTargets() : null;
        if (p == null || targets == null) return;
        for (Ref<EntityStore> t : targets) {
            if (t == null || !t.isValid() || store.getComponent(t, Player.getComponentType()) == null) continue;
            NpcServices.interact(p, ref, t, store);
            return;
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() { return NPCEntity.getComponentType(); }
}
