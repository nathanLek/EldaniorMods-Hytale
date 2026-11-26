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
 * Rune de Pluie de Flèches (Combat, Common, rune propre de l'Archer) : une volee de 10 fleches retombe en une seconde sur
 * la zone visee (rayon 4, jusqu'a 20 blocs). Chaque fleche (modele vanilla Arrow_Iron porte par
 * Projectile_Config_PluieDeFleches, deplace par le code) inflige 18 degats a l'impact ; 3 fleches au plus par ennemi.
 * Fiche de conception : scripts/runes/specs/Rune_PluieDeFleches.json
 */
public final class PluieDeFleches implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RANGE = 20.0, RADIUS = 4.0, DROP = 12.0, SPEED = 30.0, HIT_RADIUS = 1.3;
    private static final int ARROWS = 10, MAX_HITS_PER_TARGET = 3;
    private static final float DAMAGE = 18f;
    private static final long INTERVAL_MS = 100, MAX_MS = 3_000;

    // ===== Assets =====
    private static final String CARRIER = "Projectile_Config_PluieDeFleches";
    private static final String FX_IMPACT = "Block_Hit_Dust", SFX_IMPACT = "SFX_Outlander_Hunter_Arrow_Hit";

    @Override public String id() { return "PluieDeFleches"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d center = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        if (center == null || SpellFx.position(caster, store) == null) return null;
        return new Volley(center, SpellFx.forward(caster, store), SpellFx.world(store), System.currentTimeMillis());
    }

    private static final class Arrow {
        final Vector3d pos;
        final double groundY;
        final long spawnAt;
        Ref<EntityStore> model;
        boolean spawned, landed;
        Arrow(Vector3d pos, double groundY, long spawnAt) { this.pos = pos; this.groundY = groundY; this.spawnAt = spawnAt; }
    }

    private static final class Volley implements Instance {
        final List<Arrow> arrows = new ArrayList<>();
        final Map<Ref<EntityStore>, Integer> hits = new HashMap<>();
        final Vector3d fall;
        final long startAt;
        long last;

        Volley(Vector3d center, Vector3d forward, World world, long startAt) {
            this.startAt = this.last = startAt;
            this.fall = new Vector3d(forward).mul(0.25).add(0, -1, 0).normalize();     // tombe en biais, vers l'avant
            ThreadLocalRandom rnd = ThreadLocalRandom.current();
            for (int i = 0; i < ARROWS; i++) {
                double a = rnd.nextDouble(Math.PI * 2), r = RADIUS * Math.sqrt(rnd.nextDouble());
                Vector3d land = new Vector3d(center.x + Math.cos(a) * r, center.y, center.z + Math.sin(a) * r);
                double groundY = world != null ? SpellFx.ground(world, land).y : center.y;
                Vector3d top = new Vector3d(land).sub(new Vector3d(fall).mul(DROP / -fall.y));
                arrows.add(new Arrow(new Vector3d(top.x, groundY + DROP, top.z), groundY, startAt + i * INTERVAL_MS));
            }
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= MAX_MS) return true;
            double step = SPEED * Math.min(0.2, (now - last) / 1000.0);
            last = now;
            boolean pending = false;
            for (Arrow a : arrows) {
                if (a.landed) continue;
                pending = true;
                if (now < a.spawnAt) continue;
                if (!a.spawned) {
                    a.model = SpellFx.spawnModel(caster, CARRIER, a.pos, fall, cb);
                    SpellFx.notSaved(a.model, store, cb);
                    a.spawned = true;
                    continue;
                }
                a.pos.add(new Vector3d(fall).mul(step));
                if (a.pos.y <= a.groundY + 0.2) { land(caster, a, store, cb); continue; }
                SpellFx.hold(a.model, a.pos, SpellFx.facing(fall, true), store);
            }
            return !pending;
        }

        /** Impact : degats aux ennemis proches (3 fleches au plus par ennemi), poussiere, son, fleche retiree. */
        private void land(Ref<EntityStore> caster, Arrow a, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            a.landed = true;
            Vector3d at = new Vector3d(a.pos.x, a.groundY, a.pos.z);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, HIT_RADIUS + 1, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, at) > HIT_RADIUS || Math.abs(p.y - at.y) > 2.5) continue;
                int n = hits.getOrDefault(e, 0);
                if (n >= MAX_HITS_PER_TARGET) continue;
                hits.put(e, n + 1);
                SpellFx.damage(caster, e, "Projectile", DAMAGE, new Vector3d(0, 1.5, 0), "Impact_Light", cb);
            }
            SpellFx.fx(FX_IMPACT, at, 0.6f, store, cb);
            SpellFx.sound(SFX_IMPACT, at, cb);
            SpellFx.remove(a.model, cb);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Arrow a : arrows) SpellFx.remove(a.model, cb);
        }

        @Override
        public void abort() {
            for (Arrow a : arrows) RuneSpellRuntime.removeLater(a.model);
        }
    }
}
