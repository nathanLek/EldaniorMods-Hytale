package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune de la Fissure du Vide (Espace, Unique, rune de predilection du Void Primordial) : fissure de 12 blocs en travers du
 * regard, au point vise (20 blocs), pendant 12 s (3 failles Chaos_Rift + Nyx_Void). Un ennemi qui la touche est recrache
 * au hasard, a 8-16 blocs et 10 blocs au-dessus d'une case libre : 80 degats (3 fois max, 1 s d'immunite). Le lanceur qui y
 * entre est projete 12 blocs plus loin dans la direction de son regard (case libre, protection de chute).
 * Fiche de conception : scripts/runes/specs/Rune_FissureVide.json
 */
public final class FissureVide implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 12_000, CHECK_EVERY_MS = 100, FX_EVERY_MS = 600, IMMUNE_MS = 1_000, PASS_EVERY_MS = 1_500, FALL_GUARD_MS = 3_000;
    private static final double RANGE = 20.0, HALF_LENGTH = 6.0, TOUCH = 1.5, CASTER_TOUCH = 1.2, HEIGHT = 3.0;
    private static final double SCATTER_MIN = 8.0, SCATTER_MAX = 16.0, DROP = 10.0, PASS_DISTANCE = 12.0, RIFT_HEIGHT = 1.2;
    private static final int MAX_SWALLOWS = 3, TRIES = 5;
    private static final float DAMAGE = 80f;

    // ===== Assets =====
    private static final String RIFT = "Projectile_Config_Chaos_Rift";
    private static final String FX_VOID = "Nyx_Void", FX_FLASH = "Chaos_Flash";
    private static final String SFX_OPEN = "SFX_Portal_Void", SFX_PASS = "SFX_Portal_Neutral";

    @Override public String id() { return "FissureVide"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        if (SpellFx.position(caster, store) == null || world == null) return null;
        Vector3d center = SpellFx.ground(world, SpellFx.targetPoint(caster, store, RANGE, RANGE));
        Vector3d fwd = SpellFx.forward(caster, store);
        Vector3d axis = new Vector3d(-fwd.z, 0, fwd.x);
        if (axis.lengthSquared() < 1e-4) axis.set(1, 0, 0);
        axis.normalize();
        List<Ref<EntityStore>> rifts = new ArrayList<>();
        for (double k : new double[]{-4, 0, 4}) {
            Ref<EntityStore> r = SpellFx.spawnModel(caster, RIFT, new Vector3d(center).add(new Vector3d(axis).mul(k)).add(0, RIFT_HEIGHT, 0), axis, cb);
            SpellFx.notSaved(r, store, cb);
            rifts.add(r);
        }
        SpellFx.sound(SFX_OPEN, center, cb);
        SpellFx.shake(center, 15, "Impact_Light", store);
        return new Fissure(world, center, axis, rifts, System.currentTimeMillis());
    }

    private static final class Fissure implements Instance {
        final World world;
        final Vector3d center, axis, a, b;
        final List<Ref<EntityStore>> rifts;
        final long startAt;
        final Map<Ref<EntityStore>, Integer> swallowed = new HashMap<>();
        final Map<Ref<EntityStore>, Long> immuneUntil = new HashMap<>();
        long lastCheck, lastFx, lastPass;

        Fissure(World world, Vector3d center, Vector3d axis, List<Ref<EntityStore>> rifts, long startAt) {
            this.world = world; this.center = center; this.axis = axis; this.rifts = rifts; this.startAt = startAt;
            this.a = new Vector3d(center).add(new Vector3d(axis).mul(-HALF_LENGTH));
            this.b = new Vector3d(center).add(new Vector3d(axis).mul(HALF_LENGTH));
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d me = SpellFx.position(caster, store);
            if (me == null || now - startAt >= DURATION_MS) return true;
            for (int i = 0; i < rifts.size(); i++)
                SpellFx.hold(rifts.get(i), new Vector3d(center).add(new Vector3d(axis).mul(-4 + 4 * i)).add(0, RIFT_HEIGHT, 0), null, store);
            if (now - lastFx >= FX_EVERY_MS) {
                lastFx = now;
                for (double d = -HALF_LENGTH; d <= HALF_LENGTH; d += 1.5) SpellFx.fx(FX_VOID, new Vector3d(center).add(new Vector3d(axis).mul(d)).add(0, 0.2, 0), 0.8f, store, cb);
            }
            if (now - lastCheck < CHECK_EVERY_MS) return false;
            lastCheck = now;
            if (now - lastPass >= PASS_EVERY_MS && touches(me, CASTER_TOUCH)) { lastPass = now; pass(caster, me, store, cb); }
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, HALF_LENGTH + HEIGHT, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || RuneSpellRuntime.isSummon(e) || !touches(p, TOUCH)) continue;
                Long imm = immuneUntil.get(e);
                int n = swallowed.getOrDefault(e, 0);
                if (n >= MAX_SWALLOWS || (imm != null && now < imm)) continue;
                immuneUntil.put(e, now + IMMUNE_MS);
                if (scatter(caster, e, p, store, cb)) swallowed.put(e, n + 1);
            }
            return false;
        }

        private boolean touches(Vector3d p, double width) {
            if (Math.abs(p.y - center.y) > HEIGHT) return false;
            Vector3d ab = new Vector3d(b).sub(a).setComponent(1, 0);
            Vector3d ap = new Vector3d(p).sub(a).setComponent(1, 0);
            double t = Math.max(0, Math.min(1, ap.dot(ab) / ab.lengthSquared()));
            return ap.distance(new Vector3d(ab).mul(t)) <= width;
        }

        /** L'ennemi est englouti puis recrache au hasard, au-dessus d'une case libre. */
        private boolean scatter(Ref<EntityStore> caster, Ref<EntityStore> e, Vector3d p, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            for (int i = 0; i < TRIES; i++) {
                double ang = rng.nextDouble(Math.PI * 2), dist = rng.nextDouble(SCATTER_MIN, SCATTER_MAX);
                Vector3d spot = LoiArcanique.feet(world, p.x + Math.cos(ang) * dist, p.y, p.z + Math.sin(ang) * dist);
                if (spot == null) continue;
                Vector3d dest = spot.add(0, DROP, 0);
                SpellFx.fx(FX_VOID, new Vector3d(p).add(0, 0.5, 0), 1.0f, store, cb);
                SpellFx.fx(FX_FLASH, dest, 0.6f, store, cb);
                SpellFx.sound(SFX_PASS, p, cb);
                SpellFx.teleport(e, dest, store, cb);
                SpellFx.damage(caster, e, "Elemental", DAMAGE, null, null, cb);
                return true;
            }
            return false;
        }

        /** Le lanceur franchit la fissure : bond de 12 blocs vers son regard. */
        private void pass(Ref<EntityStore> caster, Vector3d me, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d fwd = SpellFx.forward(caster, store);
            for (double d = PASS_DISTANCE; d >= PASS_DISTANCE / 2; d -= 2) {
                Vector3d spot = LoiArcanique.feet(world, me.x + fwd.x * d, me.y, me.z + fwd.z * d);
                if (spot == null) continue;
                SpellFx.fx(FX_VOID, new Vector3d(me).add(0, 0.5, 0), 1.0f, store, cb);
                SpellFx.fx(FX_FLASH, new Vector3d(spot).add(0, 1, 0), 0.8f, store, cb);
                SpellFx.sound(SFX_PASS, spot, cb);
                SpellFx.protectFromFall(caster, FALL_GUARD_MS, store);
                SpellFx.teleport(caster, spot, SpellFx.facing(fwd, false), store, cb);
                return;
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { for (Ref<EntityStore> r : rifts) SpellFx.remove(r, cb); }

        @Override
        public void abort() { for (Ref<EntityStore> r : rifts) RuneSpellRuntime.removeLater(r); }
    }
}
