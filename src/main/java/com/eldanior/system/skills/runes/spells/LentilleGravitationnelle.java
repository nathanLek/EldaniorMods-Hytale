package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.component.Projectile;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rune de la Lentille Gravitationnelle (Spatial, Unique, rune de predilection de l'Archonte) : lentille de 12 s (rayon 8)
 * au point vise (25 blocs). Toutes les 0,25 s, les ennemis dedans sont devies (poussee tangentielle + legere attraction) et
 * tournent autour du coeur ; les coups du lanceur sur eux sont amplifies de 40 % (markEcho). Les projectiles dont le tireur
 * probable est un ennemi sont captures en orbite (12 max). A la fin : attraction au centre, 200 + 30 par projectile capture.
 * La gravite est invisible : seuls le trou noir (Chaos_BlackHole) et son disque (Chaos_Accretion) la montrent.
 * Fiche de conception : scripts/runes/specs/Rune_LentilleGravitationnelle.json
 */
public final class LentilleGravitationnelle implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 12_000, BEND_EVERY_MS = 250, CORE_FX_EVERY_MS = 300, DISK_FX_EVERY_MS = 1_000;
    private static final double RANGE = 25.0, RADIUS = 8.0, CORE_HEIGHT = 2.0, HEIGHT = 5.0;
    private static final double SWIRL = 2.6, DRAW = 0.8, COLLAPSE_PULL = 1.6, ORBIT_RADIUS = 2.2, ORBIT_TURN_MS = 2_400;
    private static final double MIN_PROJECTILE_SPEED = 3.0, SHOOTER_RANGE = 30.0, AIM_WIDTH = 3.0;
    private static final int MAX_CAPTURED = 12;
    private static final float FOCUS = 0.4f, COLLAPSE = 200f, PER_PROJECTILE = 30f;

    // ===== Assets =====
    private static final String FX_CORE = "Chaos_BlackHole", FX_DISK = "Chaos_Accretion";
    private static final String SFX_OPEN = "SFX_Portal_Void";

    @Override public String id() { return "LentilleGravitationnelle"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        Vector3d ground = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        Vector3d core = new Vector3d(ground).add(0, CORE_HEIGHT, 0);
        SpellFx.fx(FX_CORE, core, 1.0f, store, cb);
        SpellFx.fx(FX_DISK, core, 1.0f, store, cb);
        SpellFx.sound(SFX_OPEN, core, cb);
        return new Lens(caster, ground, core, System.currentTimeMillis());
    }

    private record Hit(Ref<EntityStore> target, float amount) {}

    private static final class Lens implements Instance {
        final Ref<EntityStore> caster;
        final Vector3d ground, core;
        final long startAt;
        final Queue<Hit> hits = new ConcurrentLinkedQueue<>();
        final Set<Ref<EntityStore>> inside = ConcurrentHashMap.newKeySet();          // lu par onOwnerHit
        final List<Ref<EntityStore>> captured = new ArrayList<>();
        final Set<Ref<EntityStore>> ignored = new HashSet<>();
        long lastBend, lastCore, lastDisk;

        Lens(Ref<EntityStore> caster, Vector3d ground, Vector3d core, long startAt) {
            this.caster = caster; this.ground = ground; this.core = core; this.startAt = startAt; this.lastDisk = startAt;
        }

        @Override
        public void onOwnerHit(Ref<EntityStore> target, float amount) {
            if (amount > 0 && inside.contains(target)) hits.add(new Hit(target, amount));
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            if (now - startAt >= DURATION_MS) { collapse(store, cb); return true; }
            Hit h;
            while ((h = hits.poll()) != null) {                                   // la lentille concentre vos coups
                float bonus = h.amount() * FOCUS;
                if (bonus < 0.5f || SpellFx.position(h.target(), store) == null) continue;
                RuneSpellRuntime.markEcho(h.target(), bonus);
                SpellFx.damage(caster, h.target(), "Crush", bonus, null, null, cb);
            }
            capture(store);
            orbit(now, store);
            if (now - lastCore >= CORE_FX_EVERY_MS) { lastCore = now; SpellFx.fx(FX_CORE, core, 1.0f, store, cb); }
            if (now - lastDisk >= DISK_FX_EVERY_MS) { lastDisk = now; SpellFx.fx(FX_DISK, core, 1.0f, store, cb); }
            if (now - lastBend >= BEND_EVERY_MS) { lastBend = now; bend(store, cb); }
            return false;
        }

        /** Les trajectoires des ennemis se courbent autour du coeur. */
        private void bend(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            inside.clear();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, ground, RADIUS + HEIGHT, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || !inZone(p)) continue;
                inside.add(e);
                Vector3d in = new Vector3d(ground.x - p.x, 0, ground.z - p.z);
                if (in.lengthSquared() < 1e-4) continue;
                in.normalize();
                SpellFx.push(e, new Vector3d(-in.z, 0, in.x).mul(SWIRL).add(new Vector3d(in).mul(DRAW)), true, cb);
            }
        }

        private boolean inZone(Vector3d p) {
            return SpellFx.horizontalDistance(p, ground) <= RADIUS && p.y - ground.y > -2 && p.y - ground.y < HEIGHT;
        }

        /** Projectiles ennemis entrant dans la lentille : pris en orbite. */
        private void capture(Store<EntityStore> store) {
            if (captured.size() >= MAX_CAPTURED) return;
            for (Ref<EntityStore> ref : TargetUtil.getAllEntitiesInSphere(core, RADIUS, store)) {
                if (captured.size() >= MAX_CAPTURED) return;
                if (ignored.contains(ref) || captured.contains(ref) || store.getComponent(ref, Projectile.getComponentType()) == null) continue;
                Velocity v = store.getComponent(ref, Velocity.getComponentType());
                Vector3d p = SpellFx.position(ref, store);
                if (v == null || p == null || v.getSpeed() < MIN_PROJECTILE_SPEED) continue;
                Vector3d back = new Vector3d(-v.getX(), -v.getY(), -v.getZ()).normalize();
                if (shooterIsEnemy(p, back, store)) captured.add(ref); else ignored.add(ref);
            }
        }

        private void orbit(long now, Store<EntityStore> store) {
            captured.removeIf(r -> !r.isValid());
            int n = captured.size();
            for (int i = 0; i < n; i++) {
                double a = 2 * Math.PI * ((now - startAt) % ORBIT_TURN_MS) / ORBIT_TURN_MS + i * 2 * Math.PI / n;
                double tilt = Math.sin(a * 2 + i) * 0.6;
                SpellFx.hold(captured.get(i), new Vector3d(core).add(Math.cos(a) * ORBIT_RADIUS, tilt, Math.sin(a) * ORBIT_RADIUS), null, store);
            }
        }

        /** Le tireur probable (entite la plus proche de la trajectoire inverse) est-il un ennemi du lanceur ? */
        private boolean shooterIsEnemy(Vector3d from, Vector3d back, Store<EntityStore> store) {
            Ref<EntityStore> best = null;
            double bestScore = Double.MAX_VALUE;
            Vector3d mid = new Vector3d(from).add(new Vector3d(back).mul(SHOOTER_RANGE / 2));
            for (Ref<EntityStore> e : TargetUtil.getAllEntitiesInSphere(mid, SHOOTER_RANGE / 2 + AIM_WIDTH, store)) {
                if (store.getComponent(e, Projectile.getComponentType()) != null) continue;
                Vector3d q = SpellFx.position(e, store);
                if (q == null) continue;
                Vector3d to = new Vector3d(q).add(0, 1, 0).sub(from);
                double along = to.dot(back);
                if (along < 0 || along > SHOOTER_RANGE) continue;
                double off = new Vector3d(to).sub(new Vector3d(back).mul(along)).length();
                if (off > AIM_WIDTH) continue;
                if (off + along * 0.05 < bestScore) { bestScore = off + along * 0.05; best = e; }
            }
            return best != null && SpellFx.isEnemy(caster, best, store);
        }

        /** Effondrement : tout converge vers le coeur. */
        private void collapse(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            float dmg = COLLAPSE + PER_PROJECTILE * captured.size();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, ground, RADIUS + HEIGHT, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || !inZone(p)) continue;
                Vector3d in = new Vector3d(ground.x - p.x, 0, ground.z - p.z).mul(COLLAPSE_PULL).add(0, 2, 0);
                SpellFx.damage(caster, e, "Crush", dmg, in, "Impact_Strong", cb);
            }
            for (Ref<EntityStore> r : captured) SpellFx.remove(r, cb);
            captured.clear();
            SpellFx.fx(FX_CORE, core, 2.0f, store, cb);
            SpellFx.fx(FX_DISK, core, 2.0f, store, cb);
            SpellFx.sound(SFX_OPEN, core, cb);
            SpellFx.shake(core, 20, "Impact_Strong", store);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { for (Ref<EntityStore> r : captured) SpellFx.remove(r, cb); }

        @Override
        public void abort() { for (Ref<EntityStore> r : captured) RuneSpellRuntime.removeLater(r); }
    }
}
