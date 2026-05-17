package com.eldanior.system.titles.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.TitleManager;
import com.eldanior.system.titles.models.TitleModel;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;

public class TitleCheckSystem extends EntityTickingSystem<EntityStore> {

    private float updateTimer = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        updateTimer += dt;
        if (updateTimer < 1.0f) return;
        if (index == 0) updateTimer = 0;

        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        if (!ref.isValid()) return;

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return;

        List<TitleModel> newTitles = TitleManager.checkTitleUnlocks(data);
        if (newTitles.isEmpty()) return;

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        for (TitleModel title : newTitles) {
            data.addTitle(title.getId());
            if (playerRef != null) {
                NotificationHelper.showEventTitle(playerRef,
                        "TITRE DEBLOQUE", title.getDisplayName(), true);
            }
        }

        commandBuffer.putComponent(ref, type, data);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
