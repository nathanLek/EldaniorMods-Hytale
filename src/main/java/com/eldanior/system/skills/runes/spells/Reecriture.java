package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rune de la Reecriture (Espace, Unique, rune de predilection du Demiurge Supreme) : domaine de 12 blocs au point vise
 * (25 blocs) pendant 12 s. Toutes les 2 s, les ennemis du domaine (2 a 12) echangent leurs places : liste melangee puis
 * decalee d'un rang (derangement : aucun ne garde la sienne), teleportation, 60 degats, Gaia_Etourdi 0,5 s au tick
 * suivant (apres la teleportation).
 * Fiche de conception : scripts/runes/specs/Rune_Reecriture.json
 */
public final class Reecriture implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 12_000, REWRITE_EVERY_MS = 2_000, STUN_MS = 500;
    private static final double RANGE = 25.0, RADIUS = 12.0, HEIGHT = 6.0;
    private static final int MAX_TARGETS = 12, RING_POINTS = 24;
    private static final float DAMAGE = 60f;

    // ===== Assets =====
    private static final String STUN = "Gaia_Etourdi", FX_LAW = "Bifrost_Divine";
    private static final String SFX_OPEN = "SFX_Portal_Neutral_Open", SFX_REWRITE = "SFX_Portal_Neutral";

    @Override public String id() { return "Reecriture"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        Vector3d center = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        SpellFx.sound(SFX_OPEN, center, cb);
        Domain d = new Domain(center, System.currentTimeMillis());
        d.ring(store, cb);
        return d;
    }

    private static final class Domain implements Instance {
        final Vector3d center;
        final long startAt;
        final List<Ref<EntityStore>> stunNext = new ArrayList<>();
        long lastRewrite;

        Domain(Vector3d center, long startAt) { this.center = center; this.startAt = startAt; this.lastRewrite = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            for (Ref<EntityStore> e : stunNext) if (e.isValid()) SpellFx.stun(e, STUN, STUN_MS, store, cb);
            stunNext.clear();
            if (now - startAt >= DURATION_MS) return true;
            if (now - lastRewrite >= REWRITE_EVERY_MS) { lastRewrite = now; rewrite(caster, store, cb); ring(store, cb); }
            return false;
        }

        /** Les ennemis du domaine echangent leurs places, aucun ne garde la sienne. */
        private void rewrite(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            List<Ref<EntityStore>> who = new ArrayList<>();
            List<Vector3d> where = new ArrayList<>();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + HEIGHT, store)) {
                if (who.size() >= MAX_TARGETS || RuneSpellRuntime.isSummon(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, center) > RADIUS || Math.abs(p.y - center.y) > HEIGHT) continue;
                who.add(e);
                where.add(p);
            }
            int n = who.size();
            if (n < 2) return;
            List<Integer> order = new ArrayList<>();
            for (int i = 0; i < n; i++) order.add(i);
            Collections.shuffle(order);
            for (int k = 0; k < n; k++) {                                         // decalage d'un rang : derangement
                Ref<EntityStore> e = who.get(order.get(k));
                Vector3d from = where.get(order.get(k)), to = where.get(order.get((k + 1) % n));
                SpellFx.fx(FX_LAW, new Vector3d(from).add(0, 1, 0), 0.8f, store, cb);
                SpellFx.fx(FX_LAW, new Vector3d(to).add(0, 1, 0), 0.8f, store, cb);
                SpellFx.teleport(e, to, store, cb);
                SpellFx.damage(caster, e, "Elemental", DAMAGE, null, null, cb);
                stunNext.add(e);
            }
            SpellFx.sound(SFX_REWRITE, center, cb);
        }

        void ring(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (int i = 0; i < RING_POINTS; i++) {
                double a = i * 2 * Math.PI / RING_POINTS;
                SpellFx.fx(FX_LAW, new Vector3d(center).add(Math.cos(a) * RADIUS, 0.8, Math.sin(a) * RADIUS), 0.6f, store, cb);
            }
        }
    }
}
