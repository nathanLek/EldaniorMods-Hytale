package com.eldanior.system.territory.events;

import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelPermission;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.UUID;

/**
 * Bloque silencieusement le minage de blocs dans les zones protegees.
 * Pas de notification (evite le spam quand le joueur attaque un mob
 * et que le clic gauche touche aussi un bloc derriere).
 */
public class ParcelDamageBlockEvent extends EntityEventSystem<EntityStore, DamageBlockEvent> {

    public ParcelDamageBlockEvent() { super(DamageBlockEvent.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl DamageBlockEvent event) {
        Player player = chunk.getComponent(index, Player.getComponentType());
        if (player == null || player.getWorld() == null) return;

        if (player.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION)) return;

        Vector3i target = event.getTargetBlock();
        String world = player.getWorld().getName();

        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        UUID playerUUID = getUUID(store, ref);
        if (playerUUID == null) return;

        ParcelData parcel = ParcelManager.getParcelAt(world, target.x(), target.y(), target.z());
        if (parcel == null) return;

        if (!parcel.hasPermission(playerUUID, ParcelPermission.BREAK)) {
            event.setCancelled(true);
            // Pas de notification — evite le spam lors d'attaques de mobs
        }
    }

    private UUID getUUID(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef == null) return null;
            return UUIDExtractor.getUUID(pRef);
        } catch (Exception e) { return null; }
    }

    @Override
    public Query<EntityStore> getQuery() { return Archetype.empty(); }
}
