package com.eldanior.system.familiers;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseEntityEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Touche d'interaction sur un familier (2026-11-13) : son maitre (ou un admin) choisit entre le chevaucher et la fenetre de
 * gestion (FamilierChoicePage). L'interaction du moteur est annulee : le role n'a plus d'action de monture (gen_familiers.py),
 * la monte passe par FamilierManager.mount. Un autre joueur : rien.
 */
public class FamilierInteractEvent extends EntityEventSystem<EntityStore, UseEntityEvent.Pre> {

    public FamilierInteractEvent() { super(UseEntityEvent.Pre.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl UseEntityEvent.Pre event) {
        try {
            if (event.getInteractionType() != InteractionType.Use) return;
            Ref<EntityStore> pet = event.getTargetEntity();
            Familier f = FamilierManager.petOf(pet, store);
            if (f == null) return;
            Ref<EntityStore> ref = event.getContext().getEntity();
            Player player = ref != null ? store.getComponent(ref, Player.getComponentType()) : null;
            PlayerRef pr = ref != null ? store.getComponent(ref, PlayerRef.getComponentType()) : null;
            if (player == null || pr == null) return;
            boolean admin = FamilierManager.isAdmin(pr.getUuid());
            event.setCancelled(true);
            if (!f.owner.equals(pr.getUuid()) && !admin) {
                FamilierManager.say(pr, f.name + " n'obéit qu'à " + f.ownerName + ".", "#e8c060");
                return;
            }
            player.getPageManager().openCustomPage(ref, store, new FamilierChoicePage(pr, f.owner, pet, admin));
        } catch (Exception e) {
            EldaniorLogger.error("[Familiers] interaction", e);
        }
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
