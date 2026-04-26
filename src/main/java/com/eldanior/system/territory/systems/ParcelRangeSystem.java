package com.eldanior.system.territory.systems;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
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
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ParcelRangeSystem extends EntityTickingSystem<EntityStore> {

    // Suivi de la parcelle actuelle de chaque joueur
    private static final Map<UUID, String> playerCurrentParcel = new ConcurrentHashMap<>();
    private int tickCounter = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        // Check toutes les 20 ticks (~1 seconde)
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

        // Changement de zone ?
        if (!java.util.Objects.equals(currentId, previousId)) {
            try {
                PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
                if (pRef != null) {
                    if (currentParcel != null) {
                        // Entree dans une parcelle
                        String color = currentParcel.isProtectedByDefault() ? "gold" : "green";
                        NotificationHelper.sendNotification(pRef,
                                "<color:" + color + ">" + currentParcel.getType().getLabel() + " : " + currentParcel.getName() + "</color> <color:gray>(Proprio: " + currentParcel.getOwnerName() + ")</color>",
                                NotificationStyle.Default);
                    } else if (previousId != null) {
                        // Sortie vers zone sauvage
                        NotificationHelper.sendNotification(pRef,
                                "<color:gray>Zone sauvage</color>",
                                NotificationStyle.Default);
                    }
                }
            } catch (Exception ignored) {}

            if (currentId != null) playerCurrentParcel.put(playerUUID, currentId);
            else playerCurrentParcel.remove(playerUUID);
        }
    }

    public static void handleDisconnect(UUID playerUUID) {
        playerCurrentParcel.remove(playerUUID);
    }

    private UUID getUUID(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef == null) return null;
            Field f = PlayerRef.class.getDeclaredField("uuid");
            f.setAccessible(true);
            return (UUID) f.get(pRef);
        } catch (Exception e) { return null; }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}