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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune de l'Eruption Primordiale (Feu, Unique, rune de predilection du Primordial) : 12 s au point vise (rayon 10,
 * 25 blocs). Six geysers (1 s puis toutes les 1,8 s), de preference sous un ennemi (pas l'un des 2 derniers vises) :
 * alerte Muspelheim_Impact_Zone 0,8 s, puis colonne de feu : 180 + projection + Elda_Brulure (r 2,5). Chaque geyser
 * laisse une mare 3x3 de Rock_Volcanic_Cracked_Lava : 15 degats / 0,5 s + brulure. Sol restaure a la fin.
 * Fiche de conception : scripts/runes/specs/Rune_EruptionPrimordiale.json
 */
public final class EruptionPrimordiale implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 12_000, FIRST_MS = 1_000, EVERY_MS = 1_800, ALERT_MS = 800, ALERT_FX_EVERY_MS = 200, POOL_EVERY_MS = 500;
    private static final double RANGE = 25.0, RADIUS = 10.0, BLAST = 2.5, POOL = 1.8, LIFT = 14.0;
    private static final int GEYSERS = 6, MEMORY = 2;
    private static final float GEYSER = 180f, POOL_DAMAGE = 15f;

    // ===== Assets =====
    private static final String LAVA = "Rock_Volcanic_Cracked_Lava", BURN = "Elda_Brulure";
    private static final String FX_ALERT = "Muspelheim_Impact_Zone", FX_PILLAR = "Muspelheim_Fire_Pillar", FX_FIRE = "Impact_Fire";
    private static final String SFX_GEYSER = "SFX_Emit_Goblin_Flame_Quadrant_Blast";

    @Override public String id() { return "EruptionPrimordiale"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        if (SpellFx.position(caster, store) == null || world == null) return null;
        Vector3d center = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        long now = System.currentTimeMillis();
        SpellFx.fx(FX_FIRE, center, 1.5f, store, cb);
        SpellFx.shake(center, 20, "Impact_Light", store);
        return new Eruption(world, center, TerrainRestore.open(world, now + DURATION_MS), now);
    }

    /** Un geyser : alerte puis jaillissement. */
    private static final class Geyser {
        final Vector3d at;
        final long eruptAt;
        long lastAlert;
        boolean erupted;
        Geyser(Vector3d at, long eruptAt) { this.at = at; this.eruptAt = eruptAt; }
    }

    private static final class Eruption implements Instance {
        final World world;
        final Vector3d center;
        final TerrainRestore.Restore batch;
        final long startAt;
        final List<Geyser> geysers = new ArrayList<>();
        final List<Vector3d> pools = new ArrayList<>();
        final Deque<Ref<EntityStore>> recent = new ArrayDeque<>();
        long lastPool;

        Eruption(World world, Vector3d center, TerrainRestore.Restore batch, long startAt) {
            this.world = world; this.center = center; this.batch = batch; this.startAt = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null || now - startAt >= DURATION_MS) return true;
            long t = now - startAt;
            if (geysers.size() < GEYSERS && t >= FIRST_MS + geysers.size() * EVERY_MS - ALERT_MS) geysers.add(new Geyser(pick(caster, store), now + ALERT_MS));
            for (Geyser g : geysers) {
                if (g.erupted) continue;
                if (now >= g.eruptAt) { erupt(caster, g, store, cb); continue; }
                if (now - g.lastAlert >= ALERT_FX_EVERY_MS) { g.lastAlert = now; SpellFx.fx(FX_ALERT, new Vector3d(g.at).add(0, 0.1, 0), (float) (BLAST / 3.0), store, cb); }
            }
            if (now - lastPool >= POOL_EVERY_MS && !pools.isEmpty()) { lastPool = now; burnPools(caster, store, cb); }
            return false;
        }

        /** Sous un ennemi de la zone (pas l'un des derniers vises), sinon un point au hasard. */
        private Vector3d pick(Ref<EntityStore> caster, Store<EntityStore> store) {
            List<Ref<EntityStore>> enemies = SpellFx.enemiesAround(caster, center, RADIUS + 3, store);
            Collections.shuffle(enemies);
            for (Ref<EntityStore> e : enemies) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || recent.contains(e) || RuneSpellRuntime.isSummon(e) || SpellFx.horizontalDistance(p, center) > RADIUS) continue;
                recent.addLast(e);
                if (recent.size() > MEMORY) recent.removeFirst();
                return SpellFx.ground(world, p);
            }
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            double a = rng.nextDouble(Math.PI * 2), r = RADIUS * Math.sqrt(rng.nextDouble());
            return SpellFx.ground(world, new Vector3d(center).add(Math.cos(a) * r, 0, Math.sin(a) * r));
        }

        private void erupt(Ref<EntityStore> caster, Geyser g, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            g.erupted = true;
            SpellFx.fx(FX_PILLAR, g.at, 1.2f, store, cb);
            SpellFx.fx(FX_FIRE, new Vector3d(g.at).add(0, 0.5, 0), 1.5f, store, cb);
            SpellFx.sound(SFX_GEYSER, g.at, cb);
            SpellFx.shake(g.at, 12, "Impact_Strong", store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, g.at, BLAST + 2, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, g.at) > BLAST || Math.abs(p.y - g.at.y) > 3) continue;
                SpellFx.damage(caster, e, "Fire", GEYSER, new Vector3d(0, LIFT, 0), "Impact_Strong", cb);
                SpellFx.effect(e, BURN, cb);
            }
            pools.add(new Vector3d(g.at));
            int cx = (int) Math.floor(g.at.x), cy = (int) Math.floor(g.at.y), cz = (int) Math.floor(g.at.z);
            world.execute(() -> {                                                  // mare de lave : surface en roche craquelee
                try {
                    for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                        int x = cx + dx, z = cz + dz;
                        for (int y = cy + 1; y >= cy - 3; y--) {
                            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z), above = WorldBlockUtil.getBlockType(world, x, y + 1, z);
                            if (t == null || t.getMaterial() != BlockMaterial.Solid || above == null || above.getMaterial() == BlockMaterial.Solid) continue;
                            String id = t.getId();
                            if (!id.equals(LAVA) && !TerrainRestore.isKept(world, id, x, y, z) && !TerrainRestore.isProtectedArea(world.getName(), x, y, z)
                                    && WorldBlockUtil.getFluidIndex(world, x, y + 1, z) <= 0)
                                TerrainRestore.place(world, batch, x, y, z, id, LAVA, 0);
                            break;
                        }
                    }
                    TerrainRestore.save();
                } catch (Exception e) {
                    EldaniorLogger.error("EruptionPrimordiale: mare", e);
                }
            });
        }

        private void burnPools(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + 4, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                for (Vector3d pool : pools) {
                    if (SpellFx.horizontalDistance(p, pool) > POOL || Math.abs(p.y - pool.y) > 2.5) continue;
                    SpellFx.damage(caster, e, "Fire", POOL_DAMAGE, null, null, cb);
                    SpellFx.effect(e, BURN, cb);
                    SpellFx.fx(FX_FIRE, new Vector3d(p).add(0, 0.5, 0), 0.6f, store, cb);
                    break;
                }
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }

        @Override
        public void abort() { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }
    }
}
