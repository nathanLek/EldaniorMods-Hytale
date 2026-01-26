package com.eldanior.system.systems;

import com.eldanior.system.EldaniorSystem;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

/**
 * Système ECS qui empêche les items de skill d'être droppés.
 */
public class SkillItemDropSystem extends EntityEventSystem<EntityStore, DropItemEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public SkillItemDropSystem() {
        super(DropItemEvent.class);
    }

    @Override
    public void handle(int index,
                       @NonNull ArchetypeChunk<EntityStore> chunk,
                       @NonNull Store<EntityStore> store,
                       @NonNull CommandBuffer<EntityStore> buffer,
                       @NonNull DropItemEvent event) {
        try {
            // Vérifier si c'est un événement de type Drop
            if (event instanceof DropItemEvent.Drop dropEvent) {
                ItemStack stack = dropEvent.getItemStack();

                // Vérifier si l'item possède un SkillId
                if (!stack.isEmpty()) {
                    String skillId = stack.getFromMetadataOrNull(EldaniorSystem.getSkillIdKey());

                    if (skillId != null && !skillId.isEmpty()) {
                        // Annuler le drop des items de skill
                        event.setCancelled(true);
                        LOGGER.atInfo().log("Drop d'item de skill annulé : " + skillId);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Erreur dans SkillItemDropSystem");
        }
    }

    @NonNull
    @Override
    public Query<EntityStore> getQuery() {
        // Ce système traite tous les événements DropItemEvent
        return Query.any();
    }
}