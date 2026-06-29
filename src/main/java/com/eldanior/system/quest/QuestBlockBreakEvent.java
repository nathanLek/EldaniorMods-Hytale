package com.eldanior.system.quest;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Ecoute les BreakBlockEvent pour faire progresser les quetes de type MINAGE et RECOLTE.
 *
 * MINAGE/RECOLTE : la progression est desormais basee sur l'inventaire du joueur,
 * pas sur le comptage de blocs casses. Ce system est conserve pour ne pas casser
 * l'enregistrement dans EldaniorSystem et pour une eventuelle extension future.
 */
public class QuestBlockBreakEvent extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    public QuestBlockBreakEvent() {
        super(BreakBlockEvent.class);
    }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl BreakBlockEvent event) {

        if (event.isCancelled()) return;
        return;
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
