package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

/**
 * Rune du Domaine de l'Epee (Combat, Epic, Grand Maitre Martial, voie martiale du Clan Luo) : domaine de 8 blocs centre sur
 * le lanceur (il le suit) pendant 8 s. Tout ennemi qui s'y deplace de plus de 0,3 bloc entre deux releves (0,25 s) est
 * tranche (60, 2 fois par seconde au plus). Les ennemis dans le domaine infligent 20 % de degats en moins au lanceur.
 * Epargne groupe, guilde et famille (enemiesAround).
 * Fiche de conception : scripts/runes/specs/Rune_DomaineEpee.json
 */
public final class DomaineEpee implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RADIUS = 8.0, MOVE = 0.3;
    private static final float DAMAGE = 60f, REDUCTION = 0.20f;
    private static final long DURATION_MS = 8_000, CHECK_MS = 250, CUT_GAP_MS = 500, RING_MS = 500;
    private static final int RING_POINTS = 12;

    // ===== Assets =====
    private static final String FX_CUT = "Impact_Sword_Charged", FX_RING = "Eldanior_Pas_Celeste", FX_EDGE = "Gilgamesh_Sword_Burst";
    private static final String SFX_CUT = "SFX_Ability_Wind_Strike_Slash", SFX_START = "SFX_Longsword_Special_Impact";

    @Override public String id() { return "DomaineEpee"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.fx(FX_RING, new Vector3d(pos).add(0, 0.1, 0), 3.0f, store, cb);
        SpellFx.sound(SFX_START, pos, cb);
        return new Domain(System.currentTimeMillis());
    }

    private static final class Domain implements Instance {
        final long startAt;
        final Map<Ref<EntityStore>, Vector3d> seen = new HashMap<>();
        final Map<Ref<EntityStore>, Long> lastCut = new HashMap<>();
        volatile Vector3d center;
        long lastCheck, lastRing;

        Domain(long startAt) { this.startAt = startAt; }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            Vector3d c = center;
            if (attacker == null || c == null || !attacker.isValid()) return amount;
            Vector3d p = SpellFx.position(attacker, attacker.getStore());
            return p != null && p.distance(c) <= RADIUS ? amount * (1f - REDUCTION) : amount;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d me = SpellFx.position(caster, store);
            if (me == null || now - startAt >= DURATION_MS) return true;
            center = new Vector3d(me);
            if (now - lastRing >= RING_MS) {                                    // bord du domaine
                lastRing = now;
                double turn = (now - startAt) / 1000.0;
                for (int i = 0; i < RING_POINTS; i++) {
                    double a = turn + i * 2 * Math.PI / RING_POINTS;
                    SpellFx.fx(FX_EDGE, new Vector3d(me).add(Math.cos(a) * RADIUS, 0.4, Math.sin(a) * RADIUS), 0.35f, store, cb);
                }
            }
            if (now - lastCheck < CHECK_MS) return false;
            lastCheck = now;
            Map<Ref<EntityStore>, Vector3d> next = new HashMap<>();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, me, RADIUS, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                next.put(e, new Vector3d(p));
                Vector3d before = seen.get(e);
                Long cut = lastCut.get(e);
                if (before == null || before.distance(p) <= MOVE || (cut != null && now - cut < CUT_GAP_MS)) continue;
                lastCut.put(e, now);
                Vector3d chest = new Vector3d(p).add(0, 1.1, 0);
                SpellFx.fx(FX_CUT, chest, 1.0f, store, cb);
                SpellFx.sound(SFX_CUT, chest, cb);
                SpellFx.damage(caster, e, "Physical", DAMAGE, null, "Impact_Light", cb);
            }
            seen.clear();
            seen.putAll(next);
            return false;
        }
    }
}
