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
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bloque le minage de blocs dans les zones protegees.
 * Notification avec cooldown de 3s pour eviter le spam.
 */
public class ParcelDamageBlockEvent extends EntityEventSystem<EntityStore, DamageBlockEvent> {

    private static final ConcurrentHashMap<UUID, Long> notifCooldown = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 3000L;

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
            // Notification avec cooldown (evite le spam)
            long now = System.currentTimeMillis();
            Long last = notifCooldown.get(playerUUID);
            if (last == null || now - last >= COOLDOWN_MS) {
                notifCooldown.put(playerUUID, now);
                try {
                    PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
                    if (pRef != null) {
                        NotificationHelper.sendNotification(pRef,
                                "<color:red>Zone protegee !</color> <color:gray>(" + parcel.getName() + ")</color>",
                                NotificationStyle.Warning);
                    }
                } catch (Exception e) { EldaniorLogger.error("ParcelDamageBlockEvent", e); }
            }
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
