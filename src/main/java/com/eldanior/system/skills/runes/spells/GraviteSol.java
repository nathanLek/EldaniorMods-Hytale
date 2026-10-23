package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import org.joml.Vector3d;

import java.util.List;
import java.util.Set;

/**
 * Terrain des sorts de gravite (Puits Gravitationnel, Autorite Gravitationnelle) : creuser au demi-bloc pres (demi-dalles),
 * tracer des fissures, trouver la surface. Toutes les ecritures passent par TerrainRestore (restaurees) ; les methodes qui
 * ecrivent s'appellent dans world.execute.
 */
final class GraviteSol {

    private GraviteSol() {}

    /**
     * Creuse la colonne (x, z) dont le dernier bloc plein est top, de depth blocs (multiple de 0.5) : les blocs au-dessus du
     * nouveau fond sont retires ; un fond a mi-bloc devient une demi-dalle, un fond plein est remplace par le materiau du fond.
     */
    static void dig(World world, TerrainRestore.Restore batch, int x, int top, int z, double depth, String full, String half) {
        double floorH = top + 1 - depth;
        for (int y = top; y >= (int) Math.floor(floorH); y--) {
            if (!editable(world, x, y, z)) return;
            String id = WorldBlockUtil.getBlockType(world, x, y, z).getId();
            TerrainRestore.place(world, batch, x, y, z, id, y >= floorH ? TerrainRestore.EMPTY : half, 0);
        }
        int below = (int) Math.floor(floorH) - 1;
        if (floorH == Math.floor(floorH) && editable(world, x, below, z))
            TerrainRestore.place(world, batch, x, below, z, WorldBlockUtil.getBlockType(world, x, below, z).getId(), full, 0);
    }

    /**
     * Trace d'une fissure (sans toucher au monde) : cases {x, z, profondeur} d'une entaille d'un bloc de large qui serpente en
     * s'amincissant (de startDepth a endDepth), avec des ramifications plus courtes.
     */
    static void crackPath(Vector3d at, double angle, double from, double length, double startDepth, double endDepth, double branchChance,
                          List<double[]> out, Set<Long> seen, java.util.Random rng, boolean canBranch) {
        double px = at.x + Math.cos(angle) * from, pz = at.z + Math.sin(angle) * from;
        for (double s = 0; s <= length; s += 0.5) {
            angle += (rng.nextDouble() - 0.5) * 0.35;
            px += Math.cos(angle) * 0.5;
            pz += Math.sin(angle) * 0.5;
            int x = (int) Math.floor(px), z = (int) Math.floor(pz);
            if (!seen.add(((long) x << 32) ^ (z & 0xffffffffL))) continue;
            double depth = Math.max(endDepth, Math.round((startDepth + (endDepth - startDepth) * s / length) * 2) / 2.0);
            out.add(new double[]{x, z, depth});
            if (canBranch && s > 1 && rng.nextDouble() < branchChance)
                crackPath(new Vector3d(px, at.y, pz), angle + (rng.nextBoolean() ? 0.7 : -0.7), 0, length * 0.4, Math.max(endDepth, depth - 0.5), endDepth, branchChance, out, seen, rng, false);
        }
    }

    static boolean editable(World world, int x, int y, int z) {
        BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
        return t != null && !TerrainRestore.isEmpty(world, x, y, z) && !TerrainRestore.isKept(world, t.getId(), x, y, z)
                && !TerrainRestore.isProtectedArea(world.getName(), x, y, z);
    }

    /** Premier bloc plein (hors plantes et feuillages) en descendant de 4 blocs au-dessus a 6 en dessous. */
    static int surface(World world, int x, int baseY, int z) {
        for (int y = baseY + 4; y >= baseY - 6; y--) {
            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
            if (t == null) return Integer.MIN_VALUE;
            if (TerrainRestore.isEmpty(world, x, y, z)) continue;
            String id = t.getId();
            if (id.startsWith("Plant_") || id.contains("Leaves")) continue;
            return y;
        }
        return Integer.MIN_VALUE;
    }
}
