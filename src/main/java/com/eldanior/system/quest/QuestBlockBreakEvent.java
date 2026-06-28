package com.eldanior.system.quest;

import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.UUID;

/**
 * Ecoute les BreakBlockEvent pour faire progresser les quetes de type MINAGE et RECOLTE.
 *
 * Classification des blocs :
 * - MINAGE : identifiants contenant "ore", "rock", "soil", "stone", "crystal", "gem"
 * - RECOLTE : identifiants contenant "plant", "wood", "log", "leaf", "flower", "crop", "bush", "grass", "vine", "mushroom"
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

        Player player = chunk.getComponent(index, Player.getComponentType());
        if (player == null || player.getWorld() == null) return;

        Vector3i target = event.getTargetBlock();
        if (target == null) return;

        // Recuperer le type de bloc AVANT qu'il ne soit casse
        String blockTypeId;
        try {
            blockTypeId = player.getWorld().getBlockType(target.x(), target.y(), target.z()).getId();
        } catch (Exception e) {
            return; // Bloc inaccessible, on ignore
        }
        if (blockTypeId == null || blockTypeId.isEmpty()) return;

        // Recuperer l'UUID du joueur
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        UUID playerUUID = getUUID(store, ref);
        if (playerUUID == null) return;

        // MINAGE/RECOLTE : la progression est basee sur l'inventaire du joueur,
        // pas sur le comptage de blocs casses. Ce system n'a plus de hook a appeler.
        // On le conserve pour une eventuelle extension future.
    }

    /**
     * Blocs consideres comme du minage (minerais, roches, sols, cristaux).
     */
    private boolean isMiningBlock(String lower) {
        return lower.contains("ore")
                || lower.contains("rock")
                || lower.contains("soil")
                || lower.contains("stone")
                || lower.contains("crystal")
                || lower.contains("gem");
    }

    /**
     * Blocs consideres comme de la recolte (plantes, bois, cultures).
     */
    private boolean isHarvestBlock(String lower) {
        return lower.contains("plant")
                || lower.contains("wood")
                || lower.contains("log")
                || lower.contains("leaf")
                || lower.contains("flower")
                || lower.contains("crop")
                || lower.contains("bush")
                || lower.contains("grass")
                || lower.contains("vine")
                || lower.contains("mushroom");
    }

    private UUID getUUID(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef == null) return null;
            return UUIDExtractor.getUUID(pRef);
        } catch (Exception e) { return null; }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
