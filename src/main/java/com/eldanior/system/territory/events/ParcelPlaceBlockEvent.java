package com.eldanior.system.territory.events;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelPermission;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.UUID;

public class ParcelPlaceBlockEvent extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    public ParcelPlaceBlockEvent() { super(PlaceBlockEvent.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl PlaceBlockEvent event) {
        Player player = chunk.getComponent(index, Player.getComponentType());
        if (player == null || player.getWorld() == null) return;

        if (player.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) return;

        Vector3i target = event.getTargetBlock();
        String world = player.getWorld().getName();

        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        UUID playerUUID = getUUID(store, ref);
        if (playerUUID == null) return;

        ParcelData parcel = ParcelManager.getParcelAt(world, target.getX(), target.getY(), target.getZ());
        if (parcel == null) return;

        if (!parcel.hasPermission(playerUUID, ParcelPermission.BUILD)) {
            event.setCancelled(true);
            try {
                PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
                if (pRef != null) {
                    NotificationHelper.sendNotification(pRef,
                            "<color:red>Zone protegee !</color> <color:gray>(" + parcel.getName() + ")</color>",
                            NotificationStyle.Warning);
                }
            } catch (Exception e) { EldaniorLogger.error("ParcelPlaceBlockEvent", e); }
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