package com.eldanior.system.quest;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.hud.CombinedHud;
import com.eldanior.system.party.PartyManager;
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

/**
 * Systeme unifie qui gere le CombinedHud (quetes + groupe).
 * Cree, met a jour, et supprime le HUD selon l'etat du joueur.
 */
public class QuestHudUpdateSystem extends EntityTickingSystem<EntityStore> {

    private int tickCounter = 0;
    private static Field uuidField;
    static {
        try { uuidField = PlayerRef.class.getDeclaredField("uuid"); uuidField.setAccessible(true); }
        catch (Exception ignored) {}
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        tickCounter++;
        if (tickCounter % 15 != 0) return; // ~0.75s

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        Player player = store.getComponent(entityRef, Player.getComponentType());
        if (player == null) return;

        PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
        if (playerRef == null) return;

        UUID playerUUID = extractUUID(playerRef);
        if (playerUUID == null) return;

        // Verifier si le joueur a besoin d'un HUD
        PlayerQuest active = QuestManager.getActiveQuest(playerUUID);
        boolean hasMainQuest = false;
        for (PlayerQuest pq : QuestManager.getPlayerQuests(playerUUID)) {
            QuestModel model = QuestManager.getQuest(pq.getQuestId());
            if (model != null && model.isMainStory() && !pq.isCompleted()) {
                hasMainQuest = true;
                break;
            }
        }
        boolean hasParty = PartyManager.hasParty(playerUUID);
        boolean needsHud = active != null || hasMainQuest || hasParty;

        CustomUIHud currentHud = player.getHudManager().getCustomHud();
        PlayerLevelData pData = store.getComponent(entityRef, EldaniorSystem.get().getPlayerLevelDataType());

        if (needsHud) {
            if (currentHud instanceof CombinedHud combined) {
                // Mettre a jour les donnees
                combined.setCachedData(pData, player);
                currentHud.show();
            } else {
                // Creer le CombinedHud (remplace tout autre HUD)
                CombinedHud newHud = new CombinedHud(playerRef, playerUUID);
                newHud.setCachedData(pData, player);
                player.getHudManager().setCustomHud(playerRef, newHud);
            }
        } else {
            // Pas besoin de HUD
            if (currentHud instanceof CombinedHud) {
                player.getHudManager().setCustomHud(playerRef, null);
            }
        }
    }

    private UUID extractUUID(PlayerRef playerRef) {
        if (uuidField == null) return null;
        try { return (UUID) uuidField.get(playerRef); }
        catch (Exception e) { return null; }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
