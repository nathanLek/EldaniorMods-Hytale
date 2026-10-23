package com.eldanior.system.skills.runes;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Entretien des runes, toutes les secondes :
 *  - don automatique de la Rune d'Aura (sac de runes) des que la dignite le permet,
 *  - restriction de classe a l'equipement : une rune non autorisee retourne dans le sac.
 */
public class RuneSystem extends EntityTickingSystem<EntityStore> {

    private float updateTimer = 0;
    private boolean shouldUpdate = false;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        if (index == 0) {
            updateTimer += dt;
            if (updateTimer >= 1.0f) {
                shouldUpdate = true;
                updateTimer = 0;
            } else {
                shouldUpdate = false;
            }
        }
        if (!shouldUpdate) return;

        Ref<EntityStore> playerRef = chunk.getReferenceTo(index);
        if (!playerRef.isValid()) return;

        PlayerLevelData data = store.getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());
        PlayerRef netRef = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (data == null || netRef == null) return;

        RuneManager.migrateLegacyRunes(playerRef, store); // runes divines renommees

        if (data.getDignity() >= RuneManager.AURA_RUNE_MIN_DIGNITY
                && RuneManager.grantAuraRuneIfMissing(playerRef, store)) {
            NotificationHelper.sendSuccess(netRef,
                    "<color:gold>Rune d'Aura debloquee !</color> <color:gray>Equipez-la depuis la fenetre des runes (/es runes).</color>");
        }

        if (RuneManager.enforceAbilitySlots(playerRef, store, data) > 0) {
            NotificationHelper.sendWarning(netRef,
                    "<color:red>Votre classe ne peut pas utiliser cette rune.</color> <color:gray>Elle a ete renvoyee dans votre sac de runes.</color>");
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
