package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Set;

/**
 * Rune de la Tempete de Feuilles (Air, Epic, rune de predilection du Druide Primordial) : tornade de feuilles de 8 s au
 * point vise (20 blocs), qui suit le viseur du lanceur a 5 b/s en restant au sol. Ennemis a moins de 5 blocs aspires vers
 * l'axe en tournoyant (poussee imposee / 0,25 s) ; a moins de 3,5 blocs : 16 degats tranchants / 0,5 s. A la fin, les
 * ennemis pris dans la tornade sont ejectes vers l'exterieur (60 degats de vent).
 * Visuels : modele Tempete_Feuilles (tornade vanilla, trainees Wind) deplace par le code + particule Tempete_Feuilles / s.
 * Fiche de conception : scripts/runes/specs/Rune_TempeteFeuilles.json
 */
public final class TempeteFeuilles implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 8_000, PULL_EVERY_MS = 250, CUT_EVERY_MS = 500, LEAVES_EVERY_MS = 1_000;
    private static final double RANGE = 20.0, SPEED = 5.0, PULL_RADIUS = 5.0, RADIUS = 3.5, HEIGHT = 5.0;
    private static final double PULL_IN = 4.0, SPIN = 3.5, LIFT = 1.5, EJECT = 12.0, EJECT_LIFT = 8.0;
    private static final float CUT = 16f, BURST = 60f;

    // ===== Assets =====
    private static final String TORNADO = "Projectile_Config_Tempete_Feuilles";
    private static final String FX_LEAVES = "Tempete_Feuilles", FX_HIT = "Block_Break_Leaves", FX_DUST = "Gaia_Dust_Burst";
    private static final String SFX_RISE = "SFX_Leaves_Break", SFX_END = "SFX_Leaves_Break";

    @Override public String id() { return "TempeteFeuilles"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        if (SpellFx.position(caster, store) == null || world == null) return null;
        Vector3d center = SpellFx.ground(world, SpellFx.targetPoint(caster, store, RANGE, RANGE));
        Ref<EntityStore> model = SpellFx.spawnModel(caster, TORNADO, center, new Vector3d(1, 0, 0), cb);
        SpellFx.notSaved(model, store, cb);
        SpellFx.fx(FX_LEAVES, center, 1.0f, store, cb);
        SpellFx.fx(FX_DUST, center, 1.2f, store, cb);
        SpellFx.sound(SFX_RISE, center, cb);
        return new Storm(world, center, model, System.currentTimeMillis());
    }

    private static final class Storm implements Instance {
        final World world;
        final Vector3d center;
        final Ref<EntityStore> model;
        final long startAt;
        final Set<Ref<EntityStore>> caught = new HashSet<>();
        long lastTick, lastPull, lastCut, lastLeaves;

        Storm(World world, Vector3d center, Ref<EntityStore> model, long startAt) {
            this.world = world; this.center = center; this.model = model; this.startAt = startAt; this.lastTick = startAt; this.lastLeaves = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            if (now - startAt >= DURATION_MS) { eject(caster, store, cb); return true; }
            double dt = Math.min(0.2, (now - lastTick) / 1000.0);
            lastTick = now;
            Vector3d aim = SpellFx.targetPoint(caster, store, RANGE, RANGE);    // la tornade suit le viseur, au sol
            Vector3d delta = new Vector3d(aim.x - center.x, 0, aim.z - center.z);
            if (delta.length() > SPEED * dt) delta.normalize().mul(SPEED * dt);
            center.add(delta);
            center.y = SpellFx.ground(world, center).y;
            SpellFx.hold(model, center, null, store);
            if (now - lastLeaves >= LEAVES_EVERY_MS) { lastLeaves = now; SpellFx.fx(FX_LEAVES, center, 1.0f, store, cb); }
            boolean cut = now - lastCut >= CUT_EVERY_MS;
            if (cut) lastCut = now;
            if (now - lastPull >= PULL_EVERY_MS) {
                lastPull = now;
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, PULL_RADIUS + HEIGHT, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || RuneSpellRuntime.isSummon(e)) continue;
                    double d = SpellFx.horizontalDistance(p, center);
                    if (d > PULL_RADIUS || p.y - center.y < -1.5 || p.y - center.y > HEIGHT) continue;
                    Vector3d in = new Vector3d(center.x - p.x, 0, center.z - p.z);
                    if (in.lengthSquared() < 1e-4) in.set(1, 0, 0);
                    in.normalize();
                    Vector3d swirl = new Vector3d(-in.z, 0, in.x).mul(SPIN);     // rotation autour de l'axe
                    SpellFx.push(e, new Vector3d(in).mul(PULL_IN * Math.min(1.0, d / RADIUS)).add(swirl).add(0, LIFT, 0), false, cb);
                    if (d <= RADIUS) caught.add(e);
                }
            }
            if (cut) {
                for (Ref<EntityStore> e : caught) {                              // les feuilles tranchent
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || SpellFx.horizontalDistance(p, center) > RADIUS || p.y - center.y > HEIGHT + 1) continue;
                    SpellFx.damage(caster, e, "Slashing", CUT, null, null, cb);
                    SpellFx.fx(FX_HIT, new Vector3d(p).add(0, 1, 0), 0.7f, store, cb);
                }
            }
            return false;
        }

        /** Fin : la tornade se disperse et rejette tout ce qu'elle tenait. */
        private void eject(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> e : caught) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, center) > PULL_RADIUS + 2) continue;
                Vector3d out = new Vector3d(p.x - center.x, 0, p.z - center.z);
                if (out.lengthSquared() < 1e-4) out.set(1, 0, 0);
                SpellFx.damage(caster, e, "Wind", BURST, out.normalize().mul(EJECT).add(0, EJECT_LIFT, 0), "Impact_Light", cb);
            }
            SpellFx.fx(FX_HIT, new Vector3d(center).add(0, 2, 0), 2.5f, store, cb);
            SpellFx.fx(FX_DUST, center, 1.6f, store, cb);
            SpellFx.sound(SFX_END, center, cb);
            SpellFx.remove(model, cb);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { SpellFx.remove(model, cb); }

        @Override
        public void abort() { RuneSpellRuntime.removeLater(model); }
    }
}
