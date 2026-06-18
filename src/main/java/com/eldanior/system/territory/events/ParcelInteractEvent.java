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
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.UUID;

public class ParcelInteractEvent extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    public ParcelInteractEvent() { super(UseBlockEvent.Pre.class); }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl UseBlockEvent.Pre event) {
        Ref<EntityStore> playerRef = event.getContext().getEntity();
        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) return;

        if (player.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION)) return;

        Vector3i target = event.getTargetBlock();
        String world = player.getWorld() != null ? player.getWorld().getName() : "";

        UUID playerUUID = getUUID(store, playerRef);
        if (playerUUID == null) return;

        ParcelData parcel = ParcelManager.getParcelAt(world, target.x(), target.y(), target.z());
        if (parcel == null) return;

        if (!parcel.hasPermission(playerUUID, ParcelPermission.INTERACT)) {
            event.setCancelled(true);
            try {
                PlayerRef pRef = store.getComponent(playerRef, PlayerRef.getComponentType());
                if (pRef != null) {
                    NotificationHelper.sendNotification(pRef,
                            "<color:red>Acces interdit !</color> <color:gray>(" + parcel.getName() + ")</color>",
                            NotificationStyle.Warning);
                }
            } catch (Exception e) { EldaniorLogger.error("ParcelInteractEvent", e); }
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