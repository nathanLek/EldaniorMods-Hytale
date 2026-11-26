package com.eldanior.system.titles.nobility;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class PlayerNameplateSystem extends EntityTickingSystem<EntityStore> {

    private float updateTimer = 0;
    private boolean shouldUpdate = false;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        if (index == 0) {
            updateTimer += dt;
            if (updateTimer >= 2.0f) {
                shouldUpdate = true;
                updateTimer = 0;
            } else {
                shouldUpdate = false;
            }
        }
        if (!shouldUpdate) return;

        Ref<EntityStore> playerRef = chunk.getReferenceTo(index);
        if (!playerRef.isValid()) return;

        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) return;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(playerRef, type);
        if (data == null) return;

        // filet de securite du x2 de la rune Arkies Domini : une fiche reecrite apres l'arret de la rune le garderait
        if (data.getStatMultiplier() != 1f
                && !com.eldanior.system.skills.runes.spells.ArkiesDomini.isActive(player.getPlayerRef().getUuid())) {
            data.setStatMultiplier(1f);
            final PlayerLevelData statData = data;
            commandBuffer.run(s -> com.eldanior.system.Leveling.utils.StatCalculator.updatePlayerStats(playerRef, s, statData));
        }

        String playerName = player.getPlayerRef().getUsername();
        // Observateur (2026-11-16) : son nom d'emprunt remplace tout (rang, famille, [PK]) pour ne rien trahir
        String alias = com.eldanior.system.classes.observateur.ObservateurManager.alias(player.getPlayerRef().getUuid(), data);
        String nameplate = alias != null ? alias : NobilityManager.buildNameplate(playerName, data);
        // Sans Visage (2026-11-17) : sous l'apparence d'une creature et sans nom d'emprunt, aucun nom ne flotte au-dessus de lui
        String masked = com.eldanior.system.skills.runes.spells.SansVisage.nameplate(player.getPlayerRef().getUuid());
        if (masked != null) nameplate = masked;

        commandBuffer.putComponent(playerRef, Nameplate.getComponentType(), new Nameplate(nameplate));
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}