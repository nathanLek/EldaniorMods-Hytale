package com.eldanior.system.quest;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.hud.CombinedHud;
import com.eldanior.system.party.PartyManager;
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
 * Systeme unifie qui gere le CombinedHud (quetes + groupe).
 * Cree, met a jour, et supprime le HUD selon l'etat du joueur.
 */
public class QuestHudUpdateSystem extends EntityTickingSystem<EntityStore> {

    private int tickCounter = 0;
    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        tickCounter++;

        // Verifier le reset journalier toutes les ~60s (sur le premier joueur seulement)
        if (tickCounter % 1200 == 0 && index == 0) {
            QuestManager.checkDailyReset();
        }

        // Sauvegarde periodique des quetes toutes les ~30s par joueur
        if (tickCounter % 600 == 0) {
            Ref<EntityStore> saveRef = archetypeChunk.getReferenceTo(index);
            if (saveRef.isValid()) {
                PlayerRef savePRef = store.getComponent(saveRef, PlayerRef.getComponentType());
                UUID saveUUID = savePRef != null ? extractUUID(savePRef) : null;
                if (saveUUID != null) {
                    PlayerLevelData saveData = store.getComponent(saveRef, EldaniorSystem.get().getPlayerLevelDataType());
                    if (saveData != null) {
                        String serialized = QuestManager.serializePlayerQuests(saveUUID);
                        String cooldowns = QuestManager.serializeCooldowns(saveUUID);
                        if (!serialized.equals(saveData.getQuestData()) || !cooldowns.equals(saveData.getCooldownData())) {
                            saveData.setQuestData(serialized);
                            saveData.setCooldownData(cooldowns);
                            commandBuffer.replaceComponent(saveRef, EldaniorSystem.get().getPlayerLevelDataType(), saveData);
                        }
                    }
                }
            }
        }

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

        CustomUIHud currentHud = player.getHudManager().getCustomHud("combined_hud");
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
                player.getHudManager().addCustomHud(playerRef, newHud);
            }
        } else {
            // Pas besoin de HUD
            if (currentHud instanceof CombinedHud) {
                player.getHudManager().removeCustomHud(playerRef, "combined_hud");
            }
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
