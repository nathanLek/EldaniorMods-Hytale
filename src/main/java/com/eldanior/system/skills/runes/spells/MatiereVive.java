package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Rune de la Matiere Vive (Spatial, Epic, rune de predilection du Demiurge Supreme) : 6 blocs de surface du point vise
 * (20 blocs, rayon 3) sont arraches (case videe, entite-bloc) et prennent vie 10 s. Chacun bondit (parabole 0,6 s,
 * 2,5 blocs) vers l'ennemi le plus proche de lui (15 blocs, cible differente si possible) : a l'arrivee, si l'ennemi est a
 * 1,8 bloc, 60 + recul. Trois impacts puis le bloc tombe en poussiere. Sol restaure a la fin.
 * Fiche de conception : scripts/runes/specs/Rune_MatiereVive.json
 */
public final class MatiereVive implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, HOP_MS = 600, REST_MS = 200;
    private static final double RANGE = 20.0, PICK_RADIUS = 3.0, HUNT = 15.0, HIT = 1.8, HOP_HEIGHT = 2.5, KNOCK = 8.0;
    private static final int STONES = 6, IMPACTS = 3;
    private static final float DAMAGE = 60f;

    // ===== Assets =====
    private static final String FX_DUST = "Gaia_Dust_Burst", FX_CRUMBS = "Block_Break_Dust";
    private static final String SFX_WAKE = "SFX_Golem_Earth_Wake", SFX_HIT = "SFX_Golem_Earth_Swing_Impact";

    @Override public String id() { return "MatiereVive"; }

    /** Un bloc vivant : position courante, bond en cours, cible et impacts. */
    private static final class Stone {
        final Ref<EntityStore> ref;
        final Vector3d pos;
        Vector3d from, to;
        Ref<EntityStore> target;
        long hopAt, restUntil;
        int impacts;
        boolean done;
        Stone(Ref<EntityStore> ref, Vector3d pos) { this.ref = ref; this.pos = pos; }
    }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        if (SpellFx.position(caster, store) == null || world == null) return null;
        Vector3d center = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        List<int[]> candidates = new ArrayList<>();
        int px = (int) Math.floor(center.x), py = (int) Math.floor(center.y), pz = (int) Math.floor(center.z), r = (int) PICK_RADIUS;
        for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
            if (Math.hypot(dx, dz) > PICK_RADIUS) continue;
            int x = px + dx, z = pz + dz;
            for (int y = py + 1; y >= py - 3; y--) {
                BlockType t = WorldBlockUtil.getBlockType(world, x, y, z), above = WorldBlockUtil.getBlockType(world, x, y + 1, z);
                if (t == null || above == null) break;
                if (t.getMaterial() != BlockMaterial.Solid || above.getMaterial() == BlockMaterial.Solid) continue;
                String id = t.getId();
                if (!id.startsWith("Plant_") && !TerrainRestore.isKept(world, id, x, y, z) && !TerrainRestore.isProtectedArea(world.getName(), x, y, z)
                        && WorldBlockUtil.getFluidIndex(world, x, y + 1, z) <= 0) candidates.add(new int[]{x, y, z});
                break;
            }
        }
        Collections.shuffle(candidates);
        long now = System.currentTimeMillis();
        TerrainRestore.Restore batch = TerrainRestore.open(world, now + DURATION_MS + 2_000);
        List<Stone> stones = new ArrayList<>();
        List<int[]> cells = new ArrayList<>();
        for (int[] c : candidates) {
            if (stones.size() >= STONES) break;
            String id = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]).getId();
            Vector3d at = new Vector3d(c[0] + 0.5, c[1] + 0.5, c[2] + 0.5);
            Ref<EntityStore> ref = SpellFx.spawnBlock(id, at, store, cb);
            if (ref == null) continue;
            Stone s = new Stone(ref, new Vector3d(at).add(0, 1, 0));
            s.restUntil = now + 300 + stones.size() * 100L;                      // ils s'eveillent l'un apres l'autre
            stones.add(s);
            cells.add(c);
            SpellFx.fx(FX_DUST, at, 0.8f, store, cb);
        }
        if (stones.isEmpty()) return null;
        world.execute(() -> {
            try {
                for (int[] c : cells) {
                    BlockType t = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]);
                    if (t != null) TerrainRestore.place(world, batch, c[0], c[1], c[2], t.getId(), TerrainRestore.EMPTY, 0);
                }
                TerrainRestore.save();
            } catch (Exception e) {
                EldaniorLogger.error("MatiereVive: sol", e);
            }
        });
        SpellFx.sound(SFX_WAKE, center, cb);
        return new Living(world, batch, stones, now);
    }

    private static final class Living implements Instance {
        final World world;
        final TerrainRestore.Restore batch;
        final List<Stone> stones;
        final long startAt;

        Living(World world, TerrainRestore.Restore batch, List<Stone> stones, long startAt) { this.world = world; this.batch = batch; this.stones = stones; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null || now - startAt >= DURATION_MS) return true;
            boolean any = false;
            for (Stone s : stones) {
                if (s.done) continue;
                any = true;
                if (s.from != null) { hop(caster, s, now, store, cb); continue; }
                SpellFx.hold(s.ref, s.pos, null, store);
                if (now >= s.restUntil) aim(caster, s, now, store);
            }
            return !any;
        }

        /** Prend pour cible l'ennemi le plus proche (de preference pas deja chasse) et bondit. */
        private void aim(Ref<EntityStore> caster, Stone s, long now, Store<EntityStore> store) {
            Set<Ref<EntityStore>> taken = new HashSet<>();
            for (Stone o : stones) if (o != s && !o.done && o.target != null) taken.add(o.target);
            Ref<EntityStore> best = null, fallback = null;
            double bestD = Double.MAX_VALUE, fallD = Double.MAX_VALUE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, s.pos, HUNT, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || RuneSpellRuntime.isSummon(e)) continue;
                double d = p.distance(s.pos);
                if (!taken.contains(e) && d < bestD) { bestD = d; best = e; }
                if (d < fallD) { fallD = d; fallback = e; }
            }
            Ref<EntityStore> t = best != null ? best : fallback;
            Vector3d tp = t != null ? SpellFx.position(t, store) : null;
            if (tp == null) { s.restUntil = now + REST_MS; return; }
            s.target = t;
            s.from = new Vector3d(s.pos);
            s.to = new Vector3d(tp).add(0, 0.6, 0);
            s.hopAt = now;
        }

        /** Bond parabolique ; impact a l'arrivee. */
        private void hop(Ref<EntityStore> caster, Stone s, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            double tau = Math.min(1.0, (now - s.hopAt) / (double) HOP_MS);
            s.pos.set(s.from).lerp(s.to, tau).add(0, HOP_HEIGHT * 4 * tau * (1 - tau), 0);
            SpellFx.hold(s.ref, s.pos, null, store);
            if (tau < 1.0) return;
            Vector3d origin = s.from;
            s.from = null;
            s.restUntil = now + REST_MS;
            Vector3d tp = s.target != null ? SpellFx.position(s.target, store) : null;
            if (tp != null && new Vector3d(tp).add(0, 0.6, 0).distance(s.pos) <= HIT) {
                Vector3d push = new Vector3d(tp.x - origin.x, 0, tp.z - origin.z);
                if (push.lengthSquared() < 1e-4) push.set(1, 0, 0);
                SpellFx.damage(caster, s.target, "Bludgeoning", DAMAGE, push.normalize().mul(KNOCK).add(0, 3, 0), "Impact_Light", cb);
                SpellFx.fx(FX_CRUMBS, s.pos, 0.9f, store, cb);
                SpellFx.sound(SFX_HIT, s.pos, cb);
                s.impacts++;
            }
            if (s.impacts >= IMPACTS) crumble(s, store, cb);
        }

        private void crumble(Stone s, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            s.done = true;
            SpellFx.fx(FX_CRUMBS, s.pos, 1.4f, store, cb);
            SpellFx.fx(FX_DUST, s.pos, 0.8f, store, cb);
            SpellFx.remove(s.ref, cb);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Stone s : stones) SpellFx.remove(s.ref, cb);
            world.execute(() -> { try { TerrainRestore.restoreNow(world, batch); TerrainRestore.save(); } catch (Exception e) { EldaniorLogger.error("MatiereVive: fin", e); } });
        }

        @Override
        public void abort() {
            for (Stone s : stones) RuneSpellRuntime.removeLater(s.ref);
            world.execute(() -> TerrainRestore.restoreNow(world, batch));
        }
    }
}
