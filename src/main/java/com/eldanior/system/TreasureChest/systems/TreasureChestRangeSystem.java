package com.eldanior.system.TreasureChest.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.TreasureChest.components.PlayerChestData;
import com.eldanior.system.TreasureChest.resources.TreasureChestConfig;
import com.eldanior.system.TreasureChest.resources.TreasureChestTemplate;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.util.TempAssetIdUtil;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.chunk.ZoneBiomeResult;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.time.LocalDate;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;

@SuppressWarnings({"deprecation", "removal", "unchecked", "ConstantConditions"})
public class TreasureChestRangeSystem extends EntityTickingSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        commandBuffer.run((deferredStore) -> {
            try {
                Player player = archetypeChunk.getComponent(index, Player.getComponentType());
                PlayerChestData playerChestData = archetypeChunk.getComponent(index, EldaniorSystem.get().getPlayerChestDataType());
                PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());

                if (player == null || playerChestData == null || playerRef == null) return;

                // On réduit à 10 ticks (0.5s) pour plus de réactivité lors des tests
                playerChestData.incrementTimer();
                if (playerChestData.getTimer() < 10) return;
                playerChestData.resetTimer();

                Vector3d playerPos = player.getTransformComponent().getPosition();
                World world = player.getWorld();
                ChunkStore chunkStore = world.getChunkStore();

                TreasureChestConfig config = chunkStore.getStore().getResource(EldaniorSystem.CONFIG_RESOURCE_TYPE);
                if (config == null) return;

                // 1. Reset Global
                int currentDay = (int) LocalDate.now().toEpochDay();
                if (config.getNextLootReset() != -1 && currentDay >= config.getNextLootReset()) {
                    config.setNextLootReset(currentDay + config.getNextLootResetInterval());
                    // TreasureResetManager.runGlobalReset(world.getEntityStore().getStore());
                }

                // 2. Détection
                ResourceType<ChunkStore, SpatialResource<Ref<ChunkStore>, ChunkStore>> spatialType = BlockStateModule.get().getItemContainerSpatialResourceType();
                SpatialResource<Ref<ChunkStore>, ChunkStore> spatial = chunkStore.getStore().getResource(spatialType);
                if (spatial == null) return;

                // --- PARTICULES (Rayon 15m) ---
                if (config.isParticlesAppear()) {
                    ObjectList<Ref<ChunkStore>> containers = SpatialResource.getThreadLocalReferenceList();
                    spatial.getSpatialStructure().collect(playerPos, 15.0, containers);

                    for (Ref<ChunkStore> ref : containers) {
                        if (ref == null || !ref.isValid()) continue;

                        ItemContainerState state = ref.getStore().getComponent(ref, BlockStateModule.get().getComponentType(ItemContainerState.class));
                        if (state == null) continue;

                        TreasureChestTemplate template = state.getReference().getStore().getResource(EldaniorSystem.CHEST_TEMPLATE_TYPE);
                        if (template == null || !template.hasTemplate(state.getBlockX(), state.getBlockY(), state.getBlockZ())) continue;

                        int x = state.getBlockX(), y = state.getBlockY(), z = state.getBlockZ();

                        // --- DÉCOUVERTE ---
                        double dx = playerPos.x - x;
                        double dy = playerPos.y - y;
                        double dz = playerPos.z - z;
                        double distSq = (dx * dx) + (dy * dy) + (dz * dz);

                        if (!playerChestData.isDiscovered(x, y, z, world.getName()) && distSq < 25) {
                            playerChestData.setDiscovered(x, y, z, world.getName(), true);
                            if (config.isMessageAppear()) {
                                EventTitleUtil.showEventTitleToPlayer(playerRef, Message.raw("Coffre au trésor découvert !"), Message.raw("Zone : " + template.getDropList(x, y, z)), true);
                                int soundId = TempAssetIdUtil.getSoundEventIndex("SFX_Memories_Unlock_Local");
                                SoundUtil.playSoundEvent2dToPlayer(playerRef, soundId, SoundCategory.SFX);
                            }
                        }

                        // --- AFFICHAGE PARTICULES ---
                        String colorHex = config.getParticlesColor().replace("#", "");
                        Color pColor = new Color((byte)255, (byte)255, (byte)255);
                        try {
                            pColor = new Color(
                                    (byte) Integer.parseInt(colorHex.substring(0, 2), 16),
                                    (byte) Integer.parseInt(colorHex.substring(2, 4), 16),
                                    (byte) Integer.parseInt(colorHex.substring(4, 6), 16)
                            );
                        } catch (Exception ignored) {}

                        ParticleUtil.spawnParticleEffect("Chest_Sparks", new Vector3d(x + 0.5, y + 1.2, z + 0.5), 0, 0, 0, 1.0f, pColor, Collections.singletonList(player.getReference()), commandBuffer);
                    }
                }

            } catch (Exception e) {
                LOGGER.atSevere().withCause(e).log("Erreur dans TreasureChestRangeSystem");
            }
        });
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        // Obligatoire : Le système doit cibler les entités qui ont le profil de loot
        return Query.and(Player.getComponentType(), EldaniorSystem.get().getPlayerChestDataType());
    }
}