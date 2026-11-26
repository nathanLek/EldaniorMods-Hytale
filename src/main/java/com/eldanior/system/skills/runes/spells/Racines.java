package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Rune des Racines (Terre, Rare, rune de predilection du Druide) : des racines filent sous terre (22 b/s, en suivant la
 * cible) vers l'ennemi vise (25 blocs) et jusqu'a 2 ennemis a 6 blocs de lui. Au jaillissement : 90 degats, Elda_Racines
 * + lockInPlace 1,5 s (epines : 15 toutes les 0,5 s), puis Yggdrasil_Ronces (x0,6, 1,6 s).
 * Fiche de conception : scripts/runes/specs/Rune_Racines.json
 */
public final class Racines implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long ROOT_MS = 1_500, THORN_EVERY_MS = 500, TRAIL_EVERY_MS = 100, MAX_TRAVEL_MS = 2_500;
    private static final double RANGE = 25.0, SPREAD = 6.0, SPEED = 22.0;
    private static final int TARGETS = 3;
    private static final float GRAB = 90f, THORNS = 15f;

    // ===== Assets =====
    private static final String ROOTS = "Elda_Racines", BRAMBLE = "Yggdrasil_Ronces";
    private static final String FX_TRAIL = "Block_Break_Dirt", FX_DUST = "Gaia_Dust_Burst", FX_LEAVES = "Block_Break_Leaves";
    private static final String SFX_GRAB = "SFX_Emit_Wind_Grass", SFX_THORN = "SFX_Bramble_MoveIn";

    @Override public String id() { return "Racines"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Ref<EntityStore> first = SpellFx.targetEntity(caster, store, RANGE);
        if (first == null || RuneSpellRuntime.isSummon(first)) first = nearestAhead(caster, pos, store);
        Vector3d fp = first != null ? SpellFx.position(first, store) : null;
        if (fp == null) return null;
        List<Ref<EntityStore>> targets = new ArrayList<>();
        targets.add(first);
        List<Ref<EntityStore>> around = SpellFx.enemiesAround(caster, fp, SPREAD, store);
        around.sort((a, b) -> Double.compare(dist(a, fp, store), dist(b, fp, store)));
        for (Ref<EntityStore> e : around) {
            if (targets.size() >= TARGETS) break;
            if (!targets.contains(e) && !RuneSpellRuntime.isSummon(e)) targets.add(e);
        }
        long now = System.currentTimeMillis();
        List<Root> roots = new ArrayList<>();
        for (Ref<EntityStore> t : targets) roots.add(new Root(t, new Vector3d(pos)));
        SpellFx.fx(FX_DUST, pos, 0.8f, store, cb);
        return new Grip(roots, now);
    }

    private static double dist(Ref<EntityStore> e, Vector3d from, Store<EntityStore> store) {
        Vector3d p = SpellFx.position(e, store);
        return p == null ? Double.MAX_VALUE : p.distance(from);
    }

    private static Ref<EntityStore> nearestAhead(Ref<EntityStore> caster, Vector3d pos, Store<EntityStore> store) {
        Vector3d fwd = SpellFx.forward(caster, store);
        Ref<EntityStore> best = null;
        double bestD = Double.MAX_VALUE;
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, new Vector3d(pos).add(new Vector3d(fwd).mul(RANGE / 2)), RANGE / 2 + 1, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null || RuneSpellRuntime.isSummon(e)) continue;
            Vector3d to = new Vector3d(p).sub(pos);
            double d = to.length();
            if (d > RANGE || to.setComponent(1, 0).normalize().dot(fwd) < 0.5) continue;
            if (d < bestD) { bestD = d; best = e; }
        }
        return best;
    }

    /** Une racine : tete qui avance sous terre vers sa cible, puis l'enserre. */
    private static final class Root {
        final Ref<EntityStore> target;
        final Vector3d head;
        long grabbedAt, lastThorn;
        boolean done;

        Root(Ref<EntityStore> target, Vector3d head) { this.target = target; this.head = head; }
    }

    private static final class Grip implements Instance {
        final List<Root> roots;
        final long startAt;
        long lastTick, lastTrail;

        Grip(List<Root> roots, long startAt) { this.roots = roots; this.startAt = startAt; this.lastTick = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            double step = SPEED * Math.min(0.2, (now - lastTick) / 1000.0);
            lastTick = now;
            boolean trail = now - lastTrail >= TRAIL_EVERY_MS;
            if (trail) lastTrail = now;
            boolean active = false;
            for (Root r : roots) {
                if (r.done) continue;
                Vector3d p = SpellFx.position(r.target, store);
                if (p == null) { release(r); continue; }                          // cible morte ou disparue
                if (r.grabbedAt == 0) {                                           // la racine file sous terre
                    Vector3d to = new Vector3d(p).sub(r.head);
                    if (to.length() <= step + 0.5) grab(caster, r, p, now, store, cb);
                    else if (now - startAt > MAX_TRAVEL_MS) { r.done = true; continue; }
                    else {
                        r.head.add(to.normalize().mul(step));
                        if (trail) SpellFx.fx(FX_TRAIL, r.head, 1.0f, store, cb);
                    }
                    active = true;
                    continue;
                }
                if (now - r.grabbedAt >= ROOT_MS) { release(r); applyBramble(r, cb); continue; }
                if (now - r.lastThorn >= THORN_EVERY_MS) {                      // les epines mordent
                    r.lastThorn = now;
                    SpellFx.damage(caster, r.target, "Physical", THORNS, null, null, cb);
                    SpellFx.fx(FX_LEAVES, new Vector3d(p).add(0, 0.6, 0), 0.6f, store, cb);
                }
                active = true;
            }
            return !active;
        }

        private void grab(Ref<EntityStore> caster, Root r, Vector3d p, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            r.grabbedAt = now;
            r.lastThorn = now;
            SpellFx.lockInPlace(r.target, ROOT_MS, store);
            try { EffectsManager.applyCustomEffect(r.target, ROOTS, ROOT_MS / 1000f, OverlapBehavior.OVERWRITE, cb); }
            catch (Exception e) { EldaniorLogger.error("Racines: effet", e); }
            SpellFx.damage(caster, r.target, "Earth", GRAB, null, "Impact_Light", cb);
            SpellFx.fx(FX_DUST, p, 1.0f, store, cb);
            SpellFx.fx(FX_LEAVES, new Vector3d(p).add(0, 0.8, 0), 1.0f, store, cb);
            SpellFx.sound(SFX_GRAB, p, cb);
            SpellFx.sound(SFX_THORN, p, cb);
        }

        private static void release(Root r) {
            r.done = true;
            SpellFx.unlock(r.target);
        }

        private static void applyBramble(Root r, CommandBuffer<EntityStore> cb) {
            if (r.target.isValid()) SpellFx.effect(r.target, BRAMBLE, cb);       // entrave apres l'etreinte
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { for (Root r : roots) if (r.grabbedAt != 0) SpellFx.unlock(r.target); }
    }
}
