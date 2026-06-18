package com.eldanior.system.TreasureChest.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.TitleManager;
import com.eldanior.system.titles.models.TitleModel;
import com.eldanior.system.TreasureChest.components.PlayerChestData;
import com.eldanior.system.TreasureChest.resources.TreasureChestConfig;
import com.eldanior.system.TreasureChest.resources.TreasureChestTemplate;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.util.TempAssetIdUtil;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;

public class TreasureChestRangeSystem extends EntityTickingSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final double PARTICLE_RADIUS_SQ = 15.0 * 15.0;
    private static final double DISCOVERY_RADIUS_SQ = 5.0 * 5.0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        commandBuffer.run(deferredStore -> {
            try {
                Player player = archetypeChunk.getComponent(index, Player.getComponentType());
                PlayerChestData playerChestData = archetypeChunk.getComponent(index, EldaniorSystem.get().getPlayerChestDataType());
                PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());

                if (player == null || playerChestData == null || playerRef == null) return;

                playerChestData.incrementTimer();
                if (playerChestData.getTimer() < 10) return;
                playerChestData.resetTimer();

                assert player.getReference() != null;
                TransformComponent transform = deferredStore.getComponent(player.getReference(), TransformComponent.getComponentType());
                if (transform == null) return;

                Vector3d playerPos = transform.getPosition();
                World world = player.getWorld();
                if (world == null) return;

                ChunkStore chunkStore = world.getChunkStore();
                TreasureChestConfig config = chunkStore.getStore().getResource(EldaniorSystem.CONFIG_RESOURCE_TYPE);
                TreasureChestTemplate template = chunkStore.getStore().getResource(EldaniorSystem.CHEST_TEMPLATE_TYPE);

                int currentDay = (int) LocalDate.now().toEpochDay();
                if (config.getNextLootReset() != -1 && currentDay >= config.getNextLootReset()) {
                    config.setNextLootReset(currentDay + config.getNextLootResetInterval());
                }

                if (!config.isParticlesAppear()) return;

                String worldName = world.getName();

                Set<String> keys = template.getTemplateKeys();
                if (keys.isEmpty()) return;

                Color pColor = parseColor(config.getParticlesColor());

                for (String key : keys) {
                    String[] parts = key.split(",");
                    if (parts.length != 3) continue;

                    int x, y, z;
                    try {
                        x = Integer.parseInt(parts[0]);
                        y = Integer.parseInt(parts[1]);
                        z = Integer.parseInt(parts[2]);
                    } catch (NumberFormatException e) { /* format invalide */
                        continue;
                    }

                    double dx = playerPos.x - x;
                    double dy = playerPos.y - y;
                    double dz = playerPos.z - z;
                    double distSq = (dx * dx) + (dy * dy) + (dz * dz);

                    if (distSq > PARTICLE_RADIUS_SQ) continue;

                    if (!playerChestData.isDiscovered(x, y, z, worldName) && distSq < DISCOVERY_RADIUS_SQ) {
                        playerChestData.setDiscovered(x, y, z, worldName, true);

                        // Incrementer le compteur de coffres decouverts + check titres
                        PlayerLevelData levelData = deferredStore.getComponent(player.getReference(),
                                EldaniorSystem.get().getPlayerLevelDataType());
                        if (levelData != null) {
                            levelData.addChestDiscovered();
                            // Progression skills à la découverte d'un coffre
                            for (var passive : levelData.getActivePassives()) {
                                String pName = passive.name();
                                if ("GOOD_OMEN".equals(pName) || "TREASURE_HUNTER".equals(pName)) {
                                    levelData.addSkillProc(pName);
                                    com.eldanior.system.Leveling.utils.NotificationHelper.sendNotification(
                                            playerRef, "<color:green>+" + pName + " progression</color>",
                                            com.hypixel.hytale.protocol.packets.interface_.NotificationStyle.Success);
                                }
                            }
                            java.util.List<TitleModel> newTitles = TitleManager.checkTitleUnlocks(levelData);
                            for (TitleModel title : newTitles) {
                                levelData.addTitle(title.getId());
                                com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(
                                        playerRef, "TITRE DEBLOQUE", title.getDisplayName(), true);
                            }
                            deferredStore.putComponent(player.getReference(),
                                    EldaniorSystem.get().getPlayerLevelDataType(), levelData);
                        }

                        if (config.isMessageAppear()) {
                            EventTitleUtil.showEventTitleToPlayer(playerRef,
                                    Message.raw("Coffre au trésor découvert !"),
                                    Message.raw("Zone : " + template.getDropList(x, y, z)),
                                    true);

                            // Utilisation de la bonne annotation pour une méthode "marked for removal"
                            @SuppressWarnings("removal")
                            int soundId = TempAssetIdUtil.getSoundEventIndex("SFX_Memories_Unlock_Local");
                            SoundUtil.playSoundEvent2dToPlayer(playerRef, soundId, SoundCategory.SFX);
                        }
                    }

                    ParticleUtil.spawnParticleEffect("Chest_Sparks",
                            new Vector3d(x + 0.5, y + 1.2, z + 0.5),
                            0, 0, 0, 1.0f, pColor,
                            Collections.singletonList(player.getReference()),
                            commandBuffer);
                }

            } catch (Exception e) {
                LOGGER.atSevere().withCause(e).log("Erreur dans TreasureChestRangeSystem");
            }
        });
    }

    private Color parseColor(String hex) {
        try {
            String h = hex.replace("#", "");
            return new Color(
                    (byte) Integer.parseInt(h.substring(0, 2), 16),
                    (byte) Integer.parseInt(h.substring(2, 4), 16),
                    (byte) Integer.parseInt(h.substring(4, 6), 16)
            );
        } catch (Exception e) {
            return new Color((byte) 255, (byte) 255, (byte) 255);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType(), EldaniorSystem.get().getPlayerChestDataType());
    }
}