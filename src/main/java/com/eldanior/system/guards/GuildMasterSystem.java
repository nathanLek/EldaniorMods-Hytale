package com.eldanior.system.guards;

import com.eldanior.system.boards.GuildMasterScreen;
import com.eldanior.system.territory.ParcelData;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maitre de guilde (GuardType.MAITRE_GUILDE) : role interactif de l'aventurier libre ; quand un joueur lui parle (etat
 * $Interaction, comme AdventurerSystem), sa fenetre s'ouvre (GuildMasterScreen). Lieu affiche : la ville du PNJ.
 */
public class GuildMasterSystem extends EntityTickingSystem<EntityStore> {

    private static final Set<Integer> inInteraction = ConcurrentHashMap.newKeySet();   // PNJ deja traites pour l'interaction en cours

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        if (npc == null || GuardType.fromRole(npc.getRoleName()) != GuardType.MAITRE_GUILDE) return;
        StateSupport st = StateSupport.get(ref, store);
        String state = st != null ? st.getStateName() : null;
        int key = System.identityHashCode(npc);
        if (state == null || !state.contains("$Interaction")) { inInteraction.remove(key); return; }
        if (!inInteraction.add(key)) return;                                            // deja traite pour cette interaction
        MarkedEntitySupport marks = MarkedEntitySupport.get(ref, store);
        Ref<EntityStore>[] targets = marks != null ? marks.getEntityTargets() : null;
        if (targets == null) return;
        for (Ref<EntityStore> t : targets) {
            Player player = t != null && t.isValid() ? store.getComponent(t, Player.getComponentType()) : null;
            PlayerRef pr = player != null ? store.getComponent(t, PlayerRef.getComponentType()) : null;
            if (pr == null) continue;
            player.getPageManager().openCustomPage(t, store, new GuildMasterScreen(pr, place(ref, store)));
            return;
        }
    }

    /** Ville (ou territoire, royaume) du PNJ, null hors des villes. */
    private static String place(Ref<EntityStore> ref, Store<EntityStore> store) {
        TransformComponent tr = store.getComponent(ref, TransformComponent.getComponentType());
        World world = store.getExternalData().getWorld();
        ParcelData town = tr != null && world != null ? BountyHunter.treasuryAt(world.getName(), new Vector3d(tr.getPosition())) : null;
        return town != null ? town.getName() : null;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}
