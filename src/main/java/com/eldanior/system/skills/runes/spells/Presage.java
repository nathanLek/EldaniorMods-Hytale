package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rune du Presage (Temps, Epic, rune de predilection du Prophete) : trois lectures de l'avenir (0,5 s, 3,5 s, 6,5 s). A
 * chaque lecture, pour les 6 ennemis les plus proches (15 blocs), un sceau dore est pose la ou ils seront dans 1,5 s
 * (position + deplacement des 0,5 dernieres secondes x3, 6 blocs max). A l'echeance, le sceau frappe tout ennemi a
 * 2 blocs : 120 + Chronos_Fige 1 s.
 * Fiche de conception : scripts/runes/specs/Rune_Presage.json
 */
public final class Presage implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long SAMPLE_EVERY_MS = 250, FORESIGHT_MS = 1_500, SEAL_FX_EVERY_MS = 300, STUN_MS = 1_000;
    private static final long[] READINGS = {500, 3_500, 6_500};
    private static final double RANGE = 15.0, MAX_LEAD = 6.0, SEAL_RADIUS = 2.0, LEAD_FACTOR = FORESIGHT_MS / 500.0;
    private static final int MAX_SEALS = 6, HISTORY = 3;
    private static final float DAMAGE = 120f;

    // ===== Assets =====
    private static final String STUN = "Chronos_Fige";
    private static final String FX_SEAL = "Chronos_Gold", FX_GLOW = "Bifrost_Divine", FX_STRIKE = "Chronos_Burst", FX_PILLAR = "Bifrost_Pillar";
    private static final String SFX_READ = "SFX_Portal_Neutral";

    @Override public String id() { return "Presage"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null || world == null) return null;
        SpellFx.fx(FX_SEAL, new Vector3d(pos).add(0, 1.8, 0), 1.2f, store, cb);
        return new Foresight(world, System.currentTimeMillis());
    }

    private static final class Seal {
        final Vector3d at;
        final long strikeAt;
        long lastFx;
        boolean struck;
        Seal(Vector3d at, long strikeAt) { this.at = at; this.strikeAt = strikeAt; }
    }

    private static final class Foresight implements Instance {
        final World world;
        final long startAt;
        final Map<Ref<EntityStore>, Deque<Vector3d>> history = new HashMap<>();
        final List<Seal> seals = new ArrayList<>();
        int readings;
        long lastSample;

        Foresight(World world, long startAt) { this.world = world; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            long t = now - startAt;
            if (now - lastSample >= SAMPLE_EVERY_MS) { lastSample = now; sample(caster, pos, store); }
            if (readings < READINGS.length && t >= READINGS[readings]) { readings++; read(caster, pos, now, store, cb); }
            boolean pending = false;
            for (Seal s : seals) {
                if (s.struck) continue;
                pending = true;
                if (now >= s.strikeAt) { strike(caster, s, store, cb); continue; }
                if (now - s.lastFx >= SEAL_FX_EVERY_MS) {
                    s.lastFx = now;
                    SpellFx.fx(FX_SEAL, new Vector3d(s.at).add(0, 0.2, 0), 1.0f, store, cb);
                    SpellFx.fx(FX_GLOW, new Vector3d(s.at).add(0, 0.4, 0), 0.6f, store, cb);
                }
            }
            return readings >= READINGS.length && !pending;
        }

        /** Garde les 3 dernieres positions (0,5 s) des ennemis proches. */
        private void sample(Ref<EntityStore> caster, Vector3d pos, Store<EntityStore> store) {
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, RANGE + 4, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Deque<Vector3d> h = history.computeIfAbsent(e, k -> new ArrayDeque<>());
                h.addLast(p);
                while (h.size() > HISTORY) h.removeFirst();
            }
        }

        /** Lecture : un sceau la ou chaque ennemi proche sera dans 1,5 s. */
        private void read(Ref<EntityStore> caster, Vector3d pos, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            List<Ref<EntityStore>> enemies = SpellFx.enemiesAround(caster, pos, RANGE, store);
            enemies.removeIf(RuneSpellRuntime::isSummon);
            enemies.sort((a, b) -> Double.compare(dist(a, pos, store), dist(b, pos, store)));
            int n = 0;
            for (Ref<EntityStore> e : enemies) {
                if (n >= MAX_SEALS) break;
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Deque<Vector3d> h = history.get(e);
                Vector3d lead = h != null && !h.isEmpty() ? new Vector3d(p).sub(h.peekFirst()).mul(LEAD_FACTOR).setComponent(1, 0) : new Vector3d();
                if (lead.length() > MAX_LEAD) lead.normalize().mul(MAX_LEAD);
                seals.add(new Seal(SpellFx.ground(world, new Vector3d(p).add(lead)), now + FORESIGHT_MS));
                n++;
            }
            if (n > 0) SpellFx.sound(SFX_READ, pos, cb);
        }

        private void strike(Ref<EntityStore> caster, Seal s, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            s.struck = true;
            SpellFx.fx(FX_STRIKE, new Vector3d(s.at).add(0, 1, 0), 1.2f, store, cb);
            SpellFx.fx(FX_PILLAR, s.at, 0.3f, store, cb);
            SpellFx.sound(SFX_READ, s.at, cb);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, s.at, SEAL_RADIUS + 2, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || RuneSpellRuntime.isSummon(e) || SpellFx.horizontalDistance(p, s.at) > SEAL_RADIUS || Math.abs(p.y - s.at.y) > 3) continue;
                SpellFx.damage(caster, e, "Elemental", DAMAGE, null, "Impact_Light", cb);
                SpellFx.stun(e, STUN, STUN_MS, store, cb);
            }
        }

        private static double dist(Ref<EntityStore> e, Vector3d from, Store<EntityStore> store) {
            Vector3d p = SpellFx.position(e, store);
            return p == null ? Double.MAX_VALUE : p.distance(from);
        }
    }
}
