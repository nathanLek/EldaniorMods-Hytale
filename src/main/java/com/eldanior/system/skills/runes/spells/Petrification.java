package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rune de la Petrification (Terre, Epic, rune de predilection du Transmutateur Divin) : l'ennemi vise (25 blocs) et jusqu'a
 * 2 ennemis a 3 blocs de lui deviennent des statues 2 s (lockInPlace + Elda_Petrifie). Les coups du lanceur sur eux sont
 * doubles (echo de 100 %, markEcho) ; a la fin la pierre se brise : 120 degats.
 * Fiche de conception : scripts/runes/specs/Rune_Petrification.json
 */
public final class Petrification implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long STONE_MS = 2_000;
    private static final double RANGE = 25.0, SPREAD = 3.0;
    private static final int TARGETS = 3;
    private static final float SHATTER = 120f, BONUS = 1.0f;

    // ===== Assets =====
    private static final String STONE = "Elda_Petrifie";
    private static final String FX_DUST = "Gaia_Dust_Burst", FX_CRUMBS = "Block_Break_Dust";
    private static final String SFX_STONE = "SFX_Golem_Earth_Wake", SFX_SHATTER = "SFX_Crystal_Break";

    @Override public String id() { return "Petrification"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Ref<EntityStore> first = SpellFx.targetEntity(caster, store, RANGE);
        if (first == null) first = nearestAhead(caster, pos, store);
        Vector3d fp = first != null ? SpellFx.position(first, store) : null;
        if (fp == null) return null;
        Set<Ref<EntityStore>> statues = ConcurrentHashMap.newKeySet();
        statues.add(first);
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, fp, SPREAD, store)) {
            if (statues.size() >= TARGETS) break;
            if (!RuneSpellRuntime.isSummon(e)) statues.add(e);
        }
        for (Ref<EntityStore> e : statues) {
            SpellFx.lockInPlace(e, STONE_MS, store);
            SpellFx.effect(e, STONE, cb);
            Vector3d p = SpellFx.position(e, store);
            if (p != null) SpellFx.fx(FX_DUST, p, 1.0f, store, cb);
        }
        SpellFx.sound(SFX_STONE, fp, cb);
        return new Statues(statues, System.currentTimeMillis());
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

    private record Hit(Ref<EntityStore> target, float amount) {}

    private static final class Statues implements Instance {
        final Set<Ref<EntityStore>> statues;
        final long startAt;
        final Queue<Hit> hits = new ConcurrentLinkedQueue<>();

        Statues(Set<Ref<EntityStore>> statues, long startAt) { this.statues = statues; this.startAt = startAt; }

        @Override
        public void onOwnerHit(Ref<EntityStore> target, float amount) {
            if (amount > 0 && statues.contains(target)) hits.add(new Hit(target, amount));
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Hit h;
            while ((h = hits.poll()) != null) {                                   // la pierre fragile prend double
                float bonus = h.amount() * BONUS;
                if (SpellFx.position(h.target(), store) == null || bonus < 0.5f) continue;
                RuneSpellRuntime.markEcho(h.target(), bonus);
                SpellFx.damage(caster, h.target(), "Bludgeoning", bonus, null, null, cb);
                Vector3d p = SpellFx.position(h.target(), store);
                if (p != null) SpellFx.fx(FX_CRUMBS, new Vector3d(p).add(0, 1, 0), 0.6f, store, cb);
            }
            if (now - startAt < STONE_MS) return false;
            for (Ref<EntityStore> e : statues) {                                   // la statue vole en eclats
                SpellFx.unlock(e);
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                try { EffectsManager.removeEffect(e, STONE, cb); } catch (Exception ex) { EldaniorLogger.error("Petrification: effet", ex); }
                SpellFx.damage(caster, e, "Bludgeoning", SHATTER, new Vector3d(0, 2, 0), "Impact_Strong", cb);
                SpellFx.fx(FX_CRUMBS, new Vector3d(p).add(0, 1, 0), 1.6f, store, cb);
                SpellFx.fx(FX_DUST, p, 1.0f, store, cb);
                SpellFx.sound(SFX_SHATTER, p, cb);
            }
            return true;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { for (Ref<EntityStore> e : statues) SpellFx.unlock(e); }
    }
}
