package com.eldanior.system.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

/**
 * Depuis l'API 0.7.x, World n'expose plus getBlockType/setBlock directement :
 * il faut passer par la section de chunk (ChunkStore) et ses composants ECS.
 */
public final class WorldBlockUtil {

    private WorldBlockUtil() {}

    public static BlockType getBlockType(World world, int x, int y, int z) {
        ChunkStore chunkStore = world.getChunkStore();
        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(x, y, z);
        if (sectionRef == null || !sectionRef.isValid()) return null;

        Store<ChunkStore> store = chunkStore.getStore();
        BlockSection blockSection = store.getComponent(sectionRef, BlockSection.getComponentType());
        if (blockSection == null) return null;

        int blockId = blockSection.get(x, y, z);
        return BlockType.getAssetMap().getAsset(blockId);
    }

    /** Index de rotation du bloc (0 = aucune), -1 si la section n'est pas chargee. */
    public static int getRotationIndex(World world, int x, int y, int z) {
        BlockSection blockSection = section(world, x, y, z);
        return blockSection != null ? blockSection.getRotationIndex(x, y, z) : -1;
    }

    /** Remplissage (filler) : != 0 pour les parties secondaires d'un bloc multi-cases, -1 si non charge. */
    public static int getFiller(World world, int x, int y, int z) {
        BlockSection blockSection = section(world, x, y, z);
        return blockSection != null ? blockSection.getFiller(x, y, z) : -1;
    }

    private static BlockSection section(World world, int x, int y, int z) {
        ChunkStore chunkStore = world.getChunkStore();
        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(x, y, z);
        if (sectionRef == null || !sectionRef.isValid()) return null;
        return chunkStore.getStore().getComponent(sectionRef, BlockSection.getComponentType());
    }

    /**
     * Pose un fluide (ex. "Lava_Source") a son niveau maximal, ou le retire (fluidId null). Le fluide n'est PAS mis
     * en ecoulement (pas de setTicking) : il reste en place, sans se propager.
     */
    public static boolean setFluid(World world, int x, int y, int z, String fluidId) {
        int id = 0;
        byte level = 0;
        if (fluidId != null) {
            id = Fluid.getAssetMap().getIndex(fluidId);
            Fluid fluid = id > 0 ? Fluid.getAssetMap().getAsset(id) : null;
            if (fluid == null) return false;
            level = (byte) fluid.getMaxFluidLevel();
        }
        FluidSection fluids = fluidSection(world, x, y, z);
        return fluids != null && fluids.setFluid(x, y, z, id, level);
    }

    /** Identifiant (index) du fluide present, 0 si aucun, -1 si la section n'est pas chargee. */
    public static int getFluidIndex(World world, int x, int y, int z) {
        FluidSection fluids = fluidSection(world, x, y, z);
        return fluids != null ? fluids.getFluidId(x, y, z) : -1;
    }

    private static FluidSection fluidSection(World world, int x, int y, int z) {
        ChunkStore chunkStore = world.getChunkStore();
        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(x, y, z);
        if (sectionRef == null || !sectionRef.isValid()) return null;
        return chunkStore.getStore().getComponent(sectionRef, FluidSection.getComponentType());
    }

    public static void setBlock(World world, int x, int y, int z, String blockTypeId) {
        setBlock(world, x, y, z, blockTypeId, 0);
    }

    public static void setBlock(World world, int x, int y, int z, String blockTypeId, int rotation) {
        BlockType blockType = BlockType.fromString(blockTypeId);
        if (blockType == null) return;

        ChunkStore chunkStore = world.getChunkStore();
        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(x, y, z);
        if (sectionRef == null || !sectionRef.isValid()) return;

        BlockOperations.setBlock(chunkStore, sectionRef, x, y, z,
                BlockType.getAssetMap().getIndex(blockType.getId()), blockType, rotation, 0, 0);
    }
}
