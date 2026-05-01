package com.eldanior.system.party;

import com.eldanior.system.hud.CombinedHud;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Rafraichit le party HUD toutes les ~0.5s.
 * Delegue au CombinedHud qui gere quetes + groupe.
 */
public class PartyHudUpdateSystem extends EntityTickingSystem<EntityStore> {

    private int tickCounter = 0;
    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        tickCounter++;
        if (tickCounter % 10 != 0) return;

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        Player player = store.getComponent(entityRef, Player.getComponentType());
        if (player == null) return;

        PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
        if (playerRef == null) return;

        UUID playerUUID = extractUUID(playerRef);
        if (playerUUID == null || !PartyManager.hasParty(playerUUID)) return;

        // Le CombinedHud gere tout. S'il n'existe pas encore, QuestHudUpdateSystem le creera.
        CustomUIHud hud = player.getHudManager().getCustomHud();
        if (hud instanceof CombinedHud) {
            hud.show(); // Force refresh
        }
    }

    private UUID extractUUID(PlayerRef playerRef) {
        try { return UUIDExtractor.getUUID(playerRef); }
        catch (Exception e) { return null; }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
