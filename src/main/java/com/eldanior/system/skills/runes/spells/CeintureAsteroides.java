package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune de la Ceinture d'Asteroides (Spatial, Epic, rune de predilection du Mystique Superieur) : la gravite arrache
 * jusqu'a 8 blocs de surface autour du lanceur (2 a 5 blocs), qui montent puis tournent en orbite autour de lui pendant
 * 10 s. Chaque clic (gauche ou droit) projette le bloc le plus en avant vers le curseur : degats au premier ennemi touche,
 * eclats autour de l'impact. Les blocs retires sont restaures apres 60 s (jamais en ville ni sur une parcelle).
 * Fiche de conception : scripts/runes/specs/Rune_CeintureAsteroides.json
 */
public final class CeintureAsteroides implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, RISE_MS = 600, SHOT_MAX_MS = 1_500, SPAWN_GRACE_MS = 1_000, RESTORE_AFTER_MS = 60_000, AURA_EVERY_MS = 300;
    private static final int ROCKS = 8;
    private static final double PICK_MIN = 2.0, PICK_MAX = 5.0, ORBIT_RADIUS = 2.5, ORBIT_HEIGHT = 1.6, ORBIT_SPEED = 1.6, BOB = 0.35, TUMBLE = 90.0;
    private static final double SHOT_SPEED = 40.0, AIM_RANGE = 40.0, HIT_RADIUS = 1.3, SPLASH_RADIUS = 2.0;
    private static final float DAMAGE = 60f, SPLASH_DAMAGE = 30f;

    // ===== Assets =====
    private static final String FX_AURA = "Telekinesie_Aura", FX_DUST = "Gaia_Dust_Burst", FX_CRUMBS = "Block_Break_Dust";
    private static final String SFX_RIP = "SFX_Golem_Earth_Wake", SFX_SHOT = "SFX_Ability_Wind_Strike_Slash", SFX_IMPACT = "SFX_Golem_Earth_Slam_Impact";

    @Override public String id() { return "CeintureAsteroides"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (pos == null || world == null) return null;
        long now = System.currentTimeMillis();
        Belt belt = new Belt(caster, world, now);
        belt.rip(pos, store, cb);
        if (belt.rocks.isEmpty()) { EldaniorLogger.info("[CeintureAsteroides] aucun bloc a arracher"); return null; }
        SpellFx.fx(FX_DUST, pos, 2.5f, store, cb);
        SpellFx.sound(SFX_RIP, pos, cb);
        return belt;
    }

    private static boolean solid(World world, int x, int y, int z) {
        BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
        return t != null && t.getMaterial() == BlockMaterial.Solid;
    }

    /** Bloc en orbite (from : case d'origine, pour la montee). */
    private static final class Rock {
        final Ref<EntityStore> ref;
        final Vector3d from;
        final double phase;
        final long born;
        Vector3d pos;
        Rock(Ref<EntityStore> ref, Vector3d from, double phase, long born) { this.ref = ref; this.from = from; this.pos = new Vector3d(from); this.phase = phase; this.born = born; }
    }

    /** Bloc tire : ligne droite vers le curseur, se brise au premier ennemi ou bloc. */
    private static final class Shot {
        final Ref<EntityStore> ref;
        final Vector3d vel;
        final long at;
        Vector3d pos;
        Shot(Ref<EntityStore> ref, Vector3d pos, Vector3d vel, long at) { this.ref = ref; this.pos = pos; this.vel = vel; this.at = at; }
    }

    private static final class Belt implements Instance {
        final Ref<EntityStore> caster;
        final World world;
        final long startAt;
        final TerrainRestore.Restore terrain;
        final List<Rock> rocks = new ArrayList<>();
        final List<Shot> shots = new ArrayList<>();
        int pendingShots;
        long lastTick, lastAura;

        Belt(Ref<EntityStore> caster, World world, long startAt) {
            this.caster = caster; this.world = world; this.startAt = startAt; this.lastTick = startAt;
            this.terrain = TerrainRestore.open(world, startAt + RESTORE_AFTER_MS);
        }

        /** Choisit jusqu'a 8 blocs de surface (solides, a l'air libre, ni plante ni fluide ni zone protegee) et les arrache. */
        void rip(Vector3d pos, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            List<int[]> candidates = new ArrayList<>();
            int px = (int) Math.floor(pos.x), py = (int) Math.floor(pos.y), pz = (int) Math.floor(pos.z), r = (int) PICK_MAX;
            for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
                double d = Math.hypot(dx, dz);
                if (d < PICK_MIN || d > PICK_MAX) continue;
                for (int y = py + 1; y >= py - 3; y--) {
                    int x = px + dx, z = pz + dz;
                    if (!solid(world, x, y, z) || solid(world, x, y + 1, z)) continue;
                    BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                    String id = t.getId();
                    if (id.startsWith("Plant_") || TerrainRestore.isKept(world, id, x, y, z) || TerrainRestore.isProtectedArea(world.getName(), x, y, z)
                            || WorldBlockUtil.getFluidIndex(world, x, y + 1, z) > 0) break;
                    candidates.add(new int[]{x, y, z});
                    break;
                }
            }
            Collections.shuffle(candidates);
            List<int[]> cells = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            long now = System.currentTimeMillis();
            for (int[] c : candidates) {
                if (rocks.size() >= ROCKS) break;
                String id = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]).getId();
                Vector3d at = new Vector3d(c[0] + 0.5, c[1] + 0.5, c[2] + 0.5);
                Ref<EntityStore> ref = SpellFx.spawnBlock(id, at, store, cb);
                if (ref == null) continue;
                rocks.add(new Rock(ref, at, rocks.size() * 2 * Math.PI / ROCKS, now));
                cells.add(c);
                ids.add(id);
                SpellFx.fx(FX_CRUMBS, at, 0.8f, store, cb);
            }
            if (cells.isEmpty()) return;
            world.execute(() -> {
                try {
                    for (int i = 0; i < cells.size(); i++) {
                        int[] c = cells.get(i);
                        BlockType t = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]);
                        if (t != null && ids.get(i).equals(t.getId())) TerrainRestore.place(world, terrain, c[0], c[1], c[2], ids.get(i), TerrainRestore.EMPTY, 0);
                    }
                } catch (Exception e) {
                    EldaniorLogger.error("CeintureAsteroides: arrachage", e);
                }
            });
        }

        @Override public boolean listensClicks() { return true; }

        @Override
        public void onClick(boolean left, Ref<EntityStore> target, Ref<EntityStore> casterRef, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            pendingShots++;
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            double dt = Math.min(0.2, (now - lastTick) / 1000.0);
            lastTick = now;
            boolean aura = now - lastAura >= AURA_EVERY_MS;
            if (aura) lastAura = now;

            double t = (now - startAt) / 1000.0;
            for (var it = rocks.iterator(); it.hasNext(); ) {                   // orbite
                Rock r = it.next();
                if (!r.ref.isValid()) { if (now - r.born >= SPAWN_GRACE_MS) it.remove(); continue; }
                double a = r.phase + t * ORBIT_SPEED;
                Vector3d slot = new Vector3d(pos.x + Math.cos(a) * ORBIT_RADIUS, pos.y + ORBIT_HEIGHT + Math.sin(t * 2.0 + r.phase) * BOB, pos.z + Math.sin(a) * ORBIT_RADIUS);
                double k = Math.min(1.0, (now - r.born) / (double) RISE_MS);
                r.pos = k < 1.0 ? new Vector3d(r.from).lerp(slot, k * k * (3 - 2 * k)) : slot;
                Rotation3f spin = new Rotation3f();
                spin.setYaw((float) Math.toRadians((t * TUMBLE + r.phase * 57) % 360));
                spin.setPitch((float) Math.toRadians(Math.sin(t + r.phase) * 25));
                SpellFx.hold(r.ref, r.pos, spin, store);
                if (aura) SpellFx.fx(FX_AURA, r.pos, 0.4f, store, cb);
            }
            while (pendingShots > 0) { pendingShots--; fire(pos, now, store, cb); }
            fly(now, dt, store, cb);
            boolean expired = now - startAt >= DURATION_MS;
            if (expired && !rocks.isEmpty()) {                                   // fin : les blocs restants s'effritent
                for (Rock r : rocks) { SpellFx.fx(FX_CRUMBS, r.pos, 0.8f, store, cb); SpellFx.remove(r.ref, cb); }
                rocks.clear();
            }
            return (expired || rocks.isEmpty()) && shots.isEmpty();
        }

        /** Tire le bloc le plus en avant (dans la direction du regard) vers le point vise. */
        private void fire(Vector3d pos, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d look = SpellFx.lookDirection(caster, store);
            Rock best = null;
            double bestDot = -2;
            for (Rock r : rocks) {
                if (!r.ref.isValid() || now - r.born < RISE_MS) continue;
                double dot = new Vector3d(r.pos).sub(pos).setComponent(1, 0).normalize().dot(new Vector3d(look.x, 0, look.z).normalize());
                if (dot > bestDot) { bestDot = dot; best = r; }
            }
            if (best == null) return;
            rocks.remove(best);
            Vector3d eye = new Vector3d(pos).add(0, 1.6, 0);
            Vector3i b = TargetUtil.getTargetBlock(caster, AIM_RANGE, store);
            Ref<EntityStore> aimed = SpellFx.targetEntity(caster, store, AIM_RANGE);
            Vector3d ap = aimed != null ? SpellFx.position(aimed, store) : null;
            Vector3d aim = ap != null ? ap.add(0, 1, 0) : b != null ? new Vector3d(b.x + 0.5, b.y + 0.5, b.z + 0.5) : new Vector3d(eye).add(new Vector3d(look).mul(AIM_RANGE));
            Vector3d dir = new Vector3d(aim).sub(best.pos);
            if (dir.lengthSquared() < 1e-6) dir.set(look);
            shots.add(new Shot(best.ref, best.pos, dir.normalize().mul(SHOT_SPEED), now));
            SpellFx.sound(SFX_SHOT, best.pos, cb);
        }

        private void fly(long now, double dt, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (var it = shots.iterator(); it.hasNext(); ) {
                Shot s = it.next();
                Vector3d next = new Vector3d(s.pos).add(new Vector3d(s.vel).mul(dt));
                Ref<EntityStore> hit = null;
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, next, HIT_RADIUS + 1.5, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p != null && new Vector3d(p).add(0, 1, 0).distance(next) <= HIT_RADIUS + 0.5) { hit = e; break; }
                }
                boolean wall = solid(world, (int) Math.floor(next.x), (int) Math.floor(next.y), (int) Math.floor(next.z));
                if (hit != null || wall || now - s.at >= SHOT_MAX_MS) {
                    impact(hit, next, s, store, cb);
                    it.remove();
                    continue;
                }
                s.pos = next;
                SpellFx.hold(s.ref, s.pos, null, store);
                SpellFx.fx(FX_AURA, s.pos, 0.35f, store, cb);                      // trainee violette
            }
        }

        private void impact(Ref<EntityStore> hit, Vector3d at, Shot s, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d push = new Vector3d(s.vel).normalize().mul(8).add(0, 3, 0);
            if (hit != null) SpellFx.damage(caster, hit, "Bludgeoning", DAMAGE, push, "Impact_Light", cb);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, SPLASH_RADIUS, store)) {
                if (e.equals(hit)) continue;
                SpellFx.damage(caster, e, "Bludgeoning", SPLASH_DAMAGE, new Vector3d(push).mul(0.5), null, cb);
            }
            SpellFx.fx(FX_DUST, at, 1.0f, store, cb);
            SpellFx.fx(FX_CRUMBS, at, 1.2f, store, cb);
            SpellFx.sound(SFX_IMPACT, at, cb);
            SpellFx.remove(s.ref, cb);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Rock r : rocks) SpellFx.remove(r.ref, cb);
            for (Shot s : shots) SpellFx.remove(s.ref, cb);
            rocks.clear();
            shots.clear();
            TerrainRestore.save();
        }

        @Override
        public void abort() {
            for (Rock r : rocks) RuneSpellRuntime.removeLater(r.ref);
            for (Shot s : shots) RuneSpellRuntime.removeLater(s.ref);
            TerrainRestore.save();
        }
    }
}
