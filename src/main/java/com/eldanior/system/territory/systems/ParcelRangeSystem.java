package com.eldanior.system.territory.systems;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ParcelRangeSystem extends EntityTickingSystem<EntityStore> {

    private static final Map<UUID, String> playerCurrentParcel = new ConcurrentHashMap<>();
    private int tickCounter = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        tickCounter++;
        if (tickCounter % 20 != 0) return;

        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (!ref.isValid()) return;

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || player.getWorld() == null) return;

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) return;

        UUID playerUUID = getUUID(store, ref);
        if (playerUUID == null) return;

        Vector3d pos = transform.getPosition();
        String world = player.getWorld().getName();

        ParcelData currentParcel = ParcelManager.getParcelAt(world, pos.x, pos.y, pos.z);
        String currentId = currentParcel != null ? currentParcel.getId() : null;
        String previousId = playerCurrentParcel.get(playerUUID);

        if (!java.util.Objects.equals(currentId, previousId)) {
            try {
                PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
                if (pRef != null && currentParcel != null) {
                    ParcelData previousParcel = previousId != null ? ParcelManager.get(previousId) : null;

                    if (isBigZone(currentParcel.getType())) {
                        // Royaume / Territoire / Ville → grand message titre
                        // SAUF si on sort d'une zone enfant vers son parent (ville → territoire)
                        boolean isParentOfPrevious = previousParcel != null
                                && currentParcel.getId().equals(previousParcel.getParentId());

                        if (!isParentOfPrevious) {
                            showZoneTitle(pRef, currentParcel);
                        }
                    } else {
                        // Plot / Housing / Room / Farm → petite notification
                        showZoneNotification(pRef, currentParcel);
                    }
                } else if (pRef != null && currentParcel == null && previousId != null) {
                    // Sortie vers zone sauvage
                    NotificationHelper.showEventTitle(pRef, "ZONE SAUVAGE", "Territoire inexplore", false);
                }
            } catch (Exception e) { EldaniorLogger.error("ParcelRangeSystem", e); }

            if (currentId != null) playerCurrentParcel.put(playerUUID, currentId);
            else playerCurrentParcel.remove(playerUUID);
        }
    }

    private boolean isBigZone(ParcelType type) {
        return type == ParcelType.KINGDOM || type == ParcelType.TERRITORY || type == ParcelType.CITY;
    }

    private void showZoneTitle(PlayerRef pRef, ParcelData parcel) {
        String title = parcel.getName();
        String subtitle = parcel.getType().getLabel().toUpperCase();

        if (parcel.getType() == ParcelType.CITY) {
            subtitle += parcel.isPvpEnabled() ? " — PvP Active" : " — Zone Sure";
        }

        NotificationHelper.showEventTitle(pRef, title, subtitle, false);
    }

    private void showZoneNotification(PlayerRef pRef, ParcelData parcel) {
        String ownerInfo = parcel.getOwnerName().isEmpty() ? "Libre" : parcel.getOwnerName();
        String color = parcel.isProtectedByDefault() ? "gold" : "green";

        NotificationHelper.sendNotification(pRef,
                "<color:" + color + ">[" + parcel.getType().getLabel() + "] " + parcel.getName() + "</color> <color:gray>(" + ownerInfo + ")</color>",
                NotificationStyle.Default);
    }

    public static void handleDisconnect(UUID playerUUID) {
        playerCurrentParcel.remove(playerUUID);
    }

    private UUID getUUID(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef == null) return null;
            return UUIDExtractor.getUUID(pRef);
        } catch (Exception e) { return null; }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
