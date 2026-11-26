package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rune des Chaines Noires (Tenebres, Rare) : enchaine l'ennemi vise a ses deux voisins les plus proches pendant 6 s
 * (faisceaux Nyx_Chain). Les enchaines sont ramenes vers leur centre s'ils s'en ecartent, et chaque coup du lanceur sur
 * l'un d'eux blesse aussi les autres (50 %, coup "echo").
 * Fiche de conception : scripts/runes/specs/Rune_ChainesNoires.json
 */
public final class ChainesNoires implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 6_000, PULL_EVERY_MS = 250;
    private static final double RANGE = 20.0, NEIGHBOUR_RANGE = 8.0, LEASH = 4.0, PULL = 7.0;
    private static final float DAMAGE = 30f, SHARE = 0.5f, CHAIN_SCALE = 0.6f;
    private static final int LINKED = 3;

    // ===== Assets =====
    private static final String CHAIN = "Nyx_Chain";
    private static final String FX_BIND = "Nyx_Chain_Burst";
    private static final String SFX_BIND = "SFX_Bear_Voidtaken_Tentacle";

    @Override public String id() { return "ChainesNoires"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> first = SpellFx.targetEntity(caster, store, RANGE);
        Vector3d fp = SpellFx.position(first, store);
        if (fp == null) return null;
        List<Ref<EntityStore>> bound = new ArrayList<>(List.of(first));
        List<Ref<EntityStore>> near = new ArrayList<>(SpellFx.enemiesAround(caster, fp, NEIGHBOUR_RANGE, store));
        near.remove(first);
        near.sort((a, b) -> Double.compare(dist(a, fp, store), dist(b, fp, store)));
        for (Ref<EntityStore> e : near) {
            if (bound.size() >= LINKED) break;
            if (store.getComponent(e, DeathComponent.getComponentType()) == null && SpellFx.position(e, store) != null) bound.add(e);
        }
        long now = System.currentTimeMillis();
        Bind b = new Bind(bound, now);
        for (Ref<EntityStore> e : bound) {
            Vector3d p = SpellFx.position(e, store);
            SpellFx.damage(caster, e, "Elemental", DAMAGE, null, null, cb);
            SpellFx.fx(FX_BIND, p, 0.8f, store, cb);
        }
        for (int i = 0; i + 1 < bound.size(); i++)                                       // une chaine entre chaque paire voisine
            b.chains.add(SpellFx.spawnBeam(CHAIN, chest(bound.get(i), store), chest(bound.get(i + 1), store), CHAIN_SCALE, DURATION_MS + 1_000, store, cb));
        SpellFx.sound(SFX_BIND, fp, cb);
        return b;
    }

    private static double dist(Ref<EntityStore> e, Vector3d from, Store<EntityStore> store) {
        Vector3d p = SpellFx.position(e, store);
        return p == null ? Double.MAX_VALUE : p.distanceSquared(from);
    }

    private static Vector3d chest(Ref<EntityStore> e, Store<EntityStore> store) {
        Vector3d p = SpellFx.position(e, store);
        return p == null ? null : p.add(0, 1, 0);
    }

    private static final class Bind implements Instance {
        final List<Ref<EntityStore>> bound;
        final List<Ref<EntityStore>> chains = new ArrayList<>();
        final long startAt;
        final Queue<Object[]> hits = new ConcurrentLinkedQueue<>();      // {cible, montant} des coups du lanceur
        long lastPull;

        Bind(List<Ref<EntityStore>> bound, long startAt) { this.bound = bound; this.startAt = startAt; }

        @Override
        public void onOwnerHit(Ref<EntityStore> target, float amount) {
            if (bound.contains(target) && hits.size() < 16) hits.add(new Object[]{target, amount});
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            List<Ref<EntityStore>> alive = new ArrayList<>();
            for (Ref<EntityStore> e : bound)
                if (e.isValid() && store.getComponent(e, DeathComponent.getComponentType()) == null) alive.add(e);
            if (alive.size() < 2) return true;                                          // une chaine a besoin de deux bouts
            for (int i = 0; i < chains.size(); i++) {
                Vector3d a = chest(bound.get(i), store), b = chest(bound.get(i + 1), store);
                if (a != null && b != null) SpellFx.beam(chains.get(i), CHAIN, a, b, CHAIN_SCALE, store);
            }
            for (Object[] h = hits.poll(); h != null; h = hits.poll()) {
                @SuppressWarnings("unchecked") Ref<EntityStore> hit = (Ref<EntityStore>) h[0];
                share(caster, hit, (Float) h[1], alive, cb);
            }
            if (now - lastPull >= PULL_EVERY_MS) { lastPull = now; pull(alive, store, cb); }
            return false;
        }

        /** Le coup porte a un enchaine se repercute sur les autres. */
        private void share(Ref<EntityStore> caster, Ref<EntityStore> hit, float amount, List<Ref<EntityStore>> alive, CommandBuffer<EntityStore> cb) {
            float part = amount * SHARE;
            if (part < 0.5f) return;
            for (Ref<EntityStore> e : alive) {
                if (e.equals(hit)) continue;
                RuneSpellRuntime.markEcho(e, part);
                SpellFx.damage(caster, e, "Elemental", part, null, null, cb);
            }
        }

        /** Un enchaine qui s'ecarte du centre du groupe y est ramene. */
        private void pull(List<Ref<EntityStore>> alive, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d center = new Vector3d();
            int n = 0;
            for (Ref<EntityStore> e : alive) { Vector3d p = SpellFx.position(e, store); if (p != null) { center.add(p); n++; } }
            if (n == 0) return;
            center.div(n);
            for (Ref<EntityStore> e : alive) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d back = new Vector3d(center.x - p.x, 0, center.z - p.z);
                if (back.length() > LEASH) SpellFx.push(e, back.normalize().mul(PULL).add(0, 1, 0), false, cb);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> c : chains) SpellFx.remove(c, cb);
        }

        @Override
        public void abort() {
            for (Ref<EntityStore> c : chains) RuneSpellRuntime.removeLater(c);
        }
    }
}
