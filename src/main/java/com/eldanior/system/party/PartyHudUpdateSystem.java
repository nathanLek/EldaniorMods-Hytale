package com.eldanior.system.party;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.UUID;

public class PartyHudUpdateSystem extends EntityTickingSystem<EntityStore> {

    private int tickCounter = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        // Rafraichir toutes les 10 ticks (~0.5s) pour ne pas spammer
        tickCounter++;
        if (tickCounter % 10 != 0) return;

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        Player player = store.getComponent(entityRef, Player.getComponentType());
        if (player == null) return;

        PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
        if (playerRef == null) return;

        UUID playerUUID = extractUUID(playerRef);
        if (playerUUID == null) return;

        if (!PartyManager.hasParty(playerUUID)) return;

        CustomUIHud hud = player.getHudManager().getCustomHud();
        if (hud instanceof PartyHud) {
            hud.show();
        }
    }

    private UUID extractUUID(PlayerRef playerRef) {
        try {
            Field uuidField = PlayerRef.class.getDeclaredField("uuid");
            uuidField.setAccessible(true);
            return (UUID) uuidField.get(playerRef);
        } catch (Exception e) {
            return null;
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
