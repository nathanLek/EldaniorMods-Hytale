package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rune du Domaine Divin (Luo, Legendaire, Dieu Martial, voie martiale, 2026-11-10) : pendant 12 s, domaine de 15 blocs centre
 * sur le lanceur (il le suit). Chaque seconde, les ennemis dedans sont ralentis (effet Unite_Vent, x0,6), ecrases par le poids
 * du ciel (50 degats, plaques au sol) ; le lanceur regagne 3 % de sa vie et subit 30 % de degats en moins tant que le domaine
 * dure. Bord du domaine trace en or, colonne de lumiere au centre. Epargne groupe, guilde et famille (enemiesAround).
 * Fiche de conception : scripts/runes/specs/Rune_DomaineDivin.json
 */
public final class DomaineDivin implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RADIUS = 15.0, CRUSH_DOWN = -9.0;
    private static final float DAMAGE = 50f, REDUCTION = 0.30f, HEAL_PCT = 0.03f;
    private static final long DURATION_MS = 12_000, PULSE_MS = 1_000, RING_MS = 400;

    // ===== Assets =====
    private static final String SLOW = "Unite_Vent";
    /** Retour du 2026-11-10 (« pas ouf ») : voile d'or au sol, dome d'orbes d'or en rotation (4 anneaux, hemisphere de RADIUS),
     *  colonne de lumiere sur le lanceur, poids du ciel visible sur chaque ennemi (gen_fx_martiaux.py). */
    private static final String FX_VEIL = "Domaine_Voile", FX_PRESS = "Domaine_Pression", FX_ORB = "Ki_Orbe_T6";
    private static final double[] DOME_ELEVATION = {0, 25, 50, 72};
    private static final int[] DOME_POINTS = {22, 18, 12, 6};
    private static final long VEIL_MS = 3_000, COLUMN_MS = 1_500;
    private static final String FX_RING = "Eldanior_Pas_Celeste", FX_EDGE = "Gilgamesh_Sword_Burst", FX_CRUSH = "Gaia_Dust_Burst",
            FX_HEAL = "Apollo_Heal_Pulse";
    private static final String SFX_START = "SFX_Longsword_Special_Impact", SFX_CRUSH = "SFX_Sword_T2_Impact";

    @Override public String id() { return "DomaineDivin"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.fx(FX_RING, new Vector3d(pos).add(0, 0.1, 0), 5.0f, store, cb);
        SpellFx.fx(FX_VEIL, pos, 1.0f, store, cb);                              // le voile d'or s'etend sur tout le domaine
        SpellFx.fx("Ki_Colonne_T8", new Vector3d(pos).add(0, 0.1, 0), 2.0f, store, cb);
        for (int h = 0; h < 6; h++) SpellFx.fx(FX_EDGE, new Vector3d(pos).add(0, 1 + h * 1.5, 0), 1.2f, store, cb);
        SpellFx.sound(SFX_START, pos, cb);
        SpellFx.shake(pos, RADIUS, "Impact_Strong", store);
        return new Domain(System.currentTimeMillis());
    }

    private static final class Domain implements Instance {
        final long startAt;
        final Map<Ref<EntityStore>, Long> own = new ConcurrentHashMap<>();
        long lastPulse, lastRing, lastVeil, lastColumn;

        Domain(long startAt) { this.startAt = startAt; }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            return amount * (1f - REDUCTION);
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d me = SpellFx.position(caster, store);
            if (me == null || now - startAt >= DURATION_MS) return true;
            if (now - lastRing >= RING_MS) {                                    // dome d'orbes d'or qui tourne
                lastRing = now;
                double turn = (now - startAt) / 1500.0;
                for (int ring = 0; ring < DOME_ELEVATION.length; ring++) {
                    double el = Math.toRadians(DOME_ELEVATION[ring]), r = RADIUS * Math.cos(el), h = RADIUS * Math.sin(el) * 0.6;
                    int n = DOME_POINTS[ring];
                    double dir = ring % 2 == 0 ? 1 : -1;
                    for (int i = 0; i < n; i++) {
                        double a = dir * turn + i * 2 * Math.PI / n;
                        SpellFx.fx(FX_ORB, new Vector3d(me).add(Math.cos(a) * r, 0.4 + h, Math.sin(a) * r), 1.1f, store, cb);
                    }
                }
            }
            if (now - lastVeil >= VEIL_MS) { lastVeil = now; SpellFx.fx(FX_VEIL, me, 1.0f, store, cb); }
            if (now - lastColumn >= COLUMN_MS) { lastColumn = now; SpellFx.fx("Ki_Colonne_T8", new Vector3d(me).add(0, 0.1, 0), 1.4f, store, cb); }
            if (now - lastPulse < PULSE_MS) return false;
            lastPulse = now;
            own.values().removeIf(v -> v < now);
            SpellFx.restore(caster, DefaultEntityStatTypes.getHealth(), HEAL_PCT, store);
            SpellFx.fx(FX_HEAL, me, 0.8f, store, cb);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, me, RADIUS, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                SpellFx.effect(e, SLOW, cb);
                SpellFx.fx(FX_PRESS, p, 1.0f, store, cb);                       // le poids du ciel tombe sur lui
                SpellFx.fx(FX_CRUSH, p, 0.6f, store, cb);
                MartialKi.kiDamage(caster, e, DAMAGE, "Physical", new Vector3d(0, CRUSH_DOWN, 0), own, cb);
            }
            SpellFx.sound(SFX_CRUSH, me, cb);
            return false;
        }
    }
}
