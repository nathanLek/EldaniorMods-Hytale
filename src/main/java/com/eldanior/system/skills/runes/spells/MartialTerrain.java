package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Le Ki des plus hauts paliers de la voie martiale marque le terrain (retours du 2026-11-10) :
 *   fissure : la lame de Ki du Dieu Martial fend le sol devant lui (blocs retires puis remis par TerrainRestore) ;
 *   pierres en levitation : la Meditation du Maitre Supreme et du Dieu Martial souleve des blocs du sol alentour (copies en
 *   entites-blocs, le terrain n'est pas touche) qui flottent et tournent lentement autour du meditant.
 * Jamais dans une zone protegee (villes, parcelles), ni sur un bloc conserve (coffres, portes...), ni dans un fluide.
 */
final class MartialTerrain {

    private MartialTerrain() {}

    private static final long RESTORE_MS = 25_000;
    private static final String[] NEVER = {"Bedrock", "Barrier"};

    private static boolean breakable(World world, int x, int y, int z, BlockType t) {
        if (t == null || t.getMaterial() != BlockMaterial.Solid || TerrainRestore.isEmpty(world, x, y, z)) return false;
        String id = t.getId();
        return !TerrainRestore.isProtectedArea(world.getName(), x, y, z) && !TerrainRestore.contains(id, NEVER)
                && !TerrainRestore.isKept(world, id, x, y, z) && WorldBlockUtil.getFluidIndex(world, x, y, z) <= 0;
    }

    /** Premier bloc plein sous le point (jusqu'a 5 blocs plus bas), sinon Integer.MIN_VALUE. */
    static int groundY(World world, int x, double y, int z) {
        for (int yy = (int) Math.floor(y) + 1; yy >= (int) Math.floor(y) - 5; yy--) {
            BlockType t = WorldBlockUtil.getBlockType(world, x, yy, z);
            if (t != null && t.getMaterial() == BlockMaterial.Solid) return yy;
        }
        return Integer.MIN_VALUE;
    }

    /** Fissure de `length` blocs dans la direction horizontale `dir`, a partir de `from` (pieds), retablie apres RESTORE_MS. */
    static void fissure(World world, Vector3d from, Vector3d dir, double length, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (world == null) return;
        Vector3d f = new Vector3d(dir.x, 0, dir.z);
        if (f.lengthSquared() < 1e-6) return;
        f.normalize();
        Vector3d side = new Vector3d(-f.z, 0, f.x);
        List<int[]> cells = new ArrayList<>();
        var r = ThreadLocalRandom.current();
        for (double d = 1.5; d <= length; d += 0.8) {
            Vector3d p = new Vector3d(from).add(new Vector3d(f).mul(d)).add(new Vector3d(side).mul(Math.sin(d * 1.3) * 0.6));
            int x = (int) Math.floor(p.x), z = (int) Math.floor(p.z);
            int gy = groundY(world, x, from.y, z);
            if (gy == Integer.MIN_VALUE) continue;
            cells.add(new int[]{x, gy, z});
            if (r.nextFloat() < 0.6) cells.add(new int[]{x, gy - 1, z});            // la faille s'enfonce par endroits
            if (((int) (d * 10)) % 3 == 0) SpellFx.fx("Gaia_Dust_Burst", new Vector3d(x + 0.5, gy + 1, z + 0.5), 0.7f, store, cb);
        }
        world.execute(() -> {
            TerrainRestore.Restore batch = null;
            for (int[] c : cells) {
                try {
                    BlockType t = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]);
                    if (!breakable(world, c[0], c[1], c[2], t)) continue;
                    if (batch == null) batch = TerrainRestore.open(world, System.currentTimeMillis() + RESTORE_MS);
                    TerrainRestore.place(world, batch, c[0], c[1], c[2], t.getId(), TerrainRestore.EMPTY, 0);
                } catch (Exception ignored) { }
            }
            if (batch != null) TerrainRestore.save();
        });
    }

    /**
     * Bloc que la Percee peut creuser : tout bloc non vide (plantes et feuilles comprises), hors fluide, zone protegee,
     * bloc conserve (coffres, portes...) et bedrock.
     */
    private static boolean borable(World world, int x, int y, int z, BlockType t) {
        if (t == null || TerrainRestore.isEmpty(world, x, y, z)) return false;
        String id = t.getId();
        return !TerrainRestore.isProtectedArea(world.getName(), x, y, z) && !TerrainRestore.contains(id, NEVER)
                && !TerrainRestore.isKept(world, id, x, y, z) && WorldBlockUtil.getFluidIndex(world, x, y, z) <= 0;
    }

    /**
     * Cases d'un tube d'axe from + dir * d (d de d0 a d1) et de rayon radius (Percee du Bokken, 2026-11-11). Rend les cases
     * dans l'ordre du trajet, sans doublon ; seen sert d'un appel a l'autre (le tube avance a chaque tick).
     */
    static List<int[]> tube(Vector3d from, Vector3d dir, double d0, double d1, double radius, java.util.Set<Long> seen) {
        Vector3d f = new Vector3d(dir).normalize();
        Vector3d u = Math.abs(f.y) > 0.9 ? new Vector3d(1, 0, 0) : new Vector3d(0, 1, 0);
        Vector3d a = new Vector3d(f).cross(u).normalize(), b = new Vector3d(f).cross(a).normalize();
        List<int[]> out = new ArrayList<>();
        double step = 0.45;
        for (double d = d0; d <= d1; d += step)
            for (double x = -radius; x <= radius + 1e-6; x += step)
                for (double y = -radius; y <= radius + 1e-6; y += step) {
                    if (x * x + y * y > radius * radius + 0.05) continue;
                    Vector3d p = new Vector3d(from).add(new Vector3d(f).mul(d)).add(new Vector3d(a).mul(x)).add(new Vector3d(b).mul(y));
                    int bx = (int) Math.floor(p.x), by = (int) Math.floor(p.y), bz = (int) Math.floor(p.z);
                    long k = (((long) bx & 0x3FFFFFL) << 42) | (((long) by & 0xFFFFFL) << 22) | ((long) bz & 0x3FFFFFL);
                    if (seen.add(k)) out.add(new int[]{bx, by, bz});
                }
        return out;
    }

    /** Ids des blocs creusables parmi des cases (lus au tick, pour les debris) ; null pour une case intacte. */
    static String[] borableIds(World world, List<int[]> cells) {
        String[] ids = new String[cells.size()];
        for (int i = 0; i < cells.size(); i++) {
            int[] c = cells.get(i);
            try {
                BlockType t = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]);
                if (borable(world, c[0], c[1], c[2], t)) ids[i] = t.getId();
            } catch (Exception ignored) { }
        }
        return ids;
    }

    /** Creuse les cases (retablies apres restoreMs) : appel hors tick ECS via world.execute. */
    static void bore(World world, List<int[]> cells, long restoreMs) {
        if (world == null || cells.isEmpty()) return;
        world.execute(() -> {
            TerrainRestore.Restore batch = null;
            for (int[] c : cells) {
                try {
                    BlockType t = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]);
                    if (!borable(world, c[0], c[1], c[2], t)) continue;
                    if (batch == null) batch = TerrainRestore.open(world, System.currentTimeMillis() + restoreMs);
                    TerrainRestore.place(world, batch, c[0], c[1], c[2], t.getId(), TerrainRestore.EMPTY, 0);
                } catch (Exception ignored) { }
            }
            if (batch != null) TerrainRestore.save();
        });
    }

    /** Bloc plein du sol pres de `center` (pour une pierre en levitation), ou null. */
    static String groundBlock(World world, Vector3d center, double radius) {
        var r = ThreadLocalRandom.current();
        for (int tries = 0; tries < 6; tries++) {
            int x = (int) Math.floor(center.x + r.nextDouble(-radius, radius)), z = (int) Math.floor(center.z + r.nextDouble(-radius, radius));
            int gy = groundY(world, x, center.y, z);
            if (gy == Integer.MIN_VALUE) continue;
            BlockType t = WorldBlockUtil.getBlockType(world, x, gy, z);
            if (t != null && !TerrainRestore.contains(t.getId(), NEVER) && !TerrainRestore.isKept(world, t.getId(), x, gy, z)) return t.getId();
        }
        return null;
    }

    /** Pierres qui flottent autour d'un point : entites-blocs tenues par le code (hold), retirees par clear. */
    static final class Floating {
        final List<Ref<EntityStore>> stones = new ArrayList<>();
        final List<double[]> orbits = new ArrayList<>();                         // {angle, rayon, hauteur, vitesse}

        void spawn(World world, Vector3d center, int count, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            var r = ThreadLocalRandom.current();
            for (int i = 0; i < count; i++) {
                String id = groundBlock(world, center, 4.0);
                if (id == null) continue;
                double[] o = {r.nextDouble(0, Math.PI * 2), r.nextDouble(2.0, 4.5), r.nextDouble(0.6, 2.8), r.nextDouble(0.25, 0.6)};
                Ref<EntityStore> s = SpellFx.spawnBlock(id, new Vector3d(center).add(Math.cos(o[0]) * o[1], 0.2, Math.sin(o[0]) * o[1]), store, cb);
                if (s == null) continue;
                SpellFx.notSaved(s, store, cb);
                stones.add(s);
                orbits.add(o);
            }
        }

        /** Les pierres montent (rise 0..1) puis tournent lentement en ondulant. */
        void tick(Vector3d center, double t, double rise, Store<EntityStore> store) {
            for (int i = 0; i < stones.size(); i++) {
                double[] o = orbits.get(i);
                double a = o[0] + t * o[3];
                double h = 0.2 + (o[2] + Math.sin(t * 1.3 + i) * 0.25) * rise;
                SpellFx.hold(stones.get(i), new Vector3d(center).add(Math.cos(a) * o[1], h, Math.sin(a) * o[1]), null, store);
            }
        }

        void clear(CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> s : stones) SpellFx.remove(s, cb);
            stones.clear();
            orbits.clear();
        }
    }
}
